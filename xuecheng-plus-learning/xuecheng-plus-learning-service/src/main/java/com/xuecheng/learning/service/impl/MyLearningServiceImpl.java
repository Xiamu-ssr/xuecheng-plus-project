package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.feign.client.ContentClient;
import com.xuecheng.feign.client.MediaClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.learning.service.MyLearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyLearningServiceImpl implements MyLearningService {
    @Autowired
    MediaClient mediaClient;
    @Autowired
    ContentClient contentClient;

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //判断学习资格
        String learnstatus = myCourseTablesService.getLearnstatus(userId, courseId).getLearnStatus();
        if ("702002".equals(learnstatus)){
            boolean preview = contentClient.isTeachplanPreview(teachplanId);
            if (preview){
                String mediaUrl = mediaClient.getPlayUrlByMediaId(mediaId).getResult();
                return RestResponse.success(mediaUrl,"本节支持限时免费，欢迎试学");
            }
            return RestResponse.validfail("无法学习，没有选课或者选课后未支付。");
        }else if ("702003".equals(learnstatus)){
            boolean preview = contentClient.isTeachplanPreview(teachplanId);
            if (preview){
                String mediaUrl = mediaClient.getPlayUrlByMediaId(mediaId).getResult();
                return RestResponse.success(mediaUrl,"本节支持限时免费，欢迎试学");
            }
            return RestResponse.validfail("无法学习，课程已过期或者未支付成功");
        }else if ("702001".equals(learnstatus)){
            //远程调用media服务返回视频播放地址
            String mediaUrl = mediaClient.getPlayUrlByMediaId(mediaId).getResult();
            return RestResponse.success(mediaUrl,"您已购买本课程，祝你学习愉快");
        }else {
            return RestResponse.validfail("未知异常");
        }
    }
}
