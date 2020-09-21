package com.xiaohong.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xiaohong.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodListener {

    @Autowired
    private SearchService searchService;

    /**
     * 监听保存方法
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.SAVE.QUEUE", durable = "true"),
            exchange = @Exchange(
                    value = "LEYOU.ITEM.EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}))
    public void save(Long id) throws JsonProcessingException {
        if (id == null){
            return;
        }
        this.searchService.save(id);
    }

    /**
     * 监听删除方法
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.DELETE.QUEUE", durable = "true"),
            exchange = @Exchange(
                    value = "LEYOU.ITEM.EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.delete"}))
    public void delete(Long id){
        if (id == null){
            return;
        }
        this.searchService.delete(id);
    }
}
