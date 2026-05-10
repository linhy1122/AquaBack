package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一 API 响应包装类
 */
@ApiModel(value = "统一响应", description = "所有 API 的统一响应格式")
public class ApiResponse {

    @ApiModelProperty(value = "是否成功", required = true, example = "true", position = 1)
    private boolean success;

    @ApiModelProperty(value = "提示信息", example = "操作成功", position = 2)
    private String message;

    @ApiModelProperty(value = "响应数据", position = 3)
    private Map<String, Object> data;

    public ApiResponse() {
        this.data = new HashMap<>();
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = new HashMap<>();
    }

    public static ApiResponse ok(String message) {
        return new ApiResponse(true, message);
    }

    public static ApiResponse fail(String message) {
        return new ApiResponse(false, message);
    }

    public ApiResponse put(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
