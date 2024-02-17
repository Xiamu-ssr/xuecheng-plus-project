package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.model.po.TeachplanMedia;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface TeachplanService {
    public List<TeachplanDto> getTreeNodes(Long courseId);

    public void saveTeachplan(SaveTeachplanDto dto);

    public void deleteTeachplan(Long teachplanId);
    public void moveTeachplan(String move, Long teachplanId);

    /**
     * 关联课程计划和媒体资源
     *
     * @param bindTeachplanMediaDto 绑定教学计划媒体dto
     * @return {@link TeachplanMedia}
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 解散关联课程计划和媒体资源
     *
     * @param teachPlanId 教学计划id
     * @param mediaId     媒体文件id
     */
    public void disAssociationMedia(Long teachPlanId, String mediaId);

    /**
     * 根据课程计划id判断是否支持试学
     *
     * @param teachplanId 教学计划id
     * @return boolean
     */
    public boolean isTeachplanPreview(Long teachplanId);
}
