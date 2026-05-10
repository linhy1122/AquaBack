package org.example.aquabackend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockingStatisticVO {

    /** 总放养量 */
    private Long totalStockingCount;

    /** 当前存活总量 */
    private Long totalCurrentNum;

    /** 平均存活率（%） */
    private Double avgSurvivalRate;

    /** 总放养重量（kg）= SUM(stock_count * avg_spec / 1000) */
    private Double totalWeight;
}
