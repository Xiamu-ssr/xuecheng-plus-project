package com.xuecheng.feign.client;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "media-api", path = "/media", fallbackFactory = MediaClientFallbackFactory.class)
public interface MediaClient {

    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(
            @RequestPart("filedata") MultipartFile file,
            @RequestParam(value = "objectName", required = false) String objectName
    );

    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId);

    @DeleteMapping("/delete/staticHtml/{courseId}")
    public boolean deleteStaticHtml4Minio(@PathVariable Long courseId);
}

