package com.xuecheng.content.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="教学计划-媒资绑定提交数据", requiredProperties = {
        "mediaId",
        "fileName",
        "teachplanId"
})
public class BindTeachplanMediaDto {

    @Schema(description = "媒资文件id")
    private String mediaId;

    @Schema(description = "媒资文件名称")
    private String fileName;

    @Schema(description = "课程计划标识")
    private Long teachplanId;


}