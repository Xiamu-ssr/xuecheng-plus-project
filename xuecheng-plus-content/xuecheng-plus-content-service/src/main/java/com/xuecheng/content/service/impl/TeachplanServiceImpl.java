package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
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
            teachplan.setOrderby(teachplanMapper.getNextOrderby(dto.getCourseId(), dto.getParentid()));
            teachplanMapper.insert(teachplan);
        }else {
            //modify
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(dto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Override
    public void deleteTeachplan(Long teachplanId) {
        //if large chapter , check whether it contains subchapter
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan.getGrade() == 1){
            //if contains subchapter, cannot delete throw error
            Long count = teachplanMapper.selectCount(new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getParentid, teachplan.getId()));
            log.info("deleteTeachplan.teachplanMapper.selectCount = {}", count);
            if (count != 0){
                XueChengPlusException.cast("This chapter contains subchapter, please delete subchapter before delete it.");
            }else {
                //if not, delete
                teachplanMapper.deleteById(teachplanId);
            }
        }else if (teachplan.getGrade() == 2){
            //if subchapter, delete it, as media resource, don't delete for now
            teachplanMapper.deleteById(teachplanId);
        }
    }

    @Override
    @Transactional
    public void moveTeachplan(String move, Long teachplanId) {
        log.info("move = {}, id = {}", move, teachplanId);
        int moveStep = 1;
        if (move.equals("movedown")){
            moveStep = 1;
        }else if (move.equals("moveup")){
            moveStep = -1;
        }
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        //if this chapter is first, cannot move up
        if (moveStep == -1 && teachplan.getOrderby() <= 1){
            XueChengPlusException.cast("The order of this chapter is already in the first place, cannot be moved up.");
        }
        //if this chapter is last, cannot move down
        Integer maxOrderby = teachplanMapper.getNextOrderby(teachplan.getCourseId(), teachplan.getParentid())-1;
        if (moveStep == 1 && teachplan.getOrderby().equals(maxOrderby)){
            XueChengPlusException.cast("The order of this chapter is already in the last place, cannot be move down");
        }
        if (moveStep == -1){
            //moveup
            Teachplan previousRecord = teachplanMapper.getPreviousRecord(teachplan.getCourseId(), teachplan.getParentid(), teachplan.getOrderby());
            Integer temp = teachplan.getOrderby();
            teachplan.setOrderby(previousRecord.getOrderby());
            previousRecord.setOrderby(temp);

            teachplan.setChangeDate(null);
            previousRecord.setChangeDate(null);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(previousRecord);
        }else {
            //movedown
            Teachplan nextRecord = teachplanMapper.getNextRecord(teachplan.getCourseId(), teachplan.getParentid(), teachplan.getOrderby());
            Integer temp = teachplan.getOrderby();
            teachplan.setOrderby(nextRecord.getOrderby());
            nextRecord.setOrderby(temp);

            teachplan.setChangeDate(null);
            nextRecord.setChangeDate(null);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(nextRecord);
        }
    }
}
