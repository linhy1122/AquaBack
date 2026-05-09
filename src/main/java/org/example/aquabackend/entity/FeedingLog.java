package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feeding_logs")
public class FeedingLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Integer logId;

    private Integer batchId;

    private Integer feedTypeId;

    private Double amount;

    private LocalDateTime feedTime;
}