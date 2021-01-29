package com.github.bluegreen.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "provider",path = "/provider")
public interface IPApi {

    @GetMapping("/test1")
    String test1();
}
