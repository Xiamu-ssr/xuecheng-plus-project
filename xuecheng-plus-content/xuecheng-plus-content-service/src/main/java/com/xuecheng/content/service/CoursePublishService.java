package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import org.springframework.web.bind.annotation.PathVariable;

public interface CoursePublishService {


    /**
     * 获取课程预览信息
     *
     * @param courseId 课程id
     * @return {@link CoursePreviewDto}
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     *
     * @param companyId 公司id
     * @param courseId  课程id
     */
    public void commitAudit(Long companyId, Long courseId);


    /**
     * 课程发布
     *
     * @param courseId 课程id
     */
    public void coursePublish(Long companyId, Long courseId);
}
