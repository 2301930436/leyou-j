package com.leyou.item.service.Impl;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Service("specService")
public class SpecServiceImpl implements SpecService {
    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private SpecParamMapper specParamMapper;


    /**
     * 根据分类id查询分组
     *
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        return specGroupMapper.select(specGroup);
    }

    /**
     * 根据条件查询规格参数
     *
     * @param gid
     * @param cid
     * @param generic
     * @param searching
     * @return
     */
    @Override
    public List<SpecParam> queryParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        return specParamMapper.select(specParam);
    }

    /**
     * 根据cid查询规格
     *
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> querySpecsByCid(Long cid) {
        List<SpecGroup> specGroups = this.queryGroupsByCid(cid);
//        为规格组添加规格参数
        specGroups.forEach(specGroup -> {
            List<SpecParam> specParams = this.queryParams(specGroup.getId(),null,null,null);
            specGroup.setParams(specParams);
        });
        return specGroups;
    }

    /**
     * 修改组属性
     *
     * @param specGroup
     */
    @Override
    public void updateSpecGroup(SpecGroup specGroup) {
        this.specGroupMapper.updateByPrimaryKeySelective(specGroup);
    }

    /**
     * 修改参数属性
     *
     * @param specParam
     */
    @Override
    public void updateSpecParam(SpecParam specParam) {
        this.specParamMapper.updateByPrimaryKeySelective(specParam);
    }

    /**
     * 通过规格组id删除规格组
     *
     * @param gid
     */
    @Override
    @Transactional
    public void deleteSpecGroup(Long gid) {
        this.specGroupMapper.deleteByPrimaryKey(gid);
        Example example = new Example(SpecParam.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("groupId",gid);
        this.specParamMapper.deleteByExample(example);
    }

    /**
     * 通过规格参数id删除规格组
     *
     * @param pid
     */
    @Override
    public void deleteSpecParam(Long pid) {
        this.specParamMapper.deleteByPrimaryKey(pid);
    }

    @Override
    public void saveSpecGroup(SpecGroup specGroup) {
        this.specGroupMapper.insert(specGroup);
    }

    @Override
    public void saveSpecParam(SpecParam specParam) {
        this.specParamMapper.insert(specParam);
    }

}
