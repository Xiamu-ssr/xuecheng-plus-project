package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;

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

    /**
     * @description 课程静态化
     * @param courseId  课程id
     * @return File 静态化文件
     */
    public File generateCourseHtml(Long courseId);

    /**
     * 上传课程html到Minio
     *
     * @param file     静态化文件
     * @param courseId 课程id
     * @description 上传课程静态化页面
     */
    public void  uploadCourseHtml(Long courseId,File file);


    /**
     * 根据courseId获取课程发布表数据
     *
     * @param courseId 课程id
     * @return {@link CoursePublish}
     */
    public CoursePublish getCoursePublish(Long courseId);
}
