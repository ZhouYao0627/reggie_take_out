package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品及套餐分类(Category)表服务实现类
 *
 * @author makejava
 * @since 2023-03-21 12:12:02
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;

    @Override
    public void remove(Long id) {
        // 需要检查想要删除的分类是否关联了菜品或套餐
        // 若直接比较不同分类列表中的数据是否相同，复杂度太高
        // 菜品或套餐的 categoryId 如果和 categoriy 类中的 id 相同则不能删除

        // 是否关联了菜品
        LambdaQueryWrapper<Dish> queryWrapperDish = new LambdaQueryWrapper<>();
        queryWrapperDish.eq(Dish::getStatus, 1);
        queryWrapperDish.eq(Dish::getCategoryId, id);
        int countDish = dishService.count(queryWrapperDish);
        if (countDish > 0) {
            throw new CustomException("已经关联菜品，不能删除");
        }

        // 是否关联了套餐
        LambdaQueryWrapper<Setmeal> queryWrapperSet = new LambdaQueryWrapper<>();
        queryWrapperSet.eq(Setmeal::getStatus, 1);
        queryWrapperSet.eq(Setmeal::getCategoryId, id);
        int countSet = setmealService.count(queryWrapperSet);
        if (countSet > 0) {
            throw new CustomException("已经关联套餐，不能删除");
        }

        // 若未关联任何菜品或套餐
        categoryService.removeById(id);
    }
}
