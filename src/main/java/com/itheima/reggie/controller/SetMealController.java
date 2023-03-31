package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.dto.SetmealDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R list(Integer page, Integer pageSize, String name) {
        // 首先先将除套餐分类之外的数据显示到页面上
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        setmealService.page(setmealPage, queryWrapper);

        Page<SetmealDto> setmealDtoPage = new Page<>();
        // 值拷贝
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        List<SetmealDto> setmealDtoList = setmealList.stream()
                .map(setmeal -> {
                    SetmealDto setmealDto = new SetmealDto();
                    BeanUtils.copyProperties(setmeal, setmealDto);
                    Category category = categoryService.getById(setmeal.getCategoryId());
                    if (category != null) {
                        setmealDto.setCategoryName(category.getName());
                    }
                    return setmealDto;
                }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);
        return R.success(setmealDtoPage);
    }

    @PostMapping
    public R save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    // 删除
    // 注：对于状态为售卖中的套餐不能删除，需要先停售，然后才能删除。
    @DeleteMapping
    public R delete(String[] ids) {
        for (String id : ids) {
            Setmeal setmeal = setmealService.getById(id);
            if (setmeal.getStatus().equals(1)) {
                return R.error("选中的套餐有启售状态，不能删除");
            } else {
                setmealService.removeById(id);
            }
        }
        return R.success("删除成功");
    }

    // 停售/起售
    @PostMapping("/status/{status}")
    public R sale(@PathVariable int status, String[] ids) {
        for (String id : ids) {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }

    // 修改套餐1 -> 根据Id查询套餐信息 -> 即回显信息
    @GetMapping("/{id}")
    public R getById(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    // 修改套餐2 -> 修改套餐
    @PutMapping
    public R update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }

    @GetMapping("/list")
    public R list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }

}















