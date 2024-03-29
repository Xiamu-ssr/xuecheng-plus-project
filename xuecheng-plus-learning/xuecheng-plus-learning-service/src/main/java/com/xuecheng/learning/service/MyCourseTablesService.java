package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;
import org.springframework.web.bind.annotation.PathVariable;

public interface MyCourseTablesService {
    /**
     * 添加 选课
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return {@link XcChooseCourseDto}
     */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);


    /**
     * 获取学习资格
     *
     * @param courseId 课程id
     * @return {@link XcCourseTablesDto}
     */
    public XcCourseTablesDto getLearnstatus(String userId, Long courseId);

    /**
     * 保存选课状态为成功
     * <br/>
     * 用于支付成功后，rabbitmq消息接收后调用
     *
     * @param chooseCourseId 选择课程id
     * @return boolean
     */
    public boolean successChooseCourse(String chooseCourseId);


    /**
     * 查询我的课程表数据
     *
     * @param params params
     * @return {@link PageResult}<{@link XcCourseTables}>
     */
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params);
}
