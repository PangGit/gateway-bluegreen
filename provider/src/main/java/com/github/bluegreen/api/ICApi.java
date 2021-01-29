package com.github.bluegreen.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "consumer", path = "/consumer")
public interface ICApi {

    @GetMapping("/test1")
    String test1();

}
