package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    BrandService brandService;

    /**
     * 品牌分页数据
     * @param key 关键字
     * @param page 页码
     * @param rows 每页的行数
     * @param sortBy 分类
     * @param desc 排序
     * @return xxx
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandsByPage(
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy", required = false)String sortBy,
            @RequestParam(value = "desc", required = false)Boolean desc
    ){
        PageResult<Brand> brandPageResult = brandService.queryBrandsByPage(key, page, rows, sortBy, desc);
//           此处判空返回404回导致前端报错
//        if(CollectionUtils.isEmpty(brandPageResult.getItems())){
//            return ResponseEntity.notFound().build();
//        }
        return ResponseEntity.ok(brandPageResult);
    }

    /**
     * 新增品牌
     * @return 状态
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand,@RequestParam("cids") List<Long> cids){

        this.brandService.saveBrand(brand,cids);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改品牌信息
     * @param brand
     * @param cids
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> UpdateBrand(Brand brand,@RequestParam("cids") List<Long> cids){

        this.brandService.UpdateBrand(brand,cids);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除品牌信息
     * @param brand
     * @return
     */
    @DeleteMapping
    public ResponseEntity<Void> DeleteBrand(@RequestBody Brand brand){
        this.brandService.DeleteBrand(brand);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据cid获取所关联的品牌
     * @param cid
     * @return
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable("cid") Long cid){
        List<Brand> brands = this.brandService.queryBrandByCid(cid);
//        if(CollectionUtils.isEmpty(brands)){
////            集合为空
//            return ResponseEntity.notFound().build();
//        }
        return ResponseEntity.ok(brands);
    }

    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id") Long id){
        Brand brand = this.brandService.queryBrandById(id);
        return ResponseEntity.ok(brand);
    }
}
