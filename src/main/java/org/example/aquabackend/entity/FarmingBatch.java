package org.example.aquabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("stocking_record")
public class FarmingBatch {

        @TableId(value = "id", type = IdType.AUTO)
    private Integer batchId;

    /** 塘口ID */
    private Integer pondId;

    /** 品种ID */
    private Integer breedId;

    /** 品种名称（冗余字段） */
    private String species;

    /** 放养数量 */
    private Integer stockCount;

    /** 当前存活数量 */
    private Integer currentNum;

    /** 平均规格（g/尾） */
    private Double avgSpec;

    /** 存活率（%）0-100 */
    private Double survivalRate;

    /** 放养日期 */
    private LocalDate stockDate;

    /** 状态：active-正常, deleted-删除 */
    private String status;
}