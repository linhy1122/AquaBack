package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 管理员创建用户请求 DTO
 */
@ApiModel(value = "创建用户请求", description = "管理员创建新用户所需的参数（角色默认为 USER，由服务端强制设定）")
public class CreateUserRequest {

    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan", position = 1)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度应为3-50个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @ApiModelProperty(value = "密码", required = true, example = "password123", position = 2)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度应为6-100个字符")
    private String password;

    @ApiModelProperty(value = "邮箱", required = true, example = "zhangsan@example.com", position = 3)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}