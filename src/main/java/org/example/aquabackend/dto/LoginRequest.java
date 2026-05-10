package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录请求 DTO
 */
@ApiModel(value = "登录请求", description = "用户登录所需的用户名、密码和验证码")
public class LoginRequest {

    @ApiModelProperty(value = "用户名", required = true, example = "admin", position = 1)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度应为3-50个字符")
    private String username;

    @ApiModelProperty(value = "密码", required = true, example = "admin123", position = 2)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;

    @ApiModelProperty(value = "验证码（可选）", example = "A1B2", position = 3)
    private String captcha;

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

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
