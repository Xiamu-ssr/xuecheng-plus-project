package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程计划管理
 *
 * @author mumu
 * @date 2024/01/27
 */
@RestController
@Tag(name = "课程计划信息接口")
public class TeachplanController {
    @Autowired
    private TeachplanService teachplanService;

    @Operation(description = "根据id查询课程的详细计划树")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable @Parameter(description = "课程id") Long courseId){
        return teachplanService.getTreeNodes(courseId);
    }

    @Operation(description = "新增或修改课程计划")
    @PostMapping ("/teachplan")
    public void saveTeachplan(@RequestBody @Validated SaveTeachplanDto dto){
        teachplanService.saveTeachplan(dto);
        return;
    }
}
