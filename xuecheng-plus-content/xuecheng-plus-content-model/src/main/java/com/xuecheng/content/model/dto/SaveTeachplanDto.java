package com.xuecheng.content.model.dto;


import com.xuecheng.base.exception.ValidationGroups;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 保存课程计划dto
 * 包括大章节、小章节，新增、修改。
 *
 * @author mumu
 * @date 2024/01/28
 */
@Data
public class SaveTeachplanDto {
    /**
     * 教学计划id
     */
    private Long id;

    /**
     * 课程计划名称
     */
    @NotEmpty(message = "name cannot be empty")
    private String pname;

    /**
     * 课程计划父级Id
     */
    @NotNull(message = "parentId cannot be empty")
    private Long parentid;

    /**
     * 层级，分为1、2、3级
     */
    @Min(value = 1, message = "grade must >= 1")
    @Max(value = 2, message = "grade must <= 2")
    private Integer grade;

    /**
     * 课程类型:1视频、2文档
     */
    private String mediaType;


    /**
     * 课程标识
     */
    @NotNull(message = "courseId cannot be empty")
    private Long courseId;

    /**
     * 课程发布标识
     */
    private Long coursePubId;


    /**
     * 是否支持试学或预览（试看）
     */
    private String isPreview;
}
