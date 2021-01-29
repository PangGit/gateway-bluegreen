package com.github.bluegreen.api;

import com.github.api.provider.IProviderApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "provider")
public interface IPApi extends IProviderApi {

}
