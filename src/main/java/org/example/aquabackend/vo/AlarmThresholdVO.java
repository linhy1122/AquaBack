package org.example.aquabackend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "报警阈值VO", description = "包含当前水质值的阈值展示")
public class AlarmThresholdVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "阈值ID")
    private Integer thresholdId;

    @ApiModelProperty(value = "塘口ID")
    private Integer pondId;

    @ApiModelProperty(value = "指标名称")
    private String targetParam;

    @ApiModelProperty(value = "下限值")
    private Double minValue;

    @ApiModelProperty(value = "上限值")
    private Double maxValue;

    @ApiModelProperty(value = "当前水质值")
    private Double currentValue;

    @ApiModelProperty(value = "状态: normal/warning/critical")
    private String status;

    @ApiModelProperty(value = "严重级别: warning/critical")
    private String severity;

    @ApiModelProperty(value = "是否启用")
    private Integer enabled;
}
