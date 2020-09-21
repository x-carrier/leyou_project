package com.xiaohong.goods.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

//feign注解
@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {

}
