package com.xuecheng.content.service.jobhandler;


import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
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

@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    SearchClient searchClient;
    @Autowired
    CoursePublishService coursePublishService;
    @Autowired
    CoursePublishMapper coursePublishMapper;

    @XxlJob("CoursePublishJobHandler")
    public void CoursePublishJobHandler(){
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }


    @Override
    public boolean execute(MqMessage mqMessage) {
        //那business_key1，为courseId
        Long corseId = Long.valueOf(mqMessage.getBusinessKey1());

        //课程静态化上传到minio
        generateCourseHtml(mqMessage, corseId);
        //向elastic search写索引数据
        saveCourseIndex(mqMessage, corseId);
        //向redis写缓存
        saveCourseCache(mqMessage, corseId);

        return true;
    }

    /**
     * 生成课程静态html
     *
     * @param mqMessage mq_message实体
     * @param courseId  课程id
     */
    public void generateCourseHtml(MqMessage mqMessage,long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理
        //取出该阶段的执行状态，如果为不为0，则无需处理
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0){
            log.debug("课程静态化任务完成，无需处理");
            return;
        }
        //处理开始
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null){
            log.error("生成静态页面为空, taskId:{}, courseId:{}",taskId, courseId);
            XueChengPlusException.cast("生成静态页面为空");
        }
        coursePublishService.uploadCourseHtml(courseId, file);
        //处理完成修改对应数据状态
        mqMessageService.completedStageOne(taskId);
    }

    /**
     * 保存课程索引到elasticsearch
     *
     * @param mqMessage mq_message实体
     * @param courseId  课程id
     */
    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理
        //取出该阶段的执行状态，如果为不为0，则无需处理
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0){
            log.debug("课程索引建立已完成，无需处理");
            return;
        }
        //处理开始
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        Boolean add = searchClient.add(courseIndex);
        if (!add){
            XueChengPlusException.cast("远程调用搜索服务添加课程索引失败");
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
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //幂等性处理
        //取出该阶段的执行状态，如果为不为0，则无需处理
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree > 0){
            log.debug("课程索引建立已完成，无需处理");
            return;
        }
        //处理开始
        //todo
        //处理完成修改对应数据状态
        mqMessageService.completedStageThree(taskId);
    }
}
