package com.xiaohong.goods.service;

import com.leyou.item.pojo.*;
import com.xiaohong.goods.client.BrandClient;
import com.xiaohong.goods.client.CategoryClient;
import com.xiaohong.goods.client.GoodsClient;
import com.xiaohong.goods.client.SpecificationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;

    /**
     * 返回页面所需所有信息
     * @param spuId
     * @return
     */
    public Map<String,Object> loadData(Long spuId){
        Map<String,Object> model = new HashMap<>();
        //根据SpuId查询spu
        Spu spu = this.goodsClient.querySpuById(spuId);
        //根据SpuId查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spuId);
        //根据SpuId查询分类：Map<String,Object>
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queyrNamesByIds(cids);
        List<Map<String,Object>> categories = new ArrayList<>();
        for (int i = 0; i < cids.size(); i++) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",cids.get(i));
            map.put("name",names.get(i));
            categories.add(map);
        }
        //根据SpuId查询查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        //根据SpuId查询sku集合
        List<Sku> skus = this.goodsClient.querySkuBySpuId(spuId);
        //根据SpuId查询规格参数组
        List<SpecGroup> groups = this.specificationClient.queryGroupsWithParams(spu.getCid3());
        //根据SpuId查询特殊规格参数
        List<SpecParam> params = this.specificationClient.quseryParamsByGid(null, spu.getCid3(), false, null);
        //初始化特殊规格参数的map
        Map<Long,String> paramMap = new HashMap<>();
        params.forEach(param -> {
            paramMap.put(param.getId(),param.getName());
        });

        model.put("spu",spu);
        model.put("spuDetail",spuDetail);
        model.put("categories",categories);
        model.put("brand",brand);
        model.put("skus",skus);
        model.put("groups",groups);
        model.put("paramMap",paramMap);

        return model;
    }

}
