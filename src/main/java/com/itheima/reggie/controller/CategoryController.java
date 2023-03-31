package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 菜品及套餐分类(Category)表控制层
 *
 * @author makejava
 * @since 2023-03-21 12:12:02
 */
@RestController
@Slf4j
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 新增菜单
    @PostMapping
    public R save(@RequestBody Category category) {
        log.info("新增菜单...");
        categoryService.save(category);
        return R.success("新增菜单..");
    }

    // 分页查询
    @GetMapping("/page")
    public R list(Integer page, Integer pageSize) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);

        Page<Category> categoryPage = new Page<>(page, pageSize);
        categoryService.page(categoryPage, queryWrapper);
        return R.success(categoryPage);
    }

    // 删除分类
    @DeleteMapping
    public R delete(Long ids) {
        log.info("删除分类，id为{}", ids);
        //categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    // 修改分类
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("分类修改成功");
    }

    // 菜品页面下拉列表
    @GetMapping("/list")
    public R list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByAsc(Category::getUpdateTime);

        List<Category> categoryList = categoryService.list(queryWrapper);
        return R.success(categoryList);
    }

}








