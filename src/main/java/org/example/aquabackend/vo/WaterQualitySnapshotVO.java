package org.example.aquabackend.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "水质快照", description = "塘口最新水质数据")
public class WaterQualitySnapshotVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "塘口ID")
    private Integer pondId;

    @ApiModelProperty(value = "塘口名称")
    private String pondName;

    @ApiModelProperty(value = "水温")
    private Double temperature;

    @ApiModelProperty(value = "pH值")
    private Double phValue;

    @ApiModelProperty(value = "溶解氧")
    private Double dissolvedOxygen;

    @ApiModelProperty(value = "氨氮")
    private Double ammoniaNitrogen;

    @ApiModelProperty(value = "亚硝酸盐")
    private Double nitrite;

    @ApiModelProperty(value = "透明度")
    private Double transparency;

    @ApiModelProperty(value = "记录时间")
    private LocalDateTime recordedAt;
}
