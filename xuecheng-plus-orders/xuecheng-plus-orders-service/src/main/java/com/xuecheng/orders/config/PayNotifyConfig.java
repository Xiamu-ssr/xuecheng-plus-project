package com.xuecheng.orders.config;

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

    @Autowired
    MqMessageService mqMessageService;

    //交换机
    public static final String PAYNOTIFY_EXCHANGE_FANOUT = "paynotify_exchange_fanout";
    //支付通知队列
    public static final String PAYNOTIFY_QUEUE = "paynotify_queue";
    //支付结果通知消息类型
    public static final String MESSAGE_TYPE = "payresult_notify";


    //声明交换机，且持久化
    @Bean(PAYNOTIFY_EXCHANGE_FANOUT)
    public FanoutExchange paynotify_exchange_fanout() {
        // 三个参数：交换机名称、是否持久化、当没有queue与其绑定时是否自动删除
        return new FanoutExchange(PAYNOTIFY_EXCHANGE_FANOUT, true, false);
    }
    //支付通知队列,且持久化
    @Bean(PAYNOTIFY_QUEUE)
    public Queue paynotify_queue() {
        return QueueBuilder.durable(PAYNOTIFY_QUEUE).build();
    }

    //交换机和支付通知队列绑定
    @Bean
    public Binding binding_paynotify_queue(@Qualifier(PAYNOTIFY_QUEUE) Queue queue, @Qualifier(PAYNOTIFY_EXCHANGE_FANOUT) FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //设置confirm callback
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String body = "1";
            if (correlationData instanceof EnhancedCorrelationData) {
                body = ((EnhancedCorrelationData) correlationData).getBody();
            }
            if (ack) {
                //消息投递到exchange。删除记录
                mqMessageService.completed(Long.parseLong(body));
                log.debug("消息发送到exchange成功:correlationData={},message_id={} ", correlationData, body);
                System.out.println("消息发送到exchange成功:correlationData={},message_id={}"+correlationData+body);
            } else {
                log.debug("消息发送到exchange失败:cause={},message_id={}",cause, body);
                System.out.println("消息发送到exchange失败:cause={},message_id={}"+cause+body);
            }
        });
        //设置return callback
        rabbitTemplate.setReturnsCallback(returned -> {
            Message message = returned.getMessage();
            int replyCode = returned.getReplyCode();
            String replyText = returned.getReplyText();
            String exchange = returned.getExchange();
            String routingKey = returned.getRoutingKey();
            // 投递失败，记录日志
            log.error("消息发送失败，应答码{}，原因{}，交换机{}，路由键{},消息{}",
                    replyCode, replyText, exchange, routingKey, message.toString());
            MqMessage mqMessage = JSON.parseObject(message.toString(), MqMessage.class);
            // 消息投递到queue失败，将消息再添加到消息表
            mqMessageService.addMessage(mqMessage.getMessageType(),mqMessage.getBusinessKey1(),mqMessage.getBusinessKey2(),mqMessage.getBusinessKey3());
        });
        return rabbitTemplate;
    }
}
