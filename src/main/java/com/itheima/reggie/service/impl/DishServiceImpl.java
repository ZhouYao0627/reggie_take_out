package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.dto.DishDto;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 菜品管理(Dish)表服务实现类
 *
 * @author makejava
 * @since 2023-03-21 13:01:48
 */
@Service("dishService")
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 先将菜品基本信息保存到菜品表
        dishService.save(dishDto);
        // 设置菜品口味
        List<DishFlavor> dishFlavorList = dishDto.getFlavors().stream()
                .map(dishFlavor -> {
                    dishFlavor.setDishId(dishDto.getId());
                    return dishFlavor;
                }).collect(Collectors.toList());
        // 保存菜品口味到 dish_flavor 数据表
        dishFlavorService.saveBatch(dishFlavorList);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 根据菜品 id 查询菜品信息与对应的口味信息
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getId, id);
        Dish dish = dishService.getOne(queryWrapper);
        // 把 dish 中的数据传到 DishDto 中
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        // 通过 dish_id 找出 dish_flavor 相关信息
        LambdaQueryWrapper<DishFlavor> queryWrapperDishDto = new LambdaQueryWrapper<>();
        queryWrapperDishDto.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapperDishDto);

        dishDto.setFlavors(dishFlavorList);
        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 点击保存后，相当于是重现上传一份数据
        // 首先保存 dish 表中的数据
        dishService.save(dishDto);

        // 可以将原先的口味数据删除，然后再重新添加
        // 先删除
        Long dishDtoId = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDtoId);
        dishFlavorService.remove(queryWrapper);
        // 再添加
        List<DishFlavor> dishFlavorList = dishDto.getFlavors().stream()
                .map(new Function<DishFlavor, DishFlavor>() {
                    @Override
                    public DishFlavor apply(DishFlavor dishFlavor) {
                        dishFlavor.setDishId(dishDtoId);
                        return dishFlavor;
                    }
                }).collect(Collectors.toList());
        dishFlavorService.saveBatch(dishFlavorList);
    }


}













