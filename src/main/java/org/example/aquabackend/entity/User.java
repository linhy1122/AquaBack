package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
@ApiModel(value = "用户实体", description = "系统用户信息")
public class User {

    @ApiModelProperty(value = "用户ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户名", example = "admin")
    private String username;

    @ApiModelProperty(value = "密码（BCrypt加密存储）", example = "$2a$10$...")
    private String password;

    @ApiModelProperty(value = "邮箱", example = "admin@aqua.com")
    private String email;

    @ApiModelProperty(value = "用户角色", allowableValues = "ADMIN, USER, MANAGER", example = "USER")
    private String role = "USER";

    @ApiModelProperty(value = "账户是否启用", example = "true")
    private Boolean enabled = true;

    @ApiModelProperty(value = "账户是否锁定", example = "false")
    private Boolean accountLocked = false;

    @ApiModelProperty(value = "账户是否过期", example = "false")
    private Boolean accountExpired = false;

    @ApiModelProperty(value = "凭据是否过期", example = "false")
    private Boolean credentialsExpired = false;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "逻辑删除标记", example = "0")
    @TableLogic
    private Integer deleted = 0;
}
