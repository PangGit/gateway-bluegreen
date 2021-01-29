package com.github.api.provider;


import org.springframework.web.bind.annotation.GetMapping;


public interface IProviderApi {

    default void test() {
        System.out.println("--------IProviderApi-----------");
    }

    @GetMapping("/test1")
    String test1();

    @GetMapping("/test2")
    String test2();

}
