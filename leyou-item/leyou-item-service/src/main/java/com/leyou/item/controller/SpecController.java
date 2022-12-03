package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecController {

    @Resource
    private SpecService specService;

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "generic",required = false) Boolean generic,
            @RequestParam(value = "searching",required = false) Boolean searching
    ){
        List<SpecParam> specParams = specService.queryParams(gid,cid,generic,searching);
        return ResponseEntity.ok(specParams);
    }
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> specGroups = this.specService.queryGroupsByCid(cid);
        return ResponseEntity.ok(specGroups);
    }


    /**
     * 修改规格组属性
     * @param specGroup
     * @return
     */
    @PutMapping("group")
    public ResponseEntity<Void> updateSpecGroup(@RequestBody SpecGroup specGroup){
        this.specService.updateSpecGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改单个规格参数属性
     * @param specParam
     * @return
     */
    @PutMapping("param")
    public ResponseEntity<Void> updateSpecGroup(@RequestBody SpecParam specParam){
        this.specService.updateSpecParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @DeleteMapping("group/{gid}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable("gid")Long gid){
        this.specService.deleteSpecGroup(gid);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("param/{pid}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("pid")Long pid){
        this.specService.deleteSpecParam(pid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecGroup specGroup){
        this.specService.saveSpecGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping("param")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecParam specParam){
        this.specService.saveSpecParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecsByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> specGroups = this.specService.querySpecsByCid(cid);
        if(specGroups==null||specGroups.size()==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specGroups);
    }


}
