package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.entity.dto.AddEmployeeDto;
import com.itheima.reggie.service.EmployeeService;
import com.itheima.reggie.utils.BeanCopyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工信息(Employee)表控制层
 *
 * @author makejava
 * @since 2023-03-20 11:13:56
 */
@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R login(HttpServletRequest request, @RequestBody Employee employee) {
        return employeeService.login(request, employee);
    }

    @PostMapping("/logout")
    public R logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    // 新增员工
    @PostMapping
    public R save(HttpServletRequest request, @RequestBody AddEmployeeDto addEmployeeDto) {
        Employee employee = BeanCopyUtils.copyBean(addEmployeeDto, Employee.class);
        log.info("新增员工，员工信息：{}", employee.toString());

        //设置初始密码，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 由哪一个人新增用户
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("新增员工成功！");
    }

    // 员工信息分页查询
    @GetMapping("/page")
    public R page(Integer page, Integer pageSize, String name) {
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        Page<Employee> employeePage = new Page<>(page, pageSize);
        //执行查询
        employeeService.page(employeePage, queryWrapper);
        return R.success(employeePage);
    }

    // 编辑/禁用用户
    @PutMapping
    public R update(HttpServletRequest request, @RequestBody Employee employee) {
        //long id = Thread.currentThread().getId();
        //log.info("线程id：{}", id);

        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("修改成功");
    }

    // 根据用户id获取用户信息
    @GetMapping("/{id}")
    public R getById(@PathVariable Long id) {
        Employee emp = employeeService.getById(id);
        if (emp != null) {
            return R.success(emp);
        }
        return R.error("未查到该用户信息");
    }

}






























