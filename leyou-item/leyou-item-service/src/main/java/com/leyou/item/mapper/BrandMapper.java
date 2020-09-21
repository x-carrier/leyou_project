package com.leyou.item.mapper;


import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface BrandMapper extends Mapper<Brand> {
    /**
     * 自定义添加CategoryAndBrand中间表
     * @param bid
     * @param cid
     */
//    有多个参数时，要写@Param注解来说明参数对应的传进来的参数
    @Insert("INSERT into tb_category_brand(category_id,brand_id) VALUES(#{cid},#{bid})")
    void insertCategoryAndBrand(@Param("bid") Long bid, @Param("cid") Long cid);

    /**
     * 根据分类id查找品牌
     * @param cid
     * @return
     */
    @Select("select * from tb_brand a INNER JOIN tb_category_brand b on a.id = b.brand_id where b.category_id = #{cid}")
    List<Brand> selectBrandsByCid(Long cid);
}
