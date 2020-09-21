package com.leyou.cart.service;

import com.leyou.cart.client.GoodClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private GoodClient goodClient;

    private static final String KEY_PREFIX = "user:cart:";

    public void addCart(Cart cart) {
        //获取skuid
        String key = cart.getSkuId().toString();
        //获取num
        Integer num = cart.getNum();
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //查询购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        //判断当前商品是否在购物车中
        if (hashOperations.hasKey(key)){
            //在购物车中，则更新数量
            //获取到购物车中的对象（是一个json对象）
            String catJson = hashOperations.get(key).toString();
            //反序列化
            cart = JsonUtils.parse(catJson, Cart.class);
            cart.setNum(cart.getNum()+num);

        }else {
            //不在则更新购物车
            //完善cart
            cart.setUserId(userInfo.getId());
            Sku sku = goodClient.querySkuBySkuId(cart.getSkuId());
            //图片是一个集合
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
        }
        //更新到redis中
        hashOperations.put(key,JsonUtils.serialize(cart));
    }

    /**
     * 查询购物车
     * @return
     */
    public List<Cart> queryCarts() {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断是否有该用户的购物车记录
        if (!stringRedisTemplate.hasKey(KEY_PREFIX+userInfo.getId())){
            return null;
        }
        //获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        //获取所有的value集合(Json结构)
        List<Object> cartJson = hashOperations.values();
        //判断购物车是否为空
        if (CollectionUtils.isEmpty(cartJson)){
            return null;
        }
        //反序列化为List<Cart>
        return cartJson.stream().map(cart -> JsonUtils.parse(cart.toString(),Cart.class)).collect(Collectors.toList());

    }

    /**
     * 更新数量
     * @param cart
     */
    public void updateNum(Cart cart) {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断是否有该用户的购物车记录
        if (!stringRedisTemplate.hasKey(KEY_PREFIX+userInfo.getId())){
            return;
        }

        Integer num = cart.getNum();

        //获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        //获取对应的购物车集合
        String cartJson = hashOperations.get(cart.getSkuId().toString()).toString();
        //反序列化
        cart = JsonUtils.parse(cartJson, Cart.class);
        cart.setNum(num);
        //更新cart
        hashOperations.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    /**
     * 删除购物车
     */
    public void deleteCart(String skuId){
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断是否有该用户的购物车记录
        if (!stringRedisTemplate.hasKey(KEY_PREFIX+userInfo.getId())){
            return;
        }
        //获取用户的购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        //删除指定购物车记录
        hashOperations.delete(skuId);
    }
}
