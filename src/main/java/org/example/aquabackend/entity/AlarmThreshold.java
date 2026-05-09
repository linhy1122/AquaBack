package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("alarm_thresholds")
public class AlarmThreshold {

    @TableId(value = "threshold_id", type = IdType.AUTO)
    private Integer thresholdId;

    private Integer pondId;

    private String targetParam;

    private Double minValue;

    private Double maxValue;
}