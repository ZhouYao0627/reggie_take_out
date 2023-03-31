package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;

import javax.servlet.http.HttpServletRequest;


/**
 * 员工信息(Employee)表服务接口
 *
 * @author makejava
 * @since 2023-03-20 11:13:56
 */
public interface EmployeeService extends IService<Employee> {

    R login(HttpServletRequest request, Employee employee);
}
