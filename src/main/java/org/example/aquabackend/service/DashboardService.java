package org.example.aquabackend.service;

import org.example.aquabackend.vo.DashboardVO;

public interface DashboardService {

    /**
     * 获取数据概览（每次直接查询数据库）
     */
    DashboardVO getSummary();
}
