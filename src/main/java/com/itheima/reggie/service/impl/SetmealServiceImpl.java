package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.entity.dto.SetmealDto;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 套餐(Setmeal)表服务实现类
 *
 * @author makejava
 * @since 2023-03-21 13:02:03
 */
@Service("setmealService")
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        // 用到两张表
        // 表1：setmeal -> 对其中的数据进行添加
        setmealService.save(setmealDto);

        // 表2：setmeal_dish ->
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes().stream()
                .map(setmealDish -> {
                    setmealDish.setSetmealId(setmealDto.getId());
                    return setmealDish;
                }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishList);
    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        // 1.先回显 Setmeal 信息
        Setmeal setmeal = setmealService.getById(id);
        // 把信息赋值到 SetmealDto中
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        // 2.再回显 setmealDish 信息
        // 得通过 setmeal_id 获取口味列表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        // 1.更新 setmeal 表中的信息
        setmealService.updateById(setmealDto);

        // 2.更新 setmealDish 表中的信息
        // 2.1 先删除原有的 dish 信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        // 2.2 再添加现在存入的 dish 相关信息
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        List<SetmealDish> SetmealDishes = setmealDishList.stream()
                .map(setmealDish -> {
                    setmealDish.setSetmealId(setmealDto.getId());
                    return setmealDish;
                }).collect(Collectors.toList());
        setmealDishService.saveBatch(SetmealDishes);
    }


}






