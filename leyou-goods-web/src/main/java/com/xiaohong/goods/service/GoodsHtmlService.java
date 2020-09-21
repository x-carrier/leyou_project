package com.xiaohong.goods.service;

import com.xiaohong.goods.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

@Service
public class GoodsHtmlService {
    //注入模板引擎
    @Autowired
    private TemplateEngine  templateEngine;
    @Autowired
    private GoodsService goodsService;

    /**
     * 生成静态页面
     */
    public void createHtml(Long spuId) {
        // 获取页面数据
        Map<String, Object> spuMap = this.goodsService.loadData(spuId);
        //初始化运行上下文
        Context context = new Context();
        //设置数据模板
        context.setVariables(spuMap);
        PrintWriter printWriter = null;
        try {
            //响应到文件本地，io流
            printWriter = new PrintWriter(new File("E:\\IDEA-workspace\\leyou_project\\tools\\nginx-1.19.0\\html\\item\\"+spuId+".html"));
            //吧静态文件生成到服务器本地(文件名根据路径名来去xxx/item/id.html,所以这里名字为item)
            this.templateEngine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (printWriter != null){
                printWriter.close();
            }
        }
    }

    /**
     * 新建线程处理页面静态化
     * @param spuId
     */
    public void asyncExcute(Long spuId) {
        ThreadUtils.execute(()->createHtml(spuId));
        /*ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                createHtml(spuId);
            }
        });*/
    }

    /**
     * 删除静态页面
     * @param id
     */
    public void deleteHtml(Long id) {
        File file = new File("E:\\IDEA-workspace\\leyou_project\\tools\\nginx-1.19.0\\html\\item\\" + id + ".html");
        file.deleteOnExit();
    }
}
