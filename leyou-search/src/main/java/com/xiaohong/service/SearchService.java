package com.xiaohong.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.xiaohong.client.BrandClient;
import com.xiaohong.client.CategoryClient;
import com.xiaohong.client.GoodsClient;
import com.xiaohong.client.SpecificationClient;
import com.xiaohong.pojo.Goods;
import com.xiaohong.pojo.SearchRequest;
import com.xiaohong.pojo.SearchResult;
import com.xiaohong.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;

    //json工具
    private static final ObjectMapper MAPPER = new ObjectMapper();


    public Goods buildGoods(Spu spu) throws JsonProcessingException {
        Goods goods = new Goods();

        //根据分类id查询分类名称
        List<String> names = this.categoryClient.queyrNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //根据品牌id查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //根据spuId查询所有的sku
        List<Sku> skus = this.goodsClient.querySkuBySpuId(spu.getId());

        List<Long> prices = new ArrayList<Long>();
        List<Map<String,Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku ->{
            //获取所有sku的价格
            prices.add(sku.getPrice());
            //收集sku的必要字段信息
            Map<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            //图片信息，数据库中图片可能是多张，只要一张就行,就行切割，以，为分隔
            map.put("image",StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            skuMapList.add(map);
        });

        //查询spu中cid3查询出所有的搜索规格参数
        List<SpecParam> params = this.specificationClient.quseryParamsByGid(null, spu.getCid3(), null, true);
        //工具spuId查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        //将通用的规格参数值反序列化成Map
        Map<String, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>(){});
        //将特殊的规格参数值反序列化成Map
        Map<String, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>(){});

        //获取所有搜索规格参数，组成一个map集合
        Map<String, Object> specs = new HashMap<>();
        params.forEach(param ->{
            //判断是否是通用规格参数
            if (param.getGeneric()){
                String value = genericSpecMap.get(param.getId().toString()).toString();
                //判断是否是数数值类型，如果是，返回一个区间字符串
                if (param.getNumeric()){
                    value = chooseSegment(value,param);
                }
                specs.put(param.getName(),value);
            }else {
                List<Object> value = specialSpecMap.get(param.getId().toString());
                specs.put(param.getName(),value);
            }
        });

        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        // 拼接all字符串需要分类名称以及品牌名称
        goods.setAll(spu.getTitle()+" "+ StringUtils.join(names," ") +" "+brand);
        //获取spu下所有sku的价格
        goods.setPrice(prices);
        //获取spu下的所有sku，并转化从json字符
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        //获取所有的查询的规格参数，结构：{name:value}
        goods.setSpecs(specs);
        return goods;
    }


    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    public SearchResult search(SearchRequest request) {
        if (StringUtils.isBlank(request.getKey())){
            return null;
        }
        //自定义查询构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件(匹配查询，求交集)
//        QueryBuilder basicQuery = QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND);
        QueryBuilder basicQuery = buildBoolQueryBuilder(request);
        nativeSearchQueryBuilder.withQuery(basicQuery);
        //添加分页（页码从0开始的）
        nativeSearchQueryBuilder.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
        //添加结果集过滤
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
        //添加分类和品牌的聚合（词条聚合）
        String categoryAggName = "categories";
        String brandAggName = "brands";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //执行查询，获取普通结果集和聚合结果集
        AggregatedPage<Goods> search = (AggregatedPage<Goods>)this.goodsRepository.search(nativeSearchQueryBuilder.build());
        //获取聚合结果集并解析
        List<Map<String,Object>> categories = getCategoryAggResult(search.getAggregation(categoryAggName));
        List<Brand> brands = brandAggResult(search.getAggregation(brandAggName));
        //判断是否是一个分类，只有一个分类才做规格参数的聚合
        List<Map<String,Object>> specs = null;
        if (!CollectionUtils.isEmpty(categories) && categories.size() == 1){
            //对规格参数进行聚合
            specs = getParamAggResult((Long)categories.get(0).get("id"),basicQuery);
        }
        return new SearchResult(search.getTotalElements(),search.getTotalPages(),search.getContent(),categories,brands,specs);
    }

    /**
     * 构建bool查询
     * @param request
     * @return
     */
    private QueryBuilder buildBoolQueryBuilder(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //添加基本的查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
        //添加过滤条件
        //获取过滤信息
        Map<String, Object> filter = request.getFilter();
        //遍历过滤信息
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.equals("品牌",key)){
                key = "brandId";
            }else if (StringUtils.equals("分类",key)){
                key = "cid3";
            }else {
                key = "specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return boolQueryBuilder;
    }

    /**
     * 根据查询条件聚合规格参数
     * @param id  分类id
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getParamAggResult(Long id, QueryBuilder basicQuery) {
        //自定义查询构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件
        nativeSearchQueryBuilder.withQuery(basicQuery);
        //根据id查询要聚合的规格参数
        List<SpecParam> params = specificationClient.quseryParamsByGid(null, id, null, true);
        //添加规格参数的聚合
        params.forEach(param -> {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));
        });
        //添加结果集过滤
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        //执行查询获取聚合查询
        AggregatedPage<Goods> goodsAggregatedPage = (AggregatedPage<Goods>)this.goodsRepository.search(nativeSearchQueryBuilder.build());
        //解析聚合结果集 key-聚合参数名（规格参数名）   value-聚合对象
        Map<String, Aggregation> stringAggregationMap = goodsAggregatedPage.getAggregations().asMap();
        //遍历map集合,返回一个list集合
        List<Map<String, Object>> specs = new ArrayList<>();
        for (Map.Entry<String, Aggregation> entry : stringAggregationMap.entrySet()) {
            //初始化一个map(k:规格参数名 options：聚合的规格参数集)
            Map<String, Object> map = new HashMap<>();
            map.put("k",entry.getKey());
            //解析聚合，获取里面的桶
            StringTerms value = (StringTerms) entry.getValue();
            //获取桶集合
            List<StringTerms.Bucket> buckets = value.getBuckets();
            //遍历桶取出key值放到options中
            List<String> options = new ArrayList<>();
            buckets.forEach(bucket -> {
                options.add(bucket.getKeyAsString());
            });
            map.put("options",options);
            specs.add(map);
        }

        return specs;
    }

    /**
     * 解析品牌的结果集
     * @param aggregation
     * @return
     */
    private List<Brand> brandAggResult(Aggregation aggregation) {
        //强转
        LongTerms terms = (LongTerms) aggregation;
        //获取聚合中的桶并将所有的品牌值组成一个数组
        List<Brand> brands = new ArrayList<>();
        List<LongTerms.Bucket> buckets = terms.getBuckets();
       /* buckets.forEach(bucket -> {
            long key = bucket.getKeyAsNumber().longValue();
            //查询品牌信息
            brands.add(brandClient.queryBrandById(key));
        });*/
        //stream表达式
        return buckets.stream().map(bucket -> {
            return brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
        }).collect(Collectors.toList());

    }

    /**
     * 解析分类的结果集
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        //强转
        LongTerms terms = (LongTerms) aggregation;
        //获取桶集合转换成map集合
       return terms.getBuckets().stream().map(category ->{
           //初始化map
           Map<String,Object> map = new HashMap<>();
           //获取桶中的分类id
           long id = category.getKeyAsNumber().longValue();
           //根据分类id查询分类名称
           List<String> names = this.categoryClient.queyrNamesByIds(Arrays.asList(id));
           //封装成map并返回
           map.put("id",id);
           map.put("name",names.get(0));
           return map;
       }).collect(Collectors.toList());
    }

    /**
     * 保存商品
     * @param id
     */
    public void save(Long id) throws JsonProcessingException {
        Spu spu = this.goodsClient.querySpuById(id);
        Goods goods = this.buildGoods(spu);
        this.goodsRepository.save(goods);
    }

    /**
     * 删除商品
     * @param id
     */
    public void delete(Long id) {
        this.goodsRepository.deleteById(id);
    }
}
