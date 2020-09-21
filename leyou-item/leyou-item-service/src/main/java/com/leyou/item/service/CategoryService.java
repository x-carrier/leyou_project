package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    /**
     * 根据父节点id查询子节点
     * @param pid
     * @return
     */
    public List<Category> queryCategorysByPid(Long pid) {
        Category recode = new Category();
        recode.setParentId(pid);
        return categoryMapper.select(recode);
    }

    public List<String> queryNamesByIds(List<Long> ids){
        List<Category> categories = categoryMapper.selectByIdList(ids);
        return categories.stream().map(category -> category.getName()).collect(Collectors.toList());
    }
}
