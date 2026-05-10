package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("breeds")
public class Breed {

    @TableId(value = "breed_id", type = IdType.AUTO)
    private Integer breedId;

    /** 品种名称，如 南美白对虾、河蟹 */
    private String name;

    /** 品种类别 */
    private String category;

    /** 默认平均规格（g/尾） */
    private Double defaultSpec;

    /** 备注 */
    private String remark;

    /** 软删除：0-正常, 1-删除 */
    private Integer deleted;
}
