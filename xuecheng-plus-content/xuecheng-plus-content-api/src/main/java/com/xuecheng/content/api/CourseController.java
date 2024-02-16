package com.xuecheng.content.api;

import com.xuecheng.content.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "course common api", description = "publish, delete...")
@RestController
public class CourseController {

    @Autowired
    CourseService courseService;

    @Operation(description = "删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId){
        courseService.deleteCourse(courseId);
        return;
    }

    @Operation(description = "下架课程")
    @GetMapping("/courseoffline/{courseId}")
    public void offlineCourse(@PathVariable Long courseId){
        courseService.offlineCourse(courseId);
    }
}
