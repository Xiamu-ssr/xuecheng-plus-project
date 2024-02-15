package com.xuecheng.feign.client;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class MediaClientFallbackFactory implements FallbackFactory<MediaClient> {
    @Override
    public MediaClient create(Throwable cause) {
        log.error("远程调用发送熔断,:{}，", cause.toString());
        return new MediaClient() {
            @Override
            public UploadFileResultDto upload(MultipartFile file, String objectName) {
                log.error("远程调用发送熔断,:{}，", cause.toString());
                System.out.println("远程调用发送熔断,");
                return null;
            }

            @Override
            public RestResponse<String> getPlayUrlByMediaId(String mediaId) {
                log.error("远程调用发送熔断,:{}，", cause.toString());
                System.out.println("远程调用发送熔断,");
                return null;
            }
        };
    }
}
