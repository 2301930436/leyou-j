package com.leyou.item.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Service("BrandService")
public class BrandServiceImpl implements BrandService {

    @Resource
    private BrandMapper brandMapper;


    /**
     * 根据查询条件分页并排序查询品牌信息
     *
     * @param key    查询关键字
     * @param page   页数
     * @param rows   每页的行数
     * @param sortBy 根据什么排序
     * @param desc   是否降序
     * @return PageResult<Brand>
     */
    @Override
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        Example example = new Example(Brand.class);
//        内部类对象，可以进行模糊查询等
        Example.Criteria criteria = example.createCriteria();

        if(StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+key+"%");
        }
//        添加分页条件
        PageHelper.startPage(page,rows);
//
        //        添加排序条件
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }

        List<Brand> brands = brandMapper.selectByExample(example);
//        分页轮子
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
////        分页结果集
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());

    }

    /**
     * @param brand 品牌
     * @param cids 分类id列表
     */
    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
//        添加品牌
        this.brandMapper.insertSelective(brand);
//        添加品牌分类中间表
        cids.forEach(cid->{
            this.brandMapper.insertCategoryAndBrand(cid, brand.getId());
        });
    }

    /**
     * 修改品牌
     *
     * @param brand 品牌
     * @param cids  关联分类数据
     */
    @Override
    @Transactional
    public void UpdateBrand(Brand brand, List<Long> cids) {
        //修改品牌
        brandMapper.updateByPrimaryKey(brand);

//        删除该品牌绑定的分类数据
        brandMapper.deleteCategoryAndBrand(brand.getId());
//        遍历添加品牌分类数据
        cids.forEach(cid->{
            this.brandMapper.insertCategoryAndBrand(cid, brand.getId());
        });
    }

    /**
     * 删除品牌信息
     *
     * @param brand 品牌
     */
    @Override
    @Transactional
    public void DeleteBrand(Brand brand) {
//        删除品牌信息
        brandMapper.deleteByPrimaryKey(brand.getId());
//        删除关联表信息
        brandMapper.deleteCategoryAndBrand(brand.getId());
    }

    /**
     * 根据cid分类id查询所有brand品牌
     *
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        return brandMapper.queryBrandByCid(cid);
    }

    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
