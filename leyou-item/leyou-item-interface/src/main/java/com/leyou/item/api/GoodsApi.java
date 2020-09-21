package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 提供对外开放的接口api，外界可以直接继承次接口获得次微服务里对外开放的方法
 */
public interface GoodsApi {
    /**
     * 根据spuId查询SpuDetail
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public SpuDetail querySpuDetailBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据条件分页查询spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows);

    /**
     * 根据SpuId查询sku集合
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public List<Sku> querySkuBySpuId(@RequestParam("id") Long spuId);

    /**
     * 根据spu的id查询SPu
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public Spu querySpuById(@RequestParam("id") Long id);

    /**
     * 根据id查询sku
     */
    @GetMapping("sku/{skuId}")
    public Sku querySkuBySkuId(@PathVariable("skuId") Long skuId);

}
