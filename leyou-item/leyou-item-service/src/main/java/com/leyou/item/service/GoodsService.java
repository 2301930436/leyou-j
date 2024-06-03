package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GoodsService {
    /**
     * 查询Spu分页数据
     *
     * @param key      xx
     * @param saleable xx
     * @param page     xx
     * @param rows     xx
     * @return xx
     */
    PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows);

    /**
     * 保存spuBo的信息 包括商品集与商品信息
     * @param spuBo
     */
    void saveGoods(SpuBo spuBo);

    /**
     * 根据spuId查询spu详情
     * @param spuId
     * @return
     */
    SpuDetail querySpuDetailBySpuId(Long spuId);

    /**
     * 根据spuId查询skus
     * @param spuId
     * @return
     */
    List<Sku> querySkusBySpuId(Long spuId);

    /**
     * 修改spuBo
     * @param spuBo
     */
    void updateGoods(SpuBo spuBo);

    Spu querySpuById(Long id);

    void sendMessage(Long id,String type);

    Sku querySkuById(Long id);
}