package com.leyou.goods.service;
import com.leyou.goods.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

@Service
public class GoodsHtmlService {

    @Resource
    private GoodsService goodsService;

    @Resource
    private TemplateEngine templateEngine;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsHtmlService.class);

    /**
     * 生成spu页面
     * @param spuId
     */
    public void createHtml(Long spuId){

        PrintWriter printWriter = null;
        try {
            Map<String,Object> spuMap = this.goodsService.LoadModel(spuId);
//            上下文
            Context context = new Context();
            context.setVariables(spuMap);
//            模板引擎处理文件
            File file = new File("D:\\JetBrains\\ideaIU-2021.1\\tools\\nginx-1.14.0\\html\\item\\"+spuId+".html");
            printWriter = new PrintWriter(file);
            templateEngine.process("item",context,printWriter);

        }catch (Exception e){
            LOGGER.error("页面静态化出错,{},"+e,spuId);
        }finally {
            if(printWriter!=null){
                printWriter.close();
            }
        }
    }
public void asyncExcute(Long spuId){
    ThreadUtils.execute(()->createHtml(spuId));
//    ThreadUtils.execute(new Runnable() {
//        @Override
//        public void run() {
//            createHtml(spuId);
//        }
//    });
}


    public void deleteHtml(Long id) {
        File file = new File("D:\\JetBrains\\ideaIU-2021.1\\tools\\nginx-1.14.0\\html\\item\\"+id+".html");
        file.deleteOnExit();
    }
}
