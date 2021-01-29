package com.github.bluegreen.config;

import com.github.bluegreen.filter.LoadBalancerClientFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 配置自定义filter给spring管理
 */
@Configuration
public class GatewayConfiguration {

    public GatewayConfiguration() {
    }

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnMissingBean({LoadBalancerClientFilter.class})
    public LoadBalancerClientFilter filter(LoadBalancerClientFactory clientFactory,
                                           LoadBalancerProperties properties) {
        return new LoadBalancerClientFilter(clientFactory, properties, env);
    }
}
