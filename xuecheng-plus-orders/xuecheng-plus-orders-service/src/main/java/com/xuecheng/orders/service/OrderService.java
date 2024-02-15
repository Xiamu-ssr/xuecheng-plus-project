package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
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
}
