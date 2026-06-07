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
@ApiModel(value = "报警概览", description = "报警中心顶部概览统计数据")
public class AlarmOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总塘口数")
    private long totalPonds;

    @ApiModelProperty(value = "已配置阈值的塘口数")
    private long configuredPonds;

    @ApiModelProperty(value = "当前报警数(未处理+处理中)")
    private long activeAlarms;

    @ApiModelProperty(value = "严重报警数")
    private long criticalAlarms;

    @ApiModelProperty(value = "预警数")
    private long warningAlarms;

    @ApiModelProperty(value = "今日新增报警数")
    private long todayAlarms;
}
