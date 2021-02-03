package com.github.bluegreen.filter;

import com.alibaba.fastjson.JSONArray;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * OpenFeign拦截器应用(RequestInterceptor)
 */
@Slf4j
@Configuration
public class FeignInterceptor implements RequestInterceptor {

    @Autowired
    private Environment env;

    @Autowired
    private DiscoveryClient client;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        // 传递header
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String values = request.getHeader(name);
                requestTemplate.header(name, values);
            }
        }
        log.info("------------headers:{}", JSONArray.toJSONString(requestTemplate.headers()));

        // 均衡配置
        Map<String, String> configs = parseConfig(request.getHeader("version"));
        List<ServiceInstance> instances = client.getInstances(requestTemplate.feignTarget().name());
        ServiceInstance serviceInstance = null;
        for (ServiceInstance instance : instances) {
            if (Objects.equals(instance.getMetadata().get("version"), configs.get(instance.getServiceId()))) {
                serviceInstance = instance;
                break;
            }
        }
        if (serviceInstance != null) {
            log.info("------------Uri:{}", serviceInstance.getUri());
        }
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

}
