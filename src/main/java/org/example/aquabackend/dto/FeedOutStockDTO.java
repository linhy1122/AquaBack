package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "饲料出库请求", description = "饲料出库参数")
public class FeedOutStockDTO {

    @NotNull(message = "物料ID不能为空")
    @ApiModelProperty(value = "物料ID", required = true, example = "1")
    private Integer materialId;

    @NotNull(message = "出库数量不能为空")
    @Min(value = 0, message = "出库数量必须大于0")
    @ApiModelProperty(value = "出库数量(kg)", required = true, example = "200")
    private Double quantity;

    @ApiModelProperty(value = "批次ID（关联养殖批次）", example = "1")
    private Integer batchId;

    @ApiModelProperty(value = "操作人", example = "张三")
    private String operator;

    @ApiModelProperty(value = "备注", example = "1号塘投喂")
    private String remark;
}
