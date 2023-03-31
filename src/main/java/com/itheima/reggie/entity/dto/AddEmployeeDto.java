package com.itheima.reggie.entity.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddEmployeeDto {
    private String username;
    private String name;
    private String phone;
    private String sex;
    private String idNumber; // 身份证号码
}
