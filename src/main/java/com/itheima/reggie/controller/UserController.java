package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息(User)表控制层
 *
 * @author makejava
 * @since 2023-03-23 15:17:50
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("sendMsg")
    public R sendMsg(@RequestBody User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.hasText(phone)) {
            // 生成随机的 4 位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            // 调用阿里云提供的短信服务 API 完成发送短信
            // ......

            // 需要将生成的验证码保存到 Session
            //session.setAttribute(phone, code);
            // 将随机生成的验证码缓存到Redis中，并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机短信发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        //String code = map.get("code").toString();

        // 从Session中获取保存的验证码
        //Object codeSession = session.getAttribute(phone);
        // 从redis中获取保存的验证码
        Object codeSession = redisTemplate.opsForValue().get(phone);

        // 进行验证码比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeSession != null) {
            // 如果能够比对成功，说明登录成功
            // 需判断该用户是否已在数据库中存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            // 若不存在，则为该用户创建登录
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            // 若存在则直接返回相关数据
            session.setAttribute("user", user.getId());

            // 如果用户登录成功则删除Redis中缓存的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登陆失败");
    }


}

















