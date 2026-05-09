package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("farming_batches")
public class FarmingBatch {

    @TableId(value = "batch_id", type = IdType.AUTO)
    private Integer batchId;

    private Integer pondId;

    private String species;

    private Integer stockCount;

    private LocalDate stockDate;

    private String status;
}