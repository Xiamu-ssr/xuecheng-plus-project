package com.xuecheng.content.service.jobhandler;


import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {

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
        //todo
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
        //todo
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
