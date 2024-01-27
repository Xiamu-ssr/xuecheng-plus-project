package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 课程库信息服务impl
 *
 * @author mumu
 * @date 2024/01/25
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    /**
     * 航向基准测绘器
     */
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 查询课程库列表
     *
     * @param pageParams 页面参数
     * @param dto        到
     * @return {@link PageResult}<{@link CourseBase}>
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto dto) {

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(dto.getCourseName()), CourseBase::getName, dto.getCourseName())
                .eq(StringUtils.isNotEmpty(dto.getAuditStatus()), CourseBase::getAuditStatus, dto.getAuditStatus())
                .eq(StringUtils.isNotEmpty(dto.getPublishStatus()), CourseBase::getStatus, dto.getPublishStatus())
        ;

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    /**
     * 创建课程基础
     *
     * @param companyId 公司id
     * @param dto       到
     * @return {@link CourseBaseInfoDto}
     */
    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //write into course_base table
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int save1 = saveCourseBase(courseBase);
        if (save1 <= 0){
            throw new RuntimeException("添加课程失败");
        }
        //write into course_market table
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseBase.getId());
        int save2 = saveCourseMarket(courseMarket);
        if (save2 <= 0){
            throw new RuntimeException("添加课程失败");
        }
        //after all data write into sql
        //need query info back to front-end

        return getCourseBaseInfo(courseBase.getId());
    }

    /**
     * 保存课程基础信息
     *
     * @param courseBase 实体
     * @return int
     */
    private int saveCourseBase(CourseBase courseBase){
        //validity check
        //check if data is already exist in sql
        CourseBase courseBase1 = courseBaseMapper.selectById(courseBase.getId());
        if (courseBase1 == null){
            return courseBaseMapper.insert(courseBase);
        }else {
            return courseBaseMapper.updateById(courseBase);
        }
    }

    /**
     * 保存课程营销信息
     *
     * @param courseMarket 实体
     * @return int
     */
    private int saveCourseMarket(CourseMarket courseMarket){
        //validity check
        if (courseMarket.getCharge().equals("201001")){
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <=0){
                XueChengPlusException.cast("价格不能为空且必须大于0");
            }
        }
        //check if data is already exist in sql
        CourseMarket courseMarket1 = courseMarketMapper.selectById(courseMarket.getId());
        if (courseMarket1 == null){
            return courseMarketMapper.insert(courseMarket);
        }else {
            return courseMarketMapper.updateById(courseMarket);
        }
    }

    /**
     * 获取课程基础信息
     *
     * @param courseId 课程id
     * @return {@link CourseBaseInfoDto}
     */
    private CourseBaseInfoDto getCourseBaseInfo(long courseId){
        //select one
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (ObjectUtils.isEmpty(courseBase)){
            return null;
        }
        //select the other
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (ObjectUtils.isEmpty(courseMarket)){
            return null;
        }
        //merge
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        //replace code to chinese
        courseBaseInfoDto.setMtName(courseCategoryMapper.selectById(courseBase.getMt()).getName());
        courseBaseInfoDto.setStName(courseCategoryMapper.selectById(courseBase.getSt()).getName());

        return courseBaseInfoDto;
    }
}
