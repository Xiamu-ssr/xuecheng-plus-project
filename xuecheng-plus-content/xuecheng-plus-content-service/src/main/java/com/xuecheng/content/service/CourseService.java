package com.xuecheng.content.service;

import org.springframework.web.bind.annotation.PathVariable;

public interface CourseService {
    public void deleteCourse(Long courseId);

    public void offlineCourse(Long courseId);
}
