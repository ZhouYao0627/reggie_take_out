package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.entity.vo.LoginVo;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import com.itheima.reggie.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 员工信息(Employee)表服务实现类
 *
 * @author makejava
 * @since 2023-03-20 11:13:56
 */
@Service("employeeService")
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeService employeeService;

    @Override
    public R login(HttpServletRequest request, Employee employee) {
        // 1、将页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 2、根据页面提交的用户名来查数据库
        String username = employee.getUsername();
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, username);
        // 3、如果没有查询到则返回失败结果
        Employee emp = employeeService.getOne(queryWrapper);
        if (emp == null) {
            return R.error("用户名错误！");
        }
        // 4、比对密码，如果不一致则返回失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误！");
        }
        // 5、查看员工状态，如果已禁用状态，则返回员工已禁用结果
        if (emp.getStatus().equals(0)) {
            return R.error("此用户已被禁用!");
        }
        // 6、登录成功，将用户id存入Session并返回成功结果
        request.getSession().setAttribute("employee", emp.getId());

        // ps1：需要将信息转换成 Vo 返回
        LoginVo loginVo = BeanCopyUtils.copyBean(employee, LoginVo.class);
        return R.success(loginVo);
    }
}
















