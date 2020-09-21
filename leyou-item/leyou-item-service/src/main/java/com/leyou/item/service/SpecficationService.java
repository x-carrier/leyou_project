package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecficationService  {
    @Autowired
    private SpecGroupMapper specGroupMaaper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据cid查找参数组
     * @param cid
     * @return
     */
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup recode = new SpecGroup();
        recode.setCid(cid);
        return specGroupMaaper.select(recode);
    }

    /**
     * 根据gid查询规格参数
     * @param gid
     * @return
     */
    public List<SpecParam> quseryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam recode = new SpecParam();
        recode.setGroupId(gid);
        recode.setCid(cid);
        recode.setSearching(searching);
        recode.setGeneric(generic);
        return specParamMapper.select(recode);
    }

    public List<SpecGroup> queryGroupsWithParams(Long cid) {
        //查询组
        List<SpecGroup> groups = this.queryGroupsByCid(cid);
        //遍历组查询到每个组下的规格参数
       groups.forEach(group -> {
           List<SpecParam> params = this.quseryParams(group.getId(), null, null, null);
           group.setParams(params);
       });
        return groups;
    }
}
