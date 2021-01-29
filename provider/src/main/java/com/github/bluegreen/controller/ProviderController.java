package com.github.bluegreen.controller;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.api.provider.IProviderApi;
import com.github.bluegreen.api.ICApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/provider")
@RestController
public class ProviderController implements IProviderApi {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private int servicePort;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosServerAddr;

    @Autowired
    private ICApi api;

    @GetMapping(value = "/info")
    public Map<String, String> info() throws NacosException {
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
        return metaData;
    }

    @Override
    public String test1() {
        return serviceName + "-" + this.getClass().getName() + "-" + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    @Override
    public String test2() {
        return this.test1() + "/n" + api.test1();
    }
}
