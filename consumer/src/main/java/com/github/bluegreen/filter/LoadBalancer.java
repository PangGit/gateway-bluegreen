package com.github.bluegreen.filter;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 权重路由
 */
@Configuration
public class LoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Log log = LogFactory.getLog(LoadBalancer.class);

    private String serviceId;

    @Autowired
    private LoadBalancerClientFactory clientFactory;

    @Autowired
    private Environment env;

    public LoadBalancer() {
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        String uriHost = clientFactory.getContextNames().iterator().next().toLowerCase();
        ObjectProvider<ServiceInstanceListSupplier> lazyProvider = clientFactory.getLazyProvider(uriHost, ServiceInstanceListSupplier.class);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest servletRequest = attributes.getRequest();

        if (lazyProvider != null) {
            ServiceInstanceListSupplier supplier = lazyProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
            return ((Flux) supplier.get()).next().map(list -> getInstanceResponse((List<ServiceInstance>) list, servletRequest.getHeader("version")));
        }
        return null;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, String version) {
        if (instances.isEmpty()) {
            return getServiceInstanceEmptyResponse();
        } else {
            // return getServiceInstanceResponseWithWeight(instances);
            return getServiceInstanceResponseByVersion(instances, version);
        }
    }

    /**
     * 根据版本进行分发
     *
     * @param instances 实例列表
     * @param version   请求头中的version
     * @return 版本分发
     */
    private Response<ServiceInstance> getServiceInstanceResponseByVersion(List<ServiceInstance> instances, String version) {
        Map<String, String> configMap = parseConfig(version);
        ServiceInstance serviceInstance = null;
        for (ServiceInstance instance : instances) {
            if (Objects.equals(instance.getMetadata().get("version"), configMap.get(instance.getServiceId()))) {
                serviceInstance = instance;
                this.serviceId = instance.getServiceId();
                break;
            }
        }
        if (ObjectUtils.isEmpty(serviceInstance)) {
            return getServiceInstanceEmptyResponse();
        }
        return new DefaultResponse(serviceInstance);
    }

    /**
     * 解析配置消息
     *
     * @param version 请求头中的version
     * @return 配置map
     */
    private Map<String, String> parseConfig(String version) {
        Map<String, String> configMap = new HashMap<>();
        // url请求中的version
        log.info("-----------version : " + version);
        if (StringUtils.isBlank(version)) {
            return configMap;
        }
        // 消息内容，例 a=1,b=2
        String config = env.getProperty("release." + version);
        log.info("-----------config : " + config);
        if (StringUtils.isBlank(config)) {
            return configMap;
        }
        String[] array = config.split(",");
        for (String node : array) {
            String[] sv = node.split("=");
            configMap.put(sv[0], sv[1]);
        }
        return configMap;
    }

    private Response<ServiceInstance> getServiceInstanceEmptyResponse() {
        log.warn("No servers available for service: " + this.serviceId);
        return new EmptyResponse();
    }
}