package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feeding_plans")
public class FeedingPlan {

    @TableId(value = "plan_id", type = IdType.AUTO)
    private Integer planId;

    private Integer pondId;

    private String pondCode;

    private String pondName;

    private Integer batchId;

    private Integer materialId;

    private String materialName;

    private Double stockWeight;

    private Double feedRate;

    private Double suggestedAmount;

    private Double actualAmount;

    private String status;

    private LocalDateTime generatedAt;

    private LocalDateTime executedAt;

    private String operator;

    private String calcReason;

    private String factorsJson;
}
