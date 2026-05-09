package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ponds")
public class Pond {

    @TableId(value = "pond_id", type = IdType.AUTO)
    private Integer pondId;

    private String name;

    private Double area;

    private String location;

    private String status;
}