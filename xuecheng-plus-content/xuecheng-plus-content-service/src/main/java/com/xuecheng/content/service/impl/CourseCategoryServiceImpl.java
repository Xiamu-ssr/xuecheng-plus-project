package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> nodes = courseCategoryMapper.selectTreeNodes(id);
        //use map replace list, make it easier to query node
        Map<String, CourseCategoryTreeDto> nodesMap = nodes.stream()
                .filter(item->!id.equals(item.getId()))
                .collect(Collectors.toMap(
                    CourseCategory::getId,
                    value -> value,
                    (exist, replace) -> {
                        return replace;
                    })
                );
        //
        ArrayList<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();
        nodes.stream()
                .filter(item->!id.equals(item.getId()))
                .forEach(node->{
                    if (node.getParentid().equals(id)){
                        courseCategoryList.add(node);
                    }else {
                        CourseCategoryTreeDto nodeP = nodesMap.get(node.getParentid());
                        if (CollectionUtils.isEmpty(nodeP.getChildrenTreeNodes())){
                            nodeP.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        nodeP.getChildrenTreeNodes().add(node);
                    }
                });

        return courseCategoryList;
    }
}
