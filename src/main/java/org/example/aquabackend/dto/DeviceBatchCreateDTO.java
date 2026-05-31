package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value = "批量设备请求", description = "同一塘口批量新增设备")
public class DeviceBatchCreateDTO {

    @NotNull(message = "塘口ID不能为空")
    @ApiModelProperty(value = "塘口ID", required = true, example = "1")
    private Integer pondId;

    @Valid
    @NotEmpty(message = "批量新增列表不能为空")
    @ApiModelProperty(value = "设备列表", required = true)
    private List<DeviceDTO> devices;
}
