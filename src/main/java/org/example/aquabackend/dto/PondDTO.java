package org.example.aquabackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "塘口请求", description = "新增/编辑塘口的请求参数")
public class PondDTO {

    @ApiModelProperty(value = "塘口ID（编辑时必填）", example = "1")
    private Integer pondId;

    @NotBlank(message = "塘口编号不能为空")
    @ApiModelProperty(value = "塘口编号", required = true, example = "P001")
    private String code;

    @NotBlank(message = "塘口名称不能为空")
    @ApiModelProperty(value = "塘口名称", required = true, example = "1号南美白对虾塘")
    private String name;

    @NotNull(message = "面积不能为空")
    @ApiModelProperty(value = "面积（亩）", required = true, example = "5.5")
    private Double area;

    @ApiModelProperty(value = "水深（米）", example = "1.8")
    private Double depth;

    @ApiModelProperty(value = "水源类型：1-地下水, 2-地表水, 3-海水", example = "1")
    private String waterSource;

    @ApiModelProperty(value = "位置/地址", example = "A区3号")
    private String location;

    @ApiModelProperty(value = "状态：1-使用中, 2-空闲, 3-维修", example = "1")
    private String status;

    @ApiModelProperty(value = "负责人", example = "张三")
    private String manager;
}
