package com.github.bluegreen.api;

import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "provider",path = "/provider")
public interface IPApi {

    @GetMapping(value = "/info")
    Map<String, String> info() throws NacosException;
}
