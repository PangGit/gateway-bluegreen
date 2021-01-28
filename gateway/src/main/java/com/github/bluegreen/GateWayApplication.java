package com.github.bluegreen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableDiscoveryClient
public class GateWayApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(GateWayApplication.class, args);
        System.err.println("----------release.version:" + applicationContext.getEnvironment().getProperty("release.v2021"));
        System.err.println("----------property:" + applicationContext.getEnvironment().getProperty("gateway.property"));
    }
}
