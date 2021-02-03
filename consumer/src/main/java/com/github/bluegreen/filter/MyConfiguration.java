//package com.github.bluegreen.filter;
//
//import feign.RequestInterceptor;
//import feign.Retryer;
//import feign.codec.ErrorDecoder;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//
///**
// * feign 客户端的自定义配置
// */
//@Slf4j
//public class MyConfiguration {
//
//    /**
//     * 自定义重试机制
//     */
//    @Bean
//    public Retryer retryer() {
//        //最大请求次数为5，初始间隔时间为100ms，下次间隔时间1.5倍递增，重试间最大间隔时间为1s，
//        return new Retryer.Default();
//    }
//
//    @Bean
//    public ErrorDecoder decoder() {
//        return (key, response) -> {
//            if (response.status() == 400) {
//                log.error("请求xxx服务400参数错误,返回:{}", response.body());
//            }
//
//            if (response.status() == 409) {
//                log.error("请求xxx服务409异常,返回:{}", response.body());
//            }
//
//            if (response.status() == 404) {
//                log.error("请求xxx服务404异常,返回:{}", response.body());
//            }
//
//            // 其他异常交给Default去解码处理
//            // 这里使用单例即可，Default不用每次都去new
//            return new ErrorDecoder.Default().decode(key, response);
//        };
//    }
//
//    /**
//     * feign 拦截器
//     */
//    @Bean
//    public RequestInterceptor interceptor() {
//        return template -> {
//            System.out.println("-------------------feign 拦截器---------------------");
//        };
//    }
//}