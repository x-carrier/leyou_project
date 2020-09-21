package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    /**
     * 根据查询条件分页查询品牌信息并排序
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsById(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
//        初始化Example对象
        Example example = new Example(Brand.class);
//        为Example对象添加查询条件
        Example.Criteria criteria = example.createCriteria();
//        根据name模糊查询，或者根据首字母模糊查询
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }
//        添加分页条件
        PageHelper.startPage(page,rows);
//        添加排序条件
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+" "+(desc ? "desc" : "asc"));
        }
        List<Brand> brands = brandMapper.selectByExample(example);
//        包装成PageInfo对象
        PageInfo<Brand> brandPageInfo = new PageInfo<>(brands);
//        包装成分页结果集返回
        return new PageResult<Brand>(brandPageInfo.getTotal(),brandPageInfo.getList());
    }


    /**
     * 保存品牌信息
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
//        insertSelective根据传入的参数来编写sql语句（效率更高），insert如果有参数没有数据则值为空
//        先增加brand表
        this.brandMapper.insertSelective(brand);
//        再添加中间表
        cids.forEach(cid ->{
            this.brandMapper.insertCategoryAndBrand(brand.getId(),cid);
        });
    }

    /**
     * 根据cid查询品牌
     * @param cid
     * @return
     */
    public List<Brand> queryByCid(Long cid) {
        return this.brandMapper.selectBrandsByCid(cid);
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
