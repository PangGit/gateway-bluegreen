package com.github.api.consumer;


import org.springframework.web.bind.annotation.GetMapping;


public interface IConsumerApi {

    default void test() {
        System.out.println("--------IConsumerApi-----------");
    }

    @GetMapping("/test1")
    String test1();

    @GetMapping("/test2")
    String test2();
}
