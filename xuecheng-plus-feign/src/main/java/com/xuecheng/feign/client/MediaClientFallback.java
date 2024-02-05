package com.xuecheng.feign.client;

import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class MediaClientFallback implements MediaClient{
    @Override
    public UploadFileResultDto upload(MultipartFile file, String objectName) {
        log.error("远程调用发送熔断，");
        System.out.println("远程调用发送熔断，");
        UploadFileResultDto dto = new UploadFileResultDto();
        dto.setFilename("\"远程调用发送熔断，\"");
        return dto;
    }
}
