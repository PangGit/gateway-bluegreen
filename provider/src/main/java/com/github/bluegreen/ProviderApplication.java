package com.github.bluegreen;

import com.github.bluegreen.filter.LoadBalancer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@LoadBalancerClient(name = "Provider-Balancer", configuration = {LoadBalancer.class})
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

}
