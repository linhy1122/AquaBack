package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_records")
public class InventoryRecord {

    @TableId(value = "record_id", type = IdType.AUTO)
    private Integer recordId;

    private Integer materialId;

    private Integer batchId;

    private String type;

    private Double quantity;

    private Double totalCost;

    private LocalDateTime recordDate;
}