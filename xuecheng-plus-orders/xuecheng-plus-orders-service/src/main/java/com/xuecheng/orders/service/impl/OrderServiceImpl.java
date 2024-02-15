package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.QRCodeUtil;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;
    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Value("${pay.qrcodeurl}")
    private String qrcodeurl;

    @Autowired
    XcOrdersMapper ordersMapper;
    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;
    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Override
    @Transactional
    public PayRecordDto generatePayCode(String userId, AddOrderDto addOrderDto){
        //校验订单信息是否被纂改
        checkOrder(userId, addOrderDto);

        //保存订单信息
        XcOrders xcOrders = saveOrder(userId, addOrderDto);

        //插入支付记录pay_record
        XcPayRecord xcPayRecord = savePayRecord(xcOrders);
        Long payNo = xcPayRecord.getPayNo();

        //生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String url = String.format(qrcodeurl, payNo);
        String qrCode = null;
        try {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码失败");
        }
        //返回
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(xcPayRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayNo(String payNo) {
        return payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        //查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        System.out.println(payStatusDto);
        //todo
        //更新支付状态
        saveAliPayStatus(null);
        return null;
    }

    private void checkOrder(String userId, AddOrderDto addOrderDto){
        //取出课程id
        //String chooseCourseId = addOrderDto.getOutBusinessId();
        //取出价格对比
        return ;
    }

    /**
     * 保存订单
     *
     * @param userId      用户id
     * @param addOrderDto 添加订单dto
     * @return {@link XcOrders}
     */
    private XcOrders saveOrder(String userId, AddOrderDto addOrderDto){
        //幂等性判断，同一个选课记录，只能有一个订单
        XcOrders orders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (orders != null){
            return orders;
        }
        //没有则新建订单
        orders = new XcOrders();
        BeanUtils.copyProperties(addOrderDto, orders);
        //使用雪花算法生成订单号
        orders.setId(IdWorkerUtils.getInstance().nextId());
        orders.setUserId(userId);
        orders.setStatus("600001");
        //插入订单主表order
        Long ordersId = orders.getId();
        int insert = ordersMapper.insert(orders);
        if (insert <= 0){
            XueChengPlusException.cast("订单插入失败");
        }
        //插入订单明细表order_goods
        String orderDetail = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> ordersGoods = JSON.parseArray(orderDetail, XcOrdersGoods.class);
        ordersGoods.forEach(goods ->{
            goods.setOrderId(ordersId);
            int insert1 = ordersGoodsMapper.insert(goods);
            if (insert1 <= 0){
                XueChengPlusException.cast("插入订单明细失败");
            }
        });

        return orders;
    }

    /**
     * 按业务id获取订单
     *
     * @param businessId 业务id-选课记录表choose_course的主键
     * @return {@link XcOrders}
     */
    private XcOrders getOrderByBusinessId(String businessId){
        return ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
    }

    /**
     * 保存支付记录
     *
     * @param orders 订单信息
     * @return {@link XcPayRecord}
     */
    private XcPayRecord savePayRecord(XcOrders orders) {
        //如果订单不存在，则停止，可能存在脏数据
        Long ordersId = orders.getId();
        XcOrders xcOrders = ordersMapper.selectById(ordersId);
        if (xcOrders == null){
            XueChengPlusException.cast("订单不存，无法添加支付记录");
        }
        //如果订单支付结果为成功
        String status = xcOrders.getStatus();
        if ("600002".equals(status)){
            XueChengPlusException.cast("此订单已支付，不能再添加支付记录");
        }
        //否则新增
        XcPayRecord xcPayRecord = new XcPayRecord();
        //支付记录号，将来传入支付宝
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());
        xcPayRecord.setOrderId(ordersId);
        xcPayRecord.setOrderName(orders.getOrderName());
        xcPayRecord.setTotalPrice(orders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        //未支付
        xcPayRecord.setStatus("601001");
        xcPayRecord.setUserId(orders.getUserId());

        int insert = payRecordMapper.insert(xcPayRecord);
        if (insert <= 0){
            XueChengPlusException.cast("插入支付记录表失败");
        }

        return xcPayRecord;
    }

    /**
     * 从支付宝查询支付结果
     *
     * @param payNo payNo
     * @return {@link PayStatusDto}
     */
    private PayStatusDto queryPayResultFromAlipay(String payNo){
        //查询支付状态
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        //创建API对应的request
        AlipayTradeQueryRequest alipayRequest = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", payNo);
        alipayRequest.setBizContent(bizContent.toString());//填充业务参数
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(alipayRequest);
        } catch (AlipayApiException e) {
            XueChengPlusException.cast("查询支付宝支付状态失败");
        }
        //解析支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_no(response.getTradeNo());
        payStatusDto.setTrade_status(response.getTradeStatus());
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(response.getTotalAmount());

        return payStatusDto;
    }

    /**
     * 保存支付状态
     * <br/>
     * 用于确认支付成功后执行，更新订单状态
     *
     * @param payStatusDto 支付状态dto
     */
    private void saveAliPayStatus(PayStatusDto payStatusDto){


    }

}
