package org.example.aquabackend.service;

import org.example.aquabackend.vo.PondStatisticVO;
import org.example.aquabackend.vo.StockingStatisticVO;

public interface StatisticService {

    /**
     * 塘口统计数据
     */
    PondStatisticVO getPondStatistic();

    /**
     * 放养统计数据
     */
    StockingStatisticVO getStockingStatistic();
}
