package com.itheima.reggie.utils;

import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.entity.vo.LoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BeanCopyUtils {
    public static <V> V copyBean(Object source, Class<V> clazz) {
        V result = null;
        try {
            // 创建目标对象
            result = clazz.newInstance();
            // 实现属性copy
            BeanUtils.copyProperties(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <O, V> List<V> copyBeanList(List<O> list, Class<V> clazz) {
        return list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
    }

}
