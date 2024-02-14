package com.xuecheng.learning.api;

import com.xuecheng.base.model.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mumu
 * @version 1.0
 * @description 我的学习接口
 * @date 2022/10/27 8:59
 */
@Tag(name = "学习过程管理接口", description = "学习过程管理接口")
@Slf4j
@RestController
public class MyLearningController {


    @Operation(description = "获取视频")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getvideo(@PathVariable("courseId") Long courseId, @PathVariable("courseId") Long teachplanId, @PathVariable("mediaId") String mediaId) {

        return null;

    }

}
