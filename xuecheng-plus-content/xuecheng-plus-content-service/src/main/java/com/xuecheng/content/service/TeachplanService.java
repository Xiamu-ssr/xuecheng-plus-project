package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface TeachplanService {
    public List<TeachplanDto> getTreeNodes(Long courseId);

    public void saveTeachplan(SaveTeachplanDto dto);

    public void deleteTeachplan(Long teachplanId);
    public void moveTeachplan(String move, Long teachplanId);

}
