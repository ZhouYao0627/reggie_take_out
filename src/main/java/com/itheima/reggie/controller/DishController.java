package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.dto.DishDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;

    // 分页展示菜品
    @GetMapping("/page")
    public R list(Integer page, Integer pageSize, String name) {
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.like(StringUtils.hasText(name), Dish::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 构造分页构造器
        Page<Dish> dishPage = new Page<>(page, pageSize);
        // 进行分页查询
        dishService.page(dishPage, queryWrapper);
        // 构造分页构造器
        Page<DishDto> dishDtoPage = new Page<>();
        // 对象拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        List<DishDto> dishDtoList = dishPage.getRecords().stream()
                .map(dish -> {
                    DishDto dishDto = new DishDto();
                    // 对象拷贝
                    BeanUtils.copyProperties(dish, dishDto);
                    Category category = categoryService.getById(dish.getCategoryId());
                    if (category != null) {
                        String categoryName = category.getName();
                        dishDto.setCategoryName(categoryName);
                    }
                    return dishDto;
                }).collect(Collectors.toList());
        dishDtoPage.setRecords(dishDtoList);
        return R.success(dishDtoPage);
    }

    // 新增菜品
    @PostMapping
    public R saveWithFlavor(DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        // 清理所有菜品缓存数据
        // Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);
        // 清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    // 修改菜品1 -> 回显信息
    // 根据 Id 查询菜品信息与对应的口味信息
    @GetMapping("/{id}")
    public R update(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    // 修改菜品2 -> 修改菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    // 起售/停售
    @PostMapping("/status/{status}")
    public R sale(@PathVariable int status, String[] ids) {
        for (String id : ids) {
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("修改成功");
    }

    // 删除菜品
    @DeleteMapping
    public R delete(String[] ids) {
        for (String id : ids) {
            dishService.removeById(id);
        }
        return R.success("删除成功");
    }

    // 套餐管理中的菜品类别
    @GetMapping("/list")
    public R list(Dish dish) {
        List<DishDto> dishDtoList = null;
        // 动态构造 key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 先从 redis 中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        // 判断 redis 中是否有这些数据
        if (dishDtoList != null) {
            // 若存在这些数据，则直接返回，无需查询数据库
            return R.success(dishDtoList);
        }
        // 若不存在这些数据，则通过数据库查询这些数据，并存入 redis 中
        // 需要通过 categoryId 查询类名
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> dishList = dishService.list(queryWrapper);

        dishDtoList = dishList.stream()
                .map(dish1 -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(dish1, dishDto);
                    // 根据 id 查询分类对象
                    Long categoryId = dish1.getCategoryId();
                    Category category = categoryService.getById(categoryId);
                    if (category != null) {
                        String categoryName = category.getName();
                        dishDto.setCategoryName(categoryName);
                    }

                    // 当前菜品 id
                    Long dish1Id = dish1.getId();
                    LambdaQueryWrapper<DishFlavor> queryWrapperDishFlavor = new LambdaQueryWrapper<>();
                    queryWrapperDishFlavor.eq(DishFlavor::getDishId, dish1Id);

                    List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapperDishFlavor);
                    dishDto.setFlavors(dishFlavorList);

                    return dishDto;
                }).collect(Collectors.toList());

        // 若不存在这些数据，则通过数据库查询这些数据，并存入 redis 中
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }


}


































