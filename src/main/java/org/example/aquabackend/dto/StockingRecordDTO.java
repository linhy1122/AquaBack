package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@ApiModel(value = "放养记录请求", description = "新增/编辑放养记录")
public class StockingRecordDTO {

    @ApiModelProperty(value = "批次ID（编辑时必填）", example = "1")
    private Integer batchId;

    @NotNull(message = "塘口ID不能为空")
    @ApiModelProperty(value = "塘口ID", required = true, example = "1")
    private Integer pondId;

    @ApiModelProperty(value = "品种ID", example = "1")
    private Integer breedId;

    @NotNull(message = "品种不能为空")
    @ApiModelProperty(value = "品种名称", required = true, example = "南美白对虾")
    private String species;

    @NotNull(message = "放养数量不能为空")
    @Min(value = 1, message = "放养数量必须大于0")
    @ApiModelProperty(value = "放养数量", required = true, example = "10000")
    private Integer stockCount;

    @ApiModelProperty(value = "当前存活数量", example = "8500")
    private Integer currentNum;

    @ApiModelProperty(value = "平均规格（g/尾）", example = "15.5")
    private Double avgSpec;

    @DecimalMin(value = "0", message = "存活率不能小于0")
    @DecimalMax(value = "100", message = "存活率不能大于100")
    @ApiModelProperty(value = "存活率（%）0-100", example = "85.0")
    private Double survivalRate;

    @NotNull(message = "放养日期不能为空")
    @ApiModelProperty(value = "放养日期", required = true, example = "2025-06-01")
    private LocalDate stockDate;

    @ApiModelProperty(value = "状态", example = "active")
    private String status;
}
