package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("materials")
public class Material {

    @TableId(value = "material_id", type = IdType.AUTO)
    private Integer materialId;

    private String name;

    private String category;

    private String unit;

    private Double unitPrice;

    private Double stockQty;
}