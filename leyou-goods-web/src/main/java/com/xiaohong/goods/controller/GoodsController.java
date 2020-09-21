package com.xiaohong.goods.controller;

import com.xiaohong.goods.service.GoodsHtmlService;
import com.xiaohong.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping("item")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @GetMapping("{id}.html")
    public ModelAndView toItemPage(@PathVariable("id") Long id){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("item");
        Map<String, Object> stringObjectMap = this.goodsService.loadData(id);
        modelAndView.addAllObjects(stringObjectMap);
        this.goodsHtmlService.asyncExcute(id);
        return modelAndView;
    }
}
