package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单表(Orders)表控制层
 *
 * @author makejava
 * @since 2023-03-25 14:13:52
 */
@RestController
@RequestMapping("orders")
@Slf4j
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    //用户下单
    @PostMapping("/submit")
    public R submit(@RequestBody Orders orders) {
        log.info("订单数据:{}", orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }


}