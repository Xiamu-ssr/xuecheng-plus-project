package com.xuecheng.content.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@Schema(description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto{

    @NotNull(message = "修改课程名称不能为空")
    private Long id;

}
