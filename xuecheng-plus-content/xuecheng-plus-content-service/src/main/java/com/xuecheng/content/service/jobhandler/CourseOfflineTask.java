package com.xuecheng.content.service.jobhandler;


import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.feign.client.MediaClient;
import com.xuecheng.feign.client.SearchClient;
import com.xuecheng.feign.pojo.CourseIndex;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

//review:未测试过。原因-课程下架可能不需要这么多操作，只需要拦截选课和支付就行。
@Component
@Slf4j
public class CourseOfflineTask extends MessageProcessAbstract {

    @Autowired
    MediaClient mediaClient;
    @Autowired
    SearchClient searchClient;
    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    CoursePublishMapper coursePublishMapper;

    @XxlJob("CourseOfflineJobHandler")
    public void CourseOfflineJobHandler(){
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        process(shardIndex, shardTotal, "course_offline", 30, 60);
    }


    @Override
    public boolean execute(MqMessage mqMessage) {
        //business_key1，为courseId
        Long corseId = Long.valueOf(mqMessage.getBusinessKey1());

        //删除minio中的静态课程文件
        deleteCourseHtml(mqMessage, corseId);

        //删除elastic中的索引
        deleteCourseIndex(mqMessage, corseId);

        //todo
        //删除redis中的缓存
        deleteCourseCache(mqMessage, corseId);

        return true;
    }

    /**
     * 生成课程静态html
     *
     * @param mqMessage mq_message实体
     * @param courseId  课程id
     */
    public void deleteCourseHtml(MqMessage mqMessage,long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理
        //取出该阶段的执行状态，如果为不为0，则无需处理
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0){
            log.debug("课程静态页面删除任务完成，无需处理");
            return;
        }
        //处理开始
        boolean b = mediaClient.deleteStaticHtml4Minio(courseId);
        if (!b){
            log.error("删除静态页面失败, taskId:{}, courseId:{}",taskId, courseId);
            XueChengPlusException.cast("删除静态页面失败");
        }
        //处理完成修改对应数据状态
        mqMessageService.completedStageOne(taskId);
    }

    /**
     * 保存课程索引到elasticsearch
     *
     * @param mqMessage mq_message实体
     * @param courseId  课程id
     */
    public void deleteCourseIndex(MqMessage mqMessage,long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理
        //取出该阶段的执行状态，如果为不为0，则无需处理
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0){
            log.debug("课程索引删除已完成，无需处理");
            return;
        }
        //处理开始
        Boolean delete = searchClient.delete(String.valueOf(courseId));
        if (!delete){
            XueChengPlusException.cast("远程调用搜索服务-删除课程索引失败");
        }
        //处理完成修改对应数据状态
        mqMessageService.completedStageTwo(taskId);
    }

    /**
     * 保存课程缓存到Redis
     *
     * @param mqMessage mq_message实体
     * @param courseId  课程id
     */
    public void deleteCourseCache(MqMessage mqMessage,long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理
        //取出该阶段的执行状态，如果为不为0，则无需处理
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree > 0){
            log.debug("课程redis缓存删除已完成，无需处理");
            return;
        }
        //处理开始
        //todo
        //处理完成修改对应数据状态
        mqMessageService.completedStageThree(taskId);
    }
}
