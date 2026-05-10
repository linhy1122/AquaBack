package org.example.aquabackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.service.DashboardService;
import org.example.aquabackend.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Api(value = "数据概览", tags = "数据概览（首页顶部4个卡片）")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    @ApiOperation(value = "获取数据概览", notes = "返回存储总量、增长率、饲料库存、可用天数、本月消耗（直接数据库查询）")
    public ApiResponse getSummary() {
        DashboardVO vo = dashboardService.getSummary();
        return ApiResponse.ok("查询成功")
                .put("totalStock", vo.getTotalStock())
                .put("stockGrowthRate", vo.getStockGrowthRate())
                .put("feedStockKg", vo.getFeedStockKg())
                .put("feedAvailableDays", vo.getFeedAvailableDays())
                .put("monthlyFeedConsumed", vo.getMonthlyFeedConsumed());
    }
}
