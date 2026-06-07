package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "报警设置DTO", description = "报警方式设置的请求参数")
public class AlarmSettingDTO {

    @ApiModelProperty(value = "塘口ID")
    private Integer pondId;

    @ApiModelProperty(value = "页面弹窗: 1-启用, 0-禁用")
    private Integer popupEnabled;

    @ApiModelProperty(value = "声音提醒: 1-启用, 0-禁用")
    private Integer soundEnabled;

    @ApiModelProperty(value = "角标提醒: 1-启用, 0-禁用")
    private Integer badgeEnabled;

    @ApiModelProperty(value = "静默时段开始 HH:mm")
    private String quietStart;

    @ApiModelProperty(value = "静默时段结束 HH:mm")
    private String quietEnd;

    @ApiModelProperty(value = "重复提醒间隔(分钟), 0不重复")
    private Integer repeatIntervalMinutes;
}
