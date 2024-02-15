package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import com.xuecheng.feign.client.MediaClient;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "课程基本信息接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    MediaClient mediaClient;


//    @PreAuthorize("hasAuthority('read')")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user")
    @Operation(summary = "查询课程信息列表")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(
            PageParams pageParams,
            @Parameter(description = "请求具体内容") @RequestBody(required = false) QueryCourseParamsDto dto){
        SecurityUtil.XcUser xcUser = SecurityUtil.getUser();
        if (ObjectUtils.isEmpty(xcUser)){
            XueChengPlusException.cast("JWT异常");
        }
        Long companyId = null;
        if (StringUtils.isNotEmpty(xcUser.getCompanyId())){
            companyId = Long.valueOf(xcUser.getCompanyId());
        }
        return courseBaseInfoService.queryCourseBaseList(companyId, pageParams, dto);
    }

    @Operation(summary = "新增课程基础信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto dto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId ,dto);
    }

    @Operation(description = "根据课程id查询课程信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
//        SecurityUtil.XcUser xcUser = SecurityUtil.getUser();
//        System.out.println(xcUser.getUsername());
//        System.out.println(xcUser.toString());
        return courseBaseInfoService.getCourseBaseById(courseId);
    }

    @Operation(description = "修改课程信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto dto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.modifyCourseBase(companyId, dto);
    }

}
