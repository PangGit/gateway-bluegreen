package com.github.bluegreen.api;

import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "provider", path = "/provider" /*,configuration = {MyConfiguration.class}*/)
public interface IPApi {

    @GetMapping(value = "/info")
    String info() throws NacosException;
}
