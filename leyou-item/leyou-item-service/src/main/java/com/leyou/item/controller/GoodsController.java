package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    //spu/page?key=&saleable=true&page=1&rows=5

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows)
    {
        PageResult<SpuBo> spuBos = this.goodsService.querySpuByPage(key,saleable,page,rows);
        if (spuBos == null || CollectionUtils.isEmpty(spuBos.getItems())){
            ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(spuBos);

    }

    /**
     * 保存商品信息
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
//    接收json参数必须用RequestBody注解
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        this.goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 更新商品信息
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
//    接收json参数必须用RequestBody注解
    //更新或删除成功显示204
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId") Long spuId){
        SpuDetail spuDetail =this.goodsService.querySpuDetailBySpuId(spuId);
        if (spuDetail == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据SpuId查询sku集合
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku> > querySkuBySpuId(@RequestParam("id") Long spuId){
        List<Sku> sku =this.goodsService.querySkuBySpuId(spuId);
        if (CollectionUtils.isEmpty(sku)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);
    }

    /**
     * 根据spu的id查询SPu
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Spu> querySpuById(@RequestParam("id") Long id){
        Spu spu =this.goodsService.querySpuById(id);
        if (spu == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spu);
    }

    /**
     * 根据id查询sku
     */
    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId") Long skuId){
        Sku sku = this.goodsService.querySkuBySkuId(skuId);
        if (sku == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);
    }

}
