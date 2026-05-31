package org.example.aquabackend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "设备快照", description = "设备当前运行状态")
public class DeviceSnapshotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("设备ID")
    private Integer deviceId;

    @ApiModelProperty("塘口ID")
    private Integer pondId;

    @ApiModelProperty("塘口名称")
    private String pondName;

    @ApiModelProperty("设备名称")
    private String deviceName;

    @ApiModelProperty("设备类型")
    private String deviceType;

    @ApiModelProperty("设备状态")
    private String status;

    @ApiModelProperty("运行时长（分钟）")
    private Double runtimeMinutes;

    @ApiModelProperty("功率（kW）")
    private Double powerKw;

    @ApiModelProperty("流量/投喂量")
    private Double flowRate;

    @ApiModelProperty("最后心跳时间")
    private LocalDateTime lastHeartbeat;

    @ApiModelProperty("更新时间")
    private LocalDateTime lastUpdate;
}
