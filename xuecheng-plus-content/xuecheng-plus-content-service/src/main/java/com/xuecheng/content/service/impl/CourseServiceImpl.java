package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CourseServiceImpl implements CourseService {
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;
    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Transactional
    @Override
    public void deleteCourse(Long courseId) {
        //validate audit status
        if (!StringUtils.equals("202002", courseBaseMapper.selectById(courseId).getAuditStatus())){
            XueChengPlusException.cast("delete Course Fail. Course's auditStatus must be 'not check[202002] when delete.'");
        }

        //delete courseBase
        int delete1 = courseBaseMapper.deleteById(courseId);
        //delete course market
        int delete2 = courseMarketMapper.deleteById(courseId);
        //delete course teachplan
        int delete3 = teachplanMapper.delete(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, courseId));
        //delete course teachplan media
        int delete4 = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getCourseId, courseId));
        //delete course teacher
        int delete5 = courseTeacherMapper.delete(new LambdaQueryWrapper<CourseTeacher>()
                .eq(CourseTeacher::getCourseId, courseId));
        if (delete1 == 0){
            XueChengPlusException.cast("delete course Fail.");
        }
        //validate
        Long count1 = courseBaseMapper.selectCount(new LambdaQueryWrapper<CourseBase>()
                .eq(CourseBase::getId, courseId));
        Long count2 = courseMarketMapper.selectCount(new LambdaQueryWrapper<CourseMarket>()
                .eq(CourseMarket::getId, courseId));
        Long count3 = teachplanMapper.selectCount(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, courseId));
        Long count4 = teachplanMediaMapper.selectCount(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getCourseId, courseId));
        Long count5 = courseTeacherMapper.selectCount(new LambdaQueryWrapper<CourseTeacher>()
                .eq(CourseTeacher::getCourseId, courseId));
        if (count1==0 && count2==0 && count3==0 && count4==0 && count5==0){
            return;
        }else {
            XueChengPlusException.cast("delete Course Fail.");
        }
    }
}
