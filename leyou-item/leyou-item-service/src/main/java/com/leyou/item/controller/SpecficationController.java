package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.SpecficationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecficationController {

    @Autowired
    private SpecficationService specficationService;

    @Autowired
    private BrandService brandService;

    /**
     * 根据cid查找参数组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> groups = this.specficationService.queryGroupsByCid(cid);
        if (CollectionUtils.isEmpty(groups)) {
            ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(groups);
    }

    /**
     * 根据gid查询规格参数
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> quseryParamsByGid(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "generic",required = false) Boolean generic,
            @RequestParam(value = "searching",required = false) Boolean searching
    ){
        List<SpecParam> params = this.specficationService.quseryParams(gid,cid,generic,searching);

        if (CollectionUtils.isEmpty(params)) {
            ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(params);
    }

    /**
     * 根据cid查找参数组及对应的规格参数
     * @param cid
     * @return
     */
    @GetMapping("groups/param/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsWithParams(@PathVariable("cid") Long cid){
        List<SpecGroup> groups = this.specficationService.queryGroupsWithParams(cid);
        if (CollectionUtils.isEmpty(groups)) {
            ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(groups);
    }

}
