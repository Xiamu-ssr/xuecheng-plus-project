package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "课程信息编辑接口")
@RestController("content")
public class CourseBaseInfoController {

    @ApiResponse(responseCode = "200", description = "Successfully retrieved user")
    @Operation(summary = "课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(
            @Parameter(description = "分页参数") PageParams params,
            @Parameter(description = "请求具体内容") @RequestBody(required = false) QueryCourseParamsDto dto){

        CourseBase courseBase = new CourseBase();
        courseBase.setCreateDate(LocalDateTime.now());

        return new PageResult<CourseBase>(new ArrayList<CourseBase>(List.of(courseBase)),20, 2, 10);
    }
}
