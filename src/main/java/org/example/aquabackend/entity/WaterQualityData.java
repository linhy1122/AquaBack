package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("water_quality_data")
public class WaterQualityData {

    @TableId(value = "data_id", type = IdType.AUTO)
    private Long dataId;

    private Integer pondId;

    private Double temperature;

    private Double phValue;

    private Double dissolvedOxygen;

    private Double ammoniaNitrogen;

    private Double nitrite;

    private Double transparency;

    private LocalDateTime recordedAt;
}
