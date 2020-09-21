package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

//    上传文件类型白名单 不同文件对应的content-type查看网址：https://tool.oschina.net/commons/
    private static final List<String> CONTENT_TYPES = Arrays.asList("application/x-jpg","image/jpeg","image/gif");

//    记录日志信息，方便管理员查看是哪个文件上传错误
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient storageClient;

    /**
     * 图片上传
     * @param file
     * @return
     */
    public String uploadImage(MultipartFile file){
//        获取原始文件名
        String originalFilename = file.getOriginalFilename();
//        获取最后一个点后面的内容
//        StringUtils.substringAfterLast(originalFilename,".");
//        或者根据头信息里的content-Type类别来判断上传文件的类型
        String contentType = file.getContentType();
//        校验文件类型
        if (!CONTENT_TYPES.contains(contentType)){
            //文件类型非法
//            输出logger日志
//            LOGGER.info("文件类型不合法："+originalFilename);
//            适用于多个参数，{}是占位符
            LOGGER.info("文件类型不合法:{}",originalFilename);
            return null;
        }
        try {
//        校验文件内容
            BufferedImage read = ImageIO.read(file.getInputStream());
//        只有图片才有高和宽属性
            if (read == null || read.getWidth() == 0 || read.getHeight() == 0){
                LOGGER.info("文件内容不合法:{}",originalFilename);
                return null;
            }
//        保存到文件服务器
//            file.transferTo(new File("E:\\IDEA-workspace\\leyou_projiect\\image\\"+originalFilename));
            String ext = StringUtils.substringAfterLast(originalFilename,".");
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);

//        返回url进行回显
//            return "http://image.xiaohong.com/"+originalFilename;
            return "http://image.leyou.com/"+storePath.getFullPath();
        } catch (IOException e) {
            LOGGER.info("服务器内部异常:"+originalFilename);
            e.printStackTrace();
        }
        return null;
    }

}
