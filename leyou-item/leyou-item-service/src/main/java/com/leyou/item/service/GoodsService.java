package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequestMapping("spu")
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 根据条件分页查询Spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);

        Example.Criteria criteria = example.createCriteria();
        //添加查询条件
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }


        //添加上下架的过滤条件
        if (saleable == null){
            criteria.andEqualTo("saleable",0);
        }
        //添加分页
        PageHelper.startPage(page,rows);
        //执行查询，获取spu集合
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);
        //转化成SpuBo集合
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu,spuBo);
//            查询品牌名称
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
//            查询分类名称
            List<String> names = categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names,"-"));
            return spuBo;
        }).collect(Collectors.toList());
        //返回PageResult<SpuBo>
        return new PageResult<SpuBo>(spuPageInfo.getTotal(),spuBos);

    }


    @Transactional
    public void saveGoods(SpuBo spuBo) {
        // 防止使用表单提交工具模拟提交表单注入
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        //先新增spu，
        this.spuMapper.insertSelective(spuBo);
        //再新增SpuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);


        spuBo.getSkus().forEach(sku -> {
            // 然后sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);
            // 最后stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);

        });
        sentMsg("insert",spuBo.getId());

    }

    private void sentMsg(String type ,Long id) {
        //发送消息到MQ(加上trycatch,一旦发送失败就能及时看到)
        try {
            this.amqpTemplate.convertAndSend("item."+type,id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据spu的id查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据spu的id查询Sku集合
     * @param spuId
     * @return
     */
    public List<Sku>  querySkuBySpuId(Long spuId) {
        Sku recode = new Sku();
        recode.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(recode);
        //查询库存信息
        skus.forEach(sku -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;
    }


    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //查询要删除的sku，根据spu的id
        Sku recode = new Sku();
        recode.setSpuId(spuBo.getId());
        List<Sku> skus = this.skuMapper.select(recode);

        skus.forEach(sku -> {
            //删除stock
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        });

        //删除sku
        Sku sku_recoude = new Sku();
        sku_recoude.setSpuId(spuBo.getId());
        this.skuMapper.delete(sku_recoude);

        spuBo.getSkus().forEach(sku -> {
            // 新增sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(spuBo.getCreateTime());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);
            // 新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);

        });

        //更新spu和spuDetail
        //不能让客户更新的数据及需要手动更新的数据
        spuBo.setCreateTime(null);  //创建时间不能修改
        spuBo.setLastUpdateTime(new Date());
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sentMsg("update",spuBo.getId());

    }

    /**
     * 根据spuId获取spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据skuId查询sku
     * @param skuId
     * @return
     */
    public Sku querySkuBySkuId(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }
}
