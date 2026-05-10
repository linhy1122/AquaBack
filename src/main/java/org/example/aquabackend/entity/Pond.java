package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ponds")
public class Pond {

    @TableId(value = "pond_id", type = IdType.AUTO)
    private Integer pondId;

    /** 塘口编号 */
    private String code;

    /** 塘口名称 */
    private String name;

    /** 面积（亩） */
    private Double area;

    /** 水深（米） */
    private Double depth;

    /** 水源类型：1-地下水, 2-地表水, 3-海水 */
    private String waterSource;

    /** 位置/地址 */
    private String location;

    /** 状态：1-使用中, 2-空闲, 3-维修 */
    private String status;

    /** 负责人 */
    private String manager;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 软删除标记：0-正常, 1-删除 */
    private Integer deleted;
}