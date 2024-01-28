package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId) {
        return teachplanMapper.getTreeNodes(courseId);
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto dto) {
        //decide if add or modify
        Long id = dto.getId();
        if (ObjectUtils.isEmpty(id)){
            //add
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(dto, teachplan);
            teachplan.setOrderby(teachplanMapper.getNextOrderby(dto.getParentid(), dto.getCourseId()));
            teachplanMapper.insert(teachplan);
        }else {
            //modify
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(dto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }
}
