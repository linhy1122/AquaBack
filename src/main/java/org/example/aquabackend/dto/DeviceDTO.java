package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "设备请求", description = "设备新增/编辑请求")
public class DeviceDTO {

    @ApiModelProperty(value = "设备ID", example = "1")
    private Integer deviceId;

    @NotNull(message = "塘口ID不能为空")
    @ApiModelProperty(value = "塘口ID", required = true, example = "1")
    private Integer pondId;

    @NotBlank(message = "设备名称不能为空")
    @ApiModelProperty(value = "设备名称", required = true, example = "1号增氧机")
    private String deviceName;

    @NotBlank(message = "设备类型不能为空")
    @ApiModelProperty(value = "设备类型", required = true, example = "aerator")
    private String deviceType;

    @ApiModelProperty(value = "设备状态", example = "on")
    private String status;
}
