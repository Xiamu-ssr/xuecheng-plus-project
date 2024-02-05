package com.xuecheng.feign.client;

import com.xuecheng.media.model.dto.UploadFileResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "media-api", path = "/media", fallbackFactory = MediaClientFallbackFactory.class)
public interface MediaClient {

    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(
            @RequestPart("filedata") MultipartFile file,
            @RequestParam(value = "objectName", required = false) String objectName
    );
}

