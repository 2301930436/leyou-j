package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import java.util.List;

public interface BrandService {

    /**
     * 根据查询条件分页并排序查询品牌信息
     *
     * @param key 查询关键字
     * @param page 页数
     * @param rows 每页的行数
     * @param sortBy 根据什么排序
     * @param desc 是否降序
     * @return PageResult<Brand>
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc);

    /**
     *保存品牌
     */
    void saveBrand(Brand brand, List<Long> cids);

    /**
     * 修改品牌
     * @param brand 品牌
     * @param cids 关联分类数据
     */
    void UpdateBrand(Brand brand, List<Long> cids);

    /**
     * 删除品牌信息
     * @param brand 品牌
     */
    public void DeleteBrand(Brand brand);

    /**
     * 根据cid分类id查询所有brand品牌
     * @param cid
     * @return
     */
    List<Brand> queryBrandByCid(Long cid);

    Brand queryBrandById(Long id);
}
