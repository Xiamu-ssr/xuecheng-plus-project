package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson2.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 监听支付通知服务
 *
 * @author mumu
 * @date 2024/02/16
 */
@Service
@Slf4j
public class ReceivePayNotifyService {
    @Autowired
    MyCourseTablesService myCourseTablesService;

    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message){
        //解析消息
        byte[] body = message.getBody();
        String string = new String(body);
        MqMessage mqMessage = JSON.parseObject(string, MqMessage.class);
        log.info("mqMessage={}", mqMessage);

        String chooseCourseId = mqMessage.getBusinessKey1();
        String orderType = mqMessage.getBusinessKey2();
        log.info(chooseCourseId, orderType);
        //约束
        if ("60201".equals(orderType)){
            //更新选课记录，并向我的课程表插入记录
            boolean b = myCourseTablesService.successChooseCourse(chooseCourseId);
            //boolean b = false;
            if (!b){
                log.error("订单支付成功，但保存选课记录、更新课程表失败");
                XueChengPlusException.cast("订单支付成功，但保存选课记录、更新课程表失败");
            }else {
                log.info("订单支付完成，后续操作完成！:)");
            }
        }
    }

}
