package com.xuecheng.content.config;

import com.xuecheng.base.exception.XueChengPlusException;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * @author mumu
 * @version 1.0
 * @description
 * @date 2022/10/15 22:13
 */
//@Configuration
//public class MultipartSupportConfig {
//
//    @Autowired
//    private ObjectFactory<HttpMessageConverters> messageConverters;
//
//    @Bean
//    @Primary//注入相同类型的bean时优先使用
//    @Scope("prototype")
//    public Encoder feignEncoder() {
//        return new SpringFormEncoder(new SpringEncoder(messageConverters));
//    }
//
//    //将file转为Multipart
//    public static MultipartFile getMultipartFile(File file) {
//        try {
//            byte[] content = Files.readAllBytes(file.toPath());
//            MultipartFile multipartFile = new MockMultipartFile(file.getName(),
//                    file.getName(), Files.probeContentType(file.toPath()), content);
//            return multipartFile;
//        } catch (IOException e) {
//            e.printStackTrace();
//            XueChengPlusException.cast("File->MultipartFile转化失败");
//            return null;
//        }
//    }
//}
