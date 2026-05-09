package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("devices")
public class Device {

    @TableId(value = "device_id", type = IdType.AUTO)
    private Integer deviceId;

    private Integer pondId;

    private String deviceName;

    private String deviceType;

    private String status;

    private LocalDateTime lastUpdate;
}