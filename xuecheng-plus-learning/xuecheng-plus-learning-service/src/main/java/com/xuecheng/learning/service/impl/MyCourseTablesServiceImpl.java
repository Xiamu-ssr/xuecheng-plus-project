package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.feign.client.ContentClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    ContentClient contentClient;

    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;
    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;

    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        //远程调用content-api，查询收费规则
        CoursePublish coursepublish = contentClient.getCoursepublish(courseId);
        if (coursepublish == null){
            XueChengPlusException.cast("课程信息不存在");
        }
        String charge = coursepublish.getCharge();
        if ("201000".equals(charge)){
            //如果是免费课程，向选课记录表、我的课程表写入数据
            xcChooseCourse = addFreeCourse(userId, coursepublish);
            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse);
        }else if ("201001".equals(charge)){
            //如果是收费课程，向选课记录表写入数据
            xcChooseCourse = addChargeCourse(userId, coursepublish);
        }else {
            XueChengPlusException.cast("课程价格存在异常");
        }
        //查询此用户此课程的学习资格，并返回
        String learnStatus = getLearnstatus(userId, courseId).getLearnStatus();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(learnStatus);
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearnstatus(String userId, Long courseId) {
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        //我的课程表是否记录
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if (xcCourseTables == null){
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        //是否未过期
        boolean before = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (before){
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        xcCourseTablesDto.setLearnStatus("702003");
        return xcCourseTablesDto;
    }

    @Override
    @Transactional
    public boolean successChooseCourse(String chooseCourseId) {
        XcChooseCourse chooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if (chooseCourse == null){
            log.error("接收到支付成功的消息，但是根据传来的选课id，找不到选课记录。chooseCourseId:{}", chooseCourseId);
            return false;
        }
        if ("701002".equals(chooseCourse.getStatus())){
            //更新选课记录表
            chooseCourse.setStatus("701001");
            int update = xcChooseCourseMapper.updateById(chooseCourse);
            if (update <= 0){
                log.error("修改选课记录失败：{}", chooseCourse);
                XueChengPlusException.cast("修改选课记录失败");
            }
            //更新课程表
            addCourseTables(chooseCourse);
            return true;
        }
        return false;
    }

    /**
     * 添加免费课程到选课记录表
     *
     * @param userId        用户id
     * @param coursePublish 课程发布
     * @return {@link XcChooseCourse}
     */
    private XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish){
        //如果存在免费的选课记录并且选课状态为成功，直接返回
        Long courseId = coursePublish.getId();

        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700001")//免费课程
                .eq(XcChooseCourse::getStatus, "701001")//选课成功
        );
        if(!CollectionUtils.isEmpty(xcChooseCourses)){
            return xcChooseCourses.get(0);
        }
        //否则，插入数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701001");
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if (insert <= 0){
            XueChengPlusException.cast("选课记录表插入异常");
        }

        return xcChooseCourse;
    }

    /**
     * 添加付费课程到选课记录表
     *
     * @param userId        用户id
     * @param coursePublish 课程发布
     * @return {@link XcChooseCourse}
     */
    private XcChooseCourse addChargeCourse(String userId, CoursePublish coursePublish){
        //如果存在选课记录并且选课状态为成功，直接返回
        Long courseId = coursePublish.getId();

        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(new LambdaQueryWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, courseId)
                .eq(XcChooseCourse::getOrderType, "700002")//收费课程
                .eq(XcChooseCourse::getStatus, "701002")//待支付
        );
        if(!CollectionUtils.isEmpty(xcChooseCourses)){
            return xcChooseCourses.get(0);
        }
        //否则，插入数据
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701002");//待支付
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if (insert <= 0){
            XueChengPlusException.cast("选课记录表插入异常");
        }

        return xcChooseCourse;
    }

    /**
     * 添加 我的课程表
     *
     * @param xcChooseCourse xc选择课程
     * @return {@link XcCourseTables}
     */
    private XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse){
        //选课成功了，才可以加入课程表
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)){
            XueChengPlusException.cast("选课没有成功，无法添加到课程表");
        }
        //先查询是否有此记录
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (xcCourseTables != null) {
            return xcCourseTables;
        }
        //无则插入
        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());//记录选课记录表的id
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());//选课类型
        int insert = xcCourseTablesMapper.insert(xcCourseTables);
        if (insert <=0 ){
            XueChengPlusException.cast("添加至我的课程表失败");
        }
        return xcCourseTables;
    }

    /**
     * 获取课程表一条item
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return {@link XcCourseTables}
     */
    private XcCourseTables getXcCourseTables(String userId, Long courseId){
        return xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId)
        );
    }
}
