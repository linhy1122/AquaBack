package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm_settings")
public class AlarmSetting {

    @TableId(value = "setting_id", type = IdType.AUTO)
    private Integer settingId;

    private Integer pondId;

    private Integer popupEnabled;

    private Integer soundEnabled;

    private Integer badgeEnabled;

    private String quietStart;

    private String quietEnd;

    private Integer repeatIntervalMinutes;

    private LocalDateTime updatedAt;
}
