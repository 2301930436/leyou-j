package com.leyou.item.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.pojo.Stock;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("goodsService")
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryService categoryService;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsServiceImpl.class);

    @Resource
    private AmqpTemplate amqpTemplate;
    /**
     * 查询Spu分页数据
     *
     * @param key      xx
     * @param saleable xx
     * @param page     xx
     * @param rows      xx
     * @return xx
     */
    @Override
    public PageResult<SpuBo> querySpuBoByPage(String key, Integer saleable, Integer page, Integer rows) {

        //        数据库查询对象
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

//        模糊搜索
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
//        条件搜索
        if (saleable!=2){
            criteria.andEqualTo("saleable",saleable);
        }


//        添加分页条件
        PageHelper.startPage(page,rows);
        //        分页轮子

        List<Spu> spus = this.spuMapper.selectByExample(example);


        PageInfo<Spu> pageInfo = new PageInfo<>(spus);


        List<SpuBo> spuBos = new ArrayList<>();
        spus.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
//            复制字段到新对象source
            BeanUtils.copyProperties(spu,spuBo);
//            设置品牌
            spuBo.setBname(brandMapper.selectByPrimaryKey(spuBo.getBrandId()).getName());
//            设置分类
            List<String> cnames = categoryService.queryNamesByIds(Arrays.asList(spuBo.getCid1(),spuBo.getCid2(),spuBo.getCid3()));
            spuBo.setCname(StringUtils.join(cnames,"/"));
            spuBos.add(spuBo);
        });

//        分页结果集
        return new PageResult<>(pageInfo.getTotal(), spuBos);
    }

    /**
     * 保存spuBo的信息 包括商品集与商品信息
     *
     * @param spuBo spu扩展类
     */
    @Override
    @Transactional
    public void saveGoods(SpuBo spuBo) {
//        ******************处理spu表
//        处理默认参数
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
//        选择性插入，进行保存
        this.spuMapper.insertSelective(spuBo);

//        ********************处理spuDetail表
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insert(spuDetail);
//        ********************处理sku与stock表
        this.saveSkuAndStock(spuBo);
//        spuBo.getSkus().forEach(sku -> {
////            处理sku
//            sku.setSpuId(spuBo.getId());
//            sku.setCreateTime(new Date());
//            sku.setLastUpdateTime(sku.getCreateTime());
//            skuMapper.insert(sku);
//
////            stock库存表处理
//            Stock stock = new Stock();
//            stock.setSkuId(sku.getId());
//            stock.setStock(sku.getStock());
//            stockMapper.insert(stock);
//        });

    }
    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku -> {
            // 新增sku
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            // 新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
        this.sendMessage(spuBo.getId(),"insert");
    }

    /**
     * 根据spuId查询spu详情
     *
     * @param spuId
     * @return
     */
    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据spuId查询skus
     *
     * @param spuId
     * @return
     */
    @Override
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        skus.forEach(sku1 -> {
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            sku1.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 修改spuBo
     *
     * @param spuBo
     */
    @Override
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        List<Sku> skus = this.querySkusBySpuId(spuBo.getId());
//        sku如果存在则先删除
        if(!CollectionUtils.isEmpty(skus)){
//            提取所有sku的id
            List<Long> skuIds = skus.stream().map(Sku::getId).collect(Collectors.toList());
//            根据skuId对库存表进行删除
            Example example = new Example(Stock.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("skuId",skuIds);
            this.stockMapper.deleteByExample(example);

//            删除与spu关联的所有sku
            Sku sku = new Sku();
            sku.setSpuId(spuBo.getId());
            this.skuMapper.delete(sku);

//            保存新的商品信息
            this.saveSkuAndStock(spuBo);
//            更新商品信息
            spuBo.setLastUpdateTime(new Date());
            spuBo.setSaleable(null);
            spuBo.setValid(null);
            spuBo.setCreateTime(null);
            this.spuMapper.updateByPrimaryKeySelective(spuBo);

//            更新商品详细信息表
            this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());



            this.sendMessage(spuBo.getId(),"update");
        }

    }

    @Override
    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    @Override
    public void sendMessage(Long id,String type) {

        try{
            this.amqpTemplate.convertAndSend("item."+type,id);

        }catch (Exception e){
            e.printStackTrace();
            LOGGER.error("出错啦item."+type+":"+id);
        }

    }

    @Override
    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }


}
