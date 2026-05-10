package org.example.aquabackend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PondStatisticVO {

    /** 塘口总数 */
    private Long totalCount;

    /** 使用中塘口数 */
    private Long inUseCount;

    /** 空闲塘口数 */
    private Long idleCount;

    /** 总养殖规模（亩） */
    private Double totalArea;
}
