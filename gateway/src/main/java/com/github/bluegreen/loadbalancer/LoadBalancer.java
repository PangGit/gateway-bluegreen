package com.github.bluegreen.loadbalancer;

import com.github.bluegreen.weight.model.WeightMeta;
import com.github.bluegreen.weight.util.WeightRandomUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 权重路由
 */
public class LoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Log log = LogFactory.getLog(LoadBalancer.class);

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    private String serviceId;

    private Environment env;

    public LoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId, Environment env) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.env = env;
    }

    public Mono<Response<ServiceInstance>> choose(Request request) {
        HttpHeaders headers = (HttpHeaders) request.getContext();
        if (this.serviceInstanceListSupplierProvider != null) {
            ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
            return ((Flux) supplier.get()).next().map(list -> getInstanceResponse((List<ServiceInstance>) list, headers));
        }
        return null;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, HttpHeaders headers) {
        if (instances.isEmpty()) {
            return getServiceInstanceEmptyResponse();
        } else {
            // return getServiceInstanceResponseWithWeight(instances);
            return getServiceInstanceResponseByVersion(instances, headers);
        }
    }

    /**
     * 根据版本进行分发
     *
     * @param instances 实例列表
     * @param headers   HttpHeaders
     * @return 版本分发
     */
    private Response<ServiceInstance> getServiceInstanceResponseByVersion(List<ServiceInstance> instances, HttpHeaders headers) {
        Map<String, String> configMap = parseConfig(headers);
        ServiceInstance serviceInstance = null;
        for (ServiceInstance instance : instances) {
            if (Objects.equals(instance.getMetadata().get("version"), configMap.get(instance.getServiceId()))) {
                serviceInstance = instance;
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
     * @param headers HttpHeaders
     * @return 配置map
     */
    private Map<String, String> parseConfig(HttpHeaders headers) {
        Map<String, String> configMap = new HashMap<>();
        // url请求中的version
        String version = headers.getFirst("version");
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

    /**
     * 根据在nacos中配置的权重值，进行分发
     *
     * @param instances 实例列表
     * @return 权重值分发
     */
    private Response<ServiceInstance> getServiceInstanceResponseWithWeight(List<ServiceInstance> instances) {
        Map<ServiceInstance, Integer> weightMap = new HashMap<>();
        for (ServiceInstance instance : instances) {
            Map<String, String> metadata = instance.getMetadata();
            System.out.println(metadata.get("version") + "-->weight:" + metadata.get("weight"));
            if (metadata.containsKey("weight")) {
                weightMap.put(instance, Integer.valueOf(metadata.get("weight")));
            }
        }
        WeightMeta<ServiceInstance> weightMeta = WeightRandomUtils.buildWeightMeta(weightMap);
        if (ObjectUtils.isEmpty(weightMeta)) {
            return getServiceInstanceEmptyResponse();
        }
        ServiceInstance serviceInstance = weightMeta.random();
        if (ObjectUtils.isEmpty(serviceInstance)) {
            return getServiceInstanceEmptyResponse();
        }
        System.out.println(serviceInstance.getMetadata().get("version"));
        return new DefaultResponse(serviceInstance);
    }

    private Response<ServiceInstance> getServiceInstanceEmptyResponse() {
        log.warn("No servers available for service: " + this.serviceId);
        return new EmptyResponse();
    }
}