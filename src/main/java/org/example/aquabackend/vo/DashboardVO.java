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
@ApiModel(value = "数据概览", description = "首页顶部4个卡片统计数据")
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "存储总量（放养总数量）", example = "50000")
    private Long totalStock;

    @ApiModelProperty(value = "较上月增长率（%）", example = "12.5")
    private Double stockGrowthRate;

    @ApiModelProperty(value = "饲料总库存（kg）", example = "2500.00")
    private Double feedStockKg;

    @ApiModelProperty(value = "饲料可用天数", example = "15")
    private Integer feedAvailableDays;

    @ApiModelProperty(value = "本月消耗饲料（kg）", example = "5000.00")
    private Double monthlyFeedConsumed;
}
