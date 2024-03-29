package com.xuecheng.search.controller;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.search.po.CourseIndex;
import com.xuecheng.search.service.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * @author mumu
 * @version 1.0
 * @description 课程索引接口
 * @date 2022/9/24 22:31
 */
@Tag(name = "课程信息索引接口", description = "课程信息索引接口")
@RestController
@RequestMapping("/index")
public class CourseIndexController {

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;

    @Autowired
    IndexService indexService;

    @Operation(description = "添加课程索引")
    @PostMapping("course")
    public Boolean add(@RequestBody CourseIndex courseIndex) {

        Long id = courseIndex.getId();
        if(id==null){
            XueChengPlusException.cast("课程id为空");
        }
        Boolean result = indexService.addCourseIndex(courseIndexStore, String.valueOf(id), courseIndex);
        if(!result){
            XueChengPlusException.cast("添加课程索引失败");
        }
        return result;
    }

    @Operation(description = "删除课程索引")
    @DeleteMapping("course/{id}")
    public Boolean delete(@PathVariable String id){
        return indexService.deleteCourseIndex(courseIndexStore, id);
    }

}
