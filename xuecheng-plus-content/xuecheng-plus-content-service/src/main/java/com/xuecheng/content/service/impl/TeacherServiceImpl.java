package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TeacherServiceImpl implements TeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getCourseTeach(Long courseId) {
        return courseTeacherMapper.selectList(new LambdaQueryWrapper<CourseTeacher>()
                .eq(CourseTeacher::getCourseId, courseId)
        );
    }

    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher dto) {
        if (ObjectUtils.isEmpty(dto.getId())){
            courseTeacherMapper.insert(dto);
        }else {
            courseTeacherMapper.updateById(dto);
        }
        return dto;
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        log.info("delete courseTeacher which courseId = {}, teacherId = {}", courseId, teacherId);
        int count = courseTeacherMapper.deleteById(teacherId);
        if (count != 0){
            return;
        }else {
            XueChengPlusException.cast("delete Course Teacher Fail.");
        }
    }
}
