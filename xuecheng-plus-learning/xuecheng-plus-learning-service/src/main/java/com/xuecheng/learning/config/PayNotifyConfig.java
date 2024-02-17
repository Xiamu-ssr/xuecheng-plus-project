package com.xuecheng.learning.config;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.ReturnCallback;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @author Mr.M
 * @version 1.0
 * @description
 * @date 2023/2/23 16:59
 */
@Slf4j
@Configuration
public class PayNotifyConfig{


    //交换机
    public static final String PAYNOTIFY_EXCHANGE_FANOUT = "paynotify_exchange_fanout";
    //支付通知队列
    public static final String PAYNOTIFY_QUEUE = "paynotify_queue";
    //支付结果通知消息类型
    public static final String MESSAGE_TYPE = "payresult_notify";

}
