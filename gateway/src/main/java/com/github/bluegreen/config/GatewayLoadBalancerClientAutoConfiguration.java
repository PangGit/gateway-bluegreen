package com.github.bluegreen.config;

import com.github.bluegreen.filter.LoadBalancerClientFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置自定义filter给spring管理
 */
@Configuration
public class GatewayLoadBalancerClientAutoConfiguration {

    public GatewayLoadBalancerClientAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean({LoadBalancerClientFilter.class})
    public LoadBalancerClientFilter grayReactiveLoadBalancerClientFilter(LoadBalancerClientFactory clientFactory,
                                                                         LoadBalancerProperties properties) {
        return new LoadBalancerClientFilter(clientFactory, properties);
    }
}
