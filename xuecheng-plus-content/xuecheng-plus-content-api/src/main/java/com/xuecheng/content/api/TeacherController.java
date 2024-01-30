package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "course teachers setting")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Operation(description = "get teacher list of specified course by courseId")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeach(@PathVariable Long courseId){
        return teacherService.getCourseTeach(courseId);
    }

    @Operation(description = "add or modify teacher for someone course")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody CourseTeacher dto){
        return teacherService.saveCourseTeacher(dto);
    }

//    @Operation(description = "modify one teacher of someone course")
//    @PutMapping("/courseTeacher")
//    public CourseTeacher modifyCourseTeacher(@RequestBody CourseTeacher dto){
//        return null;
//    }

    @DeleteMapping("courseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId){
        teacherService.deleteCourseTeacher(courseId, teacherId);
        return;
    }
}
