package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;


/**
 * 订单表(Orders)表服务接口
 *
 * @author makejava
 * @since 2023-03-25 14:13:52
 */
public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);
}
