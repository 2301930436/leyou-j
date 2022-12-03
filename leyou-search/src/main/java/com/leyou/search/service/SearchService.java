package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.reponsitory.GoodsRepository;
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
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Service
public class SearchService {
//微服务api
    @Resource
    private BrandClient brandClient;
    @Resource
    private CategoryClient categoryClient;
    @Resource
    private GoodsClient goodsClient;
    @Resource
    private SpecificationClient specificationClient;
//存储库
    @Resource
    private GoodsRepository goodsRepository;

//    格式化工具
    private static final ObjectMapper MAPPER = new ObjectMapper();

//    商品集构建
    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();
//        品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
//        三级分类
        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList((spu.getCid1()), spu.getCid2(), spu.getCid3()));
//        商品集下的商品列表
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        List<Long> prices = new ArrayList<>();
        List<Map<String,Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
//            处理sku属性
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            skuMap.put("image", StringUtils.isNotBlank(sku.getImages())?StringUtils.split(sku.getImages(),","):"");
            skuMapList.add(skuMap);
        });
//        可被搜索的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null,spu.getCid3(),null,true);
//        规格值
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
//        通用规格参数 json数据转化为java对象
        Map<Long,Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(),new TypeReference<Map<Long,Object>>(){});
//        特殊规格参数
        Map<Long,List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(),new TypeReference<Map<Long,List<Object>>>(){});
//        处理规格参数值
        Map<String,Object> paramMap = new HashMap<>();
        params.forEach(param->{
            if(param.getGeneric()){
//                通用规格参数的处理
                String value = genericSpecMap.get(param.getId())==null?"0":genericSpecMap.get(param.getId()).toString();
                if (param.getNumeric()){
//                    对数值类型的参数处理
                    value = chooseSegment(value,param);
                }
                paramMap.put(param.getName(),value);
            }else {
//                处理非通用规格参数
                paramMap.put(param.getName(), specialSpecMap.get(param.getId()));
            }
        });
//        参数赋值
        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        goods.setAll(spu.getTitle() + brand.getName() + StringUtils.join(names," "));
        goods.setPrice(prices);
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        goods.setSpecs(paramMap);

        return goods;
    }
    private String chooseSegment(String value,SpecParam specParam){
        Double val = NumberUtils.toDouble(value);
//        默认区间为其他
        String result = "其他";
        for (String segment : specParam.getSegments().split(",")) {
//            遍历每个区间
            String[] beginEnd = segment.split("-");
            Double begin = NumberUtils.toDouble(beginEnd[0]);
            Double end = beginEnd.length==2?NumberUtils.toDouble(beginEnd[1]):Double.MAX_VALUE;
            if (val >= begin && val < end) {
//                在此范围内
                if(beginEnd.length==1){
//                    ......,a<=val(最大区间)
                    result = beginEnd[0]+specParam.getUnit()+"以上";
                }else if(begin==0){
//                    0<=val<a,......(最小区间)
                    result = beginEnd[1]+specParam.getUnit()+"以下";
                }else {
//                    ......,a<=val<b,......(中间区间)
                    result = segment+specParam.getUnit();
                }
                break;
            }
        }


        return result;
    }

    public PageResult<Goods> search(SearchRequest searchRequest){
        if(StringUtils.isBlank(searchRequest.getKey())){
            return null;
        }
//        查询对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();


//        对key进行全文检索查询
        QueryBuilder boolQueryBuilder = buildBoolQueryBuilder(searchRequest);
        queryBuilder.withQuery(boolQueryBuilder);


//        同时满足所有分词
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));
//        过滤返回字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
//        添加分页
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1, searchRequest.getSize()));
//        排序
        String sortBy = searchRequest.getSortBy();
        Boolean descending = searchRequest.getDescending();
        if(StringUtils.isNotBlank(sortBy)){
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(descending? SortOrder.DESC:SortOrder.ASC));
        }

//        根据品牌和分类进行聚合
        String categoryAggName = "categories";
        String brandAggName="brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

//        聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        List<Map<String,Object>> categories = getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
        List<Brand> brands =  getBrandAggResult(goodsPage.getAggregation(brandAggName));

//        分类聚合结果唯一，进行规格参数的处理
        List<Map<String,Object>> specs = null;
        if(categories.size()==1){
            specs = getParamAggResult((Long)categories.get(0).get("id"),boolQueryBuilder);
        }

        return new SearchResult(goodsPage.getTotalElements(), goodsPage.getTotalPages(), goodsPage.getContent(), categories, brands,specs);
    }
//    解析分类聚合结果集
    private List<Map<String,Object>> getCategoryAggResult(Aggregation aggregation){
        LongTerms terms= (LongTerms) aggregation;
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        List<Map<String,Object>> categories = new ArrayList<>();
        List<Long> cids = new ArrayList<>();
        buckets.forEach(bucket -> {
            Long cid = bucket.getKeyAsNumber().longValue();
            cids.add(cid);
        });
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        for (int i = 0; i < cids.size(); i++) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",cids.get(i));
            map.put("name",names.get(i));
            categories.add(map);
        }
        return categories;
    }
//    解析品牌聚合结果
    private List<Brand> getBrandAggResult(Aggregation aggregation){
        LongTerms terms= (LongTerms) aggregation;
//        获取所有桶
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        List<Brand> brands = new ArrayList<>();
//        遍历所有brandId，获取Brand
        buckets.forEach(bucket -> {
            Brand brand = this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
            brands.add(brand);
        });
        return brands;
    }
//    解析规格参数聚合结果
    private List<Map<String,Object>> getParamAggResult(Long id,QueryBuilder basicBuilder){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//        基于基本的查询条件进行查询
        queryBuilder.withQuery(basicBuilder);
//        查询需要根据什么参数进行聚合
        List<SpecParam> params = this.specificationClient.queryParams(null,id,null,true);
        params.forEach(param->{
//             添加每一个规格参数为聚合条件
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));
        });
//        过滤结果集，只需要聚合
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());
//聚合结果集
        List<Map<String,Object>> paramMapList = new ArrayList<>();
//        聚合转换为map
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String, Aggregation> entry:aggregationMap.entrySet()){
            Map<String,Object> map = new HashMap<>();
            map.put("k",entry.getKey());
            List<Object> options = new ArrayList<>();
            StringTerms terms = (StringTerms) entry.getValue();
            terms.getBuckets().forEach(bucket -> {
                options.add(bucket.getKeyAsString());
            });
            map.put("options",options);
            paramMapList.add(map);
        }
        return paramMapList;

    }

    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequest request){
        System.out.println(request);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        搜索蓝查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()).operator(Operator.AND));
//        过滤条件查询
        if(CollectionUtils.isEmpty(request.getFilter())){
            return boolQueryBuilder;
        }
//        遍历过滤条件
        for (Map.Entry<String,String> entry:request.getFilter().entrySet()){
            String key = entry.getKey();
//            如果是品牌与分类则过滤对应的id,否则就过滤对应规格参数字段
            if(StringUtils.equals("品牌",key)){
                key="brandId";
            }else if(StringUtils.equals("分类",key)){
                key="cid3";
            }else {
                key="specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return boolQueryBuilder;
    }

    public void createIndex(Long id) throws IOException {
       Spu spu = this.goodsClient.querySpuById(id);

       Goods goods = this.buildGoods(spu);

       this.goodsRepository.save(goods);
    }

    public void deleteIndex(Long id){
        this.goodsRepository.deleteById(id);
    }
}











