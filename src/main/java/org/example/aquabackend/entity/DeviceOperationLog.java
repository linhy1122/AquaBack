package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_operation_logs")
public class DeviceOperationLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Integer deviceId;

    private Integer pondId;

    private String status;

    private Double runtimeMinutes;

    private Double powerKw;

    private Double flowRate;

    private LocalDateTime recordedAt;
}
