package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("extra_costs")
public class ExtraCost {

    @TableId(value = "cost_id", type = IdType.AUTO)
    private Integer costId;

    private Integer batchId;

    private String itemName;

    private Double amount;

    private LocalDate recordDate;
}