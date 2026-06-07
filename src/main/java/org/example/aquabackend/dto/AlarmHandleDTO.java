package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "报警处理DTO", description = "处理报警记录时的请求参数")
public class AlarmHandleDTO {

    @ApiModelProperty(value = "处理方式: manual/popup_ack")
    private String handleMethod;

    @ApiModelProperty(value = "处理人")
    private String handledBy;

    @ApiModelProperty(value = "备注")
    private String remark;
}
