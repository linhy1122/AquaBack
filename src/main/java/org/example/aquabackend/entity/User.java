package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private String username;

    private String passwordHash;

    private String role;

    private String phone;

    private LocalDateTime createdAt;
}