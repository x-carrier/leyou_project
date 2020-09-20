package com.xiaohong.leyousearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Spu;
import com.xiaohong.LeyouSearchApplication;
import com.xiaohong.client.GoodsClient;
import com.xiaohong.pojo.Goods;
import com.xiaohong.repository.GoodsRepository;
import com.xiaohong.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = LeyouSearchApplication.class)
@RunWith(SpringRunner.class)
public class ElasticsearchTests {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;



    @Test
    public void test() {
        this.elasticsearchTemplate.createIndex(Goods.class);
        this.elasticsearchTemplate.putMapping(Goods.class);
        //分页查询spu,获取分页结果集
        Integer page = 1;
        Integer rows = 100;

        do {
            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(null, true, page, rows);
            //获取当前页数据
            List<SpuBo> items = result.getItems();
            //处理SpuBo集合成Goods集合
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods((Spu)spuBo);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            //执行新增数据
            this.goodsRepository.saveAll(goodsList);
            rows = items.size();
            page ++;
        }while (rows == 100);


    }

    //基本查询
    @Test
    public void testSearch(){
        //查询所有
        Iterable<Goods> all = this.goodsRepository.findAll();
        all.forEach(item -> {
            System.out.println(item);
        });
    }

}
