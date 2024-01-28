package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 查询课程计划信息dto
 *
 * @author mumu
 * @date 2024/01/28
 */
@Data
@ToString(callSuper = true)
public class TeachplanDto extends Teachplan {
    private List<TeachplanDto> teachPlanTreeNodes;
    private TeachplanMedia teachplanMedia;
}
