package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm_logs")
public class AlarmLog {

    @TableId(value = "alarm_id", type = IdType.AUTO)
    private Integer alarmId;

    private Integer pondId;

    private String alarmItem;

    private String alarmValue;

    private Double currentValue;

    private Double thresholdMin;

    private Double thresholdMax;

    private String severity;

    private String status;

    private String handleMethod;

    private String handledBy;

    private LocalDateTime handledAt;

    private Integer triggerCount;

    private LocalDateTime lastTriggeredAt;

    private String remark;

    private LocalDateTime createdAt;
}
