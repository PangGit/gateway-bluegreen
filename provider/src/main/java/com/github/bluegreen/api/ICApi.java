package com.github.bluegreen.api;

import com.github.api.consumer.IConsumerApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("consumer")
public interface ICApi extends IConsumerApi {

}
