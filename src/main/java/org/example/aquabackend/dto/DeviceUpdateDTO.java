package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "设备更新请求", description = "设备编辑请求")
public class DeviceUpdateDTO {

    @NotNull(message = "设备ID不能为空")
    @ApiModelProperty(value = "设备ID", required = true, example = "1")
    private Integer deviceId;

    @NotNull(message = "塘口ID不能为空")
    @ApiModelProperty(value = "塘口ID", required = true, example = "1")
    private Integer pondId;

    @ApiModelProperty(value = "设备名称", example = "1号增氧机")
    private String deviceName;

    @ApiModelProperty(value = "设备类型", example = "aerator")
    private String deviceType;

    @ApiModelProperty(value = "设备状态", example = "on")
    private String status;
}
