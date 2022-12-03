package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecService {

    /**
     * 根据分类id查询分组
     * @param cid
     * @return
     */
    List<SpecGroup> queryGroupsByCid(Long cid);
    /**
     * 根据条件查询规格参数
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    List<SpecParam> queryParams(Long gid,Long cid,Boolean generic,Boolean searching);

    /**
     *  根据cid查询规格
     * @param cid
     * @return
     */
    List<SpecGroup>querySpecsByCid(Long cid);

    /**
     * 修改组属性
     * @param specGroup
     */
    void updateSpecGroup(SpecGroup specGroup);

    /**
     * 修改参数属性
     * @param specParam
     */
    void updateSpecParam(SpecParam specParam);

    /**
     * 通过规格组id删除规格组
     * @param gid
     */
    void deleteSpecGroup(Long gid);
    /**
     * 通过规格参数id删除规格组
     * @param pid
     */
    void deleteSpecParam(Long pid);

    void saveSpecGroup(SpecGroup specGroup);
    void saveSpecParam(SpecParam specParam);

}
