package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点id查询子节点
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategorysByPid(@RequestParam(value = "pid",defaultValue = "0") Long pid) {
        if (pid == null || pid < 0) {
            //400参数不合法
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            return ResponseEntity.badRequest().build();
        }
        List<Category> categorys = this.categoryService.queryCategorysByPid(pid);
        if (CollectionUtils.isEmpty(categorys)) {
            //404资源服务器未找到
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        //200查询成功
        return ResponseEntity.ok(categorys);
    }

    /**
     * 根据分类id的集合查询分类名称
     * @param ids
     * @return
     */
    @GetMapping
    public ResponseEntity<List<String>> queyrNamesByIds(@RequestParam("ids") List<Long> ids){
        List<String> names = this.categoryService.queryNamesByIds(ids);
        if (CollectionUtils.isEmpty(names)) {
            //404资源服务器未找到
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        //200查询成功
        return ResponseEntity.ok(names);
    }

}
