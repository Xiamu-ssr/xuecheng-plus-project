package com.xuecheng.learning.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.service.MyLearningService;
import com.xuecheng.learning.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    MyLearningService myLearningService;

    @Operation(description = "获取视频")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getvideo(@PathVariable("courseId") Long courseId, @PathVariable("teachplanId") Long teachplanId, @PathVariable("mediaId") String mediaId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = user.getId();
        return myLearningService.getVideo(userId, courseId, teachplanId, mediaId);
    }

}
