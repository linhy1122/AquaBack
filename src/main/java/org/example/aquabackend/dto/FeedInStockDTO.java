package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "饲料入库请求", description = "饲料入库参数")
public class FeedInStockDTO {

    @NotNull(message = "物料ID不能为空")
    @ApiModelProperty(value = "物料ID", required = true, example = "1")
    private Integer materialId;

    @NotNull(message = "入库数量不能为空")
    @Min(value = 0, message = "入库数量必须大于0")
    @ApiModelProperty(value = "入库数量(kg)", required = true, example = "500")
    private Double quantity;

    @ApiModelProperty(value = "单价(元/kg)", example = "150.00")
    private Double unitPrice;

    @ApiModelProperty(value = "操作人", example = "张三")
    private String operator;

    @ApiModelProperty(value = "备注", example = "新批次饲料到货")
    private String remark;
}
