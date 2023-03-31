package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 购物车(ShoppingCart)表控制层
 *
 * @author makejava
 * @since 2023-03-25 12:26:45
 */
@RestController
@RequestMapping("shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingcartService;

    // 添加购物车
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("购物车数据：{}", shoppingCart);
        // 设置用户id，指定当前是哪个用户的购物车数据
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 查询当前菜品或者套餐是否已经在购物车当中
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        // 判断添加到购物车的是菜品还是套餐
        if (shoppingCart.getDishId() != null) {
            // 添加到购物车的为菜品
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            // 添加到购物车的为套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart cartServiceone = shoppingcartService.getOne(queryWrapper);

        // 判断添加的菜品/套餐是否已存在
        if (cartServiceone != null) {
            // 若已存在，则在原来的基础上加一
            cartServiceone.setNumber(cartServiceone.getNumber() + 1);
            shoppingcartService.updateById(cartServiceone);
        } else {
            // 若不存在，则添加到购物车中，默认为一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingcartService.save(shoppingCart);
            cartServiceone = shoppingCart;
        }
        return R.success(cartServiceone);
    }

    // 查看购物车
    @GetMapping("/list")
    public R list() {
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCartList = shoppingcartService.list(queryWrapper);
        return R.success(shoppingCartList);
    }

    // 清空购物车
    @DeleteMapping("/clean")
    public R<String> clean() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingcartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

    // 减少菜品
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());

        // 判断想要减少的是套餐还是菜品
        if (shoppingCart.getSetmealId() != null) {
            // 说明想要减少的是套餐数量
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        } else {
            // 说明想要减少的是菜品数量
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        }

        ShoppingCart one = shoppingcartService.getOne(queryWrapper);
        if (one.getNumber() == 1) {
            shoppingcartService.remove(queryWrapper);
        } else {
            one.setNumber(one.getNumber() - 1);
            shoppingcartService.updateById(one);
        }
        return R.success(one);
    }

}
















