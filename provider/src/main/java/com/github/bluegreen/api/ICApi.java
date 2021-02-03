package com.github.bluegreen.api;

import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(value = "consumer", path = "/consumer")
public interface ICApi {

    @GetMapping(value = "/info")
    String info() throws NacosException;

}
