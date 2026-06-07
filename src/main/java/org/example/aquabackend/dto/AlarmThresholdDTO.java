package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "报警阈值DTO", description = "单个报警阈值的请求参数")
public class AlarmThresholdDTO {

    @ApiModelProperty(value = "塘口ID")
    private Integer pondId;

    @ApiModelProperty(value = "指标名称: temperature/ph/dissolvedOxygen/ammoniaNitrogen/nitrite/transparency")
    private String targetParam;

    @ApiModelProperty(value = "下限值(可空)")
    private Double minValue;

    @ApiModelProperty(value = "上限值(可空)")
    private Double maxValue;

    @ApiModelProperty(value = "严重级别: warning/critical")
    private String severity;

    @ApiModelProperty(value = "是否启用: 1-启用, 0-禁用")
    private Integer enabled;
}
