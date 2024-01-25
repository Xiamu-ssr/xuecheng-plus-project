package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author xiamussr
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    //use recursion query category
    public List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
