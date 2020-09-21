package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 根据查询条件分页查询品牌信息并排序
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("page")
//    page?key=&page=1&rows=5&sortBy=id&desc=false
    public ResponseEntity<PageResult<Brand>> queryBrandsById(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "desc",required = false) Boolean desc
    ){
        PageResult<Brand> result = brandService.queryBrandsById(key,page,rows,sortBy,desc);
        if (CollectionUtils.isEmpty(result.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

//    当页面发送的数据是一个json数据时，不能用多个对象来接收，只能用一个对象接收，响应json数据要加@RequestBody
//    在Vue里面可以使用qs工具将对象转出
//    qs.stringify可以将json对象分割开单个的请求参数，qs.parse可以将分开的请求参数转换成一个json对象
//    接收json对象需要加注解@RequestBody,接收普通对象不需要加注解

    /**
     * 保存品牌信息
     * @param brand
     * @param cids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids")List<Long> cids){
        this.brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryByCid(@PathVariable("cid") Long cid){
        List<Brand> brands = brandService.queryByCid(cid);
        if (CollectionUtils.isEmpty(brands)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brands);
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id") Long id){
        Brand brand = this.brandService.queryBrandById(id);
        if (brand == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(brand);
    }

}
