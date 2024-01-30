package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author xiamussr
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    public List<TeachplanDto> getTreeNodes(@Param("courseId") Long courseId);

    @Select("SELECT MAX(orderby)+1 FROM xc_content.teachplan WHERE parentid = #{parentid} AND course_id = #{courseId}")
    Integer getNextOrderby(Long courseId, Long parentid);

    @Select("select * from xc_content.teachplan " +
            "where course_id = #{courseId} and parentid = #{parentid} " +
            "and orderby < #{orderby} " +
            "order by orderby DESC " +
            "limit 1")
    Teachplan getPreviousRecord(Long courseId, Long parentid, Integer orderby);

    @Select("select * from xc_content.teachplan " +
            "where course_id = #{courseId} and parentid = #{parentid} " +
            "and orderby > #{orderby} " +
            "order by orderby " +
            "limit 1")
    Teachplan getNextRecord(Long courseId, Long parentid, Integer orderby);
}
