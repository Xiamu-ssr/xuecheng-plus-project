package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import org.springframework.web.bind.annotation.RequestBody;

public interface OrderService {

    /**
     * 生成付款二维码
     *
     * @param userId      用户id
     * @param addOrderDto 添加订单dto
     * @return {@link PayRecordDto}
     */
    public PayRecordDto generatePayCode(String userId, AddOrderDto addOrderDto);

    /**
     * 按payNo获取支付记录
     *
     * @param payNo 支付No
     * @return {@link XcPayRecord}
     */
    public XcPayRecord getPayRecordByPayNo(String payNo);

    /**
     * 查询支付结果
     *
     * @param payNo payNo
     * @return {@link PayRecordDto}
     */
    public PayRecordDto queryPayResult(String payNo);

    /**
     * 保存支付状态
     * <br/>
     * 用于确认支付成功后执行，更新订单状态
     *
     * @param payStatusDto 支付状态dto
     */
    public void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 发送支付结果通知到rabbitmq
     *
     * @param message 消息
     */
    public void notifyPayResult(MqMessage message);
}
