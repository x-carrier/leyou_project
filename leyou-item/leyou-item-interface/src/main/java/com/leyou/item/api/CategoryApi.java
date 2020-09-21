package com.leyou.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("category")
public interface CategoryApi {
    /**
     * 根据分类id的集合查询分类名称
     * @param ids
     * @return
     */
    @GetMapping
    public List<String> queyrNamesByIds(@RequestParam("ids") List<Long> ids);

}
