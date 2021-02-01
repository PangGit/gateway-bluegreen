package com.github.bluegreen.controller;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.bluegreen.api.IPApi;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RequestMapping("/consumer")
@RestController
public class ConsumerController {

    private static final Log log = LogFactory.getLog(ConsumerController.class);

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private int servicePort;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosServerAddr;

    @Autowired
    private NacosServiceDiscovery client;

    @Autowired
    private IPApi api;

    @GetMapping(value = "/info")
    public String info() throws NacosException {
        NamingService namingService = NacosFactory.createNamingService(nacosServerAddr);
        Map<String, String> metaData = new LinkedHashMap<>();
        List<Instance> instances = namingService.selectInstances(serviceName, true);
        if (!CollectionUtils.isEmpty(instances)) {
            for (Instance instance : instances) {
                if (servicePort == instance.getPort()) {
                    metaData.put("ip", instance.getIp());
                    metaData.put("port", String.valueOf(instance.getPort()));
                    metaData.putAll(instance.getMetadata());
                }
            }
        }
        return metaData.toString();
    }

    @GetMapping("/test")
    public String test(HttpServletRequest request) throws NacosException {

        Map<String, String> configMap = parseConfig(request.getHeader("version"));
        List<ServiceInstance> instances = client.getInstances("provider");

        ServiceInstance instance = null;
        for (ServiceInstance service : instances) {
            if (Objects.equals(service.getMetadata().get("version"), configMap.get(service.getServiceId()))) {
                instance = service;
                break;
            }
        }

        RestTemplate restTemplate = new RestTemplate();

        return this.info() + "----------------" + (instance != null ? restTemplate.getForObject(instance.getUri() + "/provider/info", String.class) : null);
    }

    @Autowired
    private Environment env;

    /**
     * 解析配置消息
     *
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
