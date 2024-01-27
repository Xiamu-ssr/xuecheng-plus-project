package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
public class TeachplanDto extends Teachplan {
    private List<TeachplanDto> teachPlanTreeNodes;
    private TeachplanMedia teachplanMedia;
}
