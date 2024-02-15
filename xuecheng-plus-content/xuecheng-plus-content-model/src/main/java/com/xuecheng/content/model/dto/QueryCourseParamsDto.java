package com.xuecheng.content.model.dto;

import com.xuecheng.base.model.PageParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询课程参数dto
 *
 * @author mumu
 * @date 2024/01/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryCourseParamsDto {

    /**
     * 审核状态
     */
    private String auditStatus;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 发布状态
     */
    private String publishStatus;

}
