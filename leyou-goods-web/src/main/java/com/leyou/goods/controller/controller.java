package com.leyou.goods.controller;

import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Map;

@Controller
@RequestMapping("item")
public class controller {

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsHtmlService goodsHtmlService;
    /***
     * 商品详情页
     * @param model
     * @param id
     * @return
     */
    @GetMapping("{id}.html")
    public String toItemPage(Model model ,@PathVariable("id") Long id){
        Map<String,Object> modelMap = this.goodsService.LoadModel(id);

        model.addAllAttributes(modelMap);
//        生成静态页面
        this.goodsHtmlService.asyncExcute(id);

        return "item";
    }
}
