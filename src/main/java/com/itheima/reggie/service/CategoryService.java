package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;


/**
 * 菜品及套餐分类(Category)表服务接口
 *
 * @author makejava
 * @since 2023-03-21 12:12:02
 */
public interface CategoryService extends IService<Category> {

    void remove(Long id);
}
