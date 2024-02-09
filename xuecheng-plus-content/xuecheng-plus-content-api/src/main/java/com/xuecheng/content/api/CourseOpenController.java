package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程Open控制器
 *
 * @author mumu
 * @date 2024/02/03
 */
@RestController
@RequestMapping("/open")
@Tag(name = "课程公开查询接口")
public class CourseOpenController {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser xcUser = SecurityUtil.getUser();
        System.out.println(xcUser.getUsername());
        //获取课程预览信息
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
