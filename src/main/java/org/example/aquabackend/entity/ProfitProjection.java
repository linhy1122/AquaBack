package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("profit_projections")
public class ProfitProjection {

    @TableId(value = "projection_id", type = IdType.AUTO)
    private Integer projectionId;

    private Integer batchId;

    private Double targetPrice;

    private Double expSurvivalRate;

    private Double expAvgWeight;
}