package com.xiaohong.repository;

import com.xiaohong.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
