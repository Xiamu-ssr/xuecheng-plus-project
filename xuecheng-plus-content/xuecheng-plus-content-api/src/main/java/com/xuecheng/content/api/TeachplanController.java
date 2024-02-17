package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bouncycastle.pqc.crypto.util.PQCOtherInfoGenerator;
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

    @Operation(description = "delete teach plan by teachplan_id")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void deleteTeachplan(@PathVariable Long teachplanId){
        teachplanService.deleteTeachplan(teachplanId);
        return;
    }

    @Operation(description = "move down/up the order of teach plan by teachplan_id. {move} = ['movedown','moveup']")
    @PostMapping("/teachplan/{move}/{teachplanId}")
    public void moveTeachplan(@PathVariable String move, @PathVariable Long teachplanId){

        teachplanService.moveTeachplan(move ,teachplanId);
        return;
    }

    @Operation(description = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @Operation(description = "课程计划和媒资信息解除绑定")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public void disAssociationMedia(@PathVariable Long teachPlanId, @PathVariable String mediaId){
        teachplanService.disAssociationMedia(teachPlanId, mediaId);
    }


    @Operation(description = "指定课程计划是否支持试学")
    @GetMapping("/teachplan/isPreview/{teachplanId}")
    public boolean isTeachplanPreview(@PathVariable Long teachplanId){
        return teachplanService.isTeachplanPreview(teachplanId);
    }

}
