package com.leyou.upload.service.UploadServiceImpl;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.upload.service.UploadService;
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

@Service("uploadService")
public class UploadServiceImpl implements UploadService {

//    支持的文件类型
    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg","image/gif","image/png");

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    //存储客户端
    @Autowired
    private FastFileStorageClient storageClient;
    @Override
    public String upload(MultipartFile file) {
//        获取原始文件名
        String originalFilename = file.getOriginalFilename();
//        获取请求头,中文件类型
        String contentType = file.getContentType();
        if(!CONTENT_TYPES.contains(contentType)){
//            不支持的文件类型
            LOGGER.info("文件类型不合法:{}",originalFilename);
            return null;
        }

        try {
//            校验图片内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage ==null){
                //            不支持的文件类型
                LOGGER.info("文件内容不合法:{}",originalFilename);
                return null;
            }
//            图片保存
//            file.transferTo(new File("D:\\JetBrains\\ideaIU-2021.1\\static-file\\image\\"+ originalFilename));

            String fileExtName = StringUtils.substringAfterLast(originalFilename, ".");
            // 上传并保存图片，参数：1-上传的文件流 2-文件的大小 3-文件的后缀 4-可以不管他
            StorePath storePath = this.storageClient.uploadFile(
                    file.getInputStream(), file.getSize(), fileExtName, null);

            return "http://cloud126.sfycname.cn//"+storePath.getFullPath();
        } catch (IOException e) {
            LOGGER.info("文件损坏:{}",originalFilename);
            e.printStackTrace();
        }

        return null;
    }
}
