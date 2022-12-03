package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    /**
     * 添加分类与品牌的中间表
     * @param cid 分类id
     * @param bid 品牌id
     * @return
     */
    @Insert("INSERT INTO `tb_category_brand`(`category_id`, `brand_id`) VALUES (#{cid}, #{bid})")
    int insertCategoryAndBrand(@Param("cid") Long cid, @Param("bid")Long bid);

    /**
     * 删除分类与品牌中间表
     * @param bid
     * @return
     */
    @Delete("DELETE FROM `tb_category_brand` WHERE `brand_id` = #{bid}")
    int deleteCategoryAndBrand(@Param("bid")Long bid);

//    根据cid查询所有关联的brand
    @Select("SELECT b.* FROM `tb_brand` b INNER JOIN tb_category_brand cb on b.id=cb.brand_id WHERE cb.category_id=#{cid}")
    List<Brand> queryBrandByCid(@Param("cid")Long cid);

}
