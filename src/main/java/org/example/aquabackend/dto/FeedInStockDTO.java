package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "饲料入库请求", description = "饲料入库参数")
public class FeedInStockDTO {

    @ApiModelProperty(value = "物料ID（与物料名称二选一）", example = "1")
    private Integer materialId;

    @ApiModelProperty(value = "饲料名称（与物料ID二选一，物料不存在时自动创建）", example = "对虾配合饲料")
    private String materialName;

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
