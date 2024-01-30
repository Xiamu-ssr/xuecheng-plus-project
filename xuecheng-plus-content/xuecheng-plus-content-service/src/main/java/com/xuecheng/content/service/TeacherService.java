package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface TeacherService {
    public List<CourseTeacher> getCourseTeach(Long courseId);

    public CourseTeacher saveCourseTeacher(CourseTeacher dto);
    public void deleteCourseTeacher(Long courseId, Long teacherId);
}
