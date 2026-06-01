package org.example.aquabackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/report")
@Api(value = "报表中心", tags = "日报/月报/分析报表")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/daily")
    @ApiOperation(value = "获取日报", notes = "按日期+塘口获取当天经营快照")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "date", value = "日期 YYYY-MM-DD", paramType = "query", dataTypeClass = String.class),
        @ApiImplicitParam(name = "pondId", value = "塘口ID（可选）", paramType = "query", dataTypeClass = Integer.class)
    })
    public ApiResponse getDaily(@RequestParam(required = false) String date,
                                 @RequestParam(required = false) Integer pondId) {
        Map<String, Object> report = reportService.getDailyReport(date, pondId);
        return ApiResponse.ok("查询成功").put("report", report);
    }

    @GetMapping("/monthly")
    @ApiOperation(value = "获取月报", notes = "按月份+塘口获取月度趋势和排行")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "month", value = "月份 YYYY-MM", paramType = "query", dataTypeClass = String.class),
        @ApiImplicitParam(name = "pondId", value = "塘口ID（可选）", paramType = "query", dataTypeClass = Integer.class)
    })
    public ApiResponse getMonthly(@RequestParam(required = false) String month,
                                   @RequestParam(required = false) Integer pondId) {
        Map<String, Object> report = reportService.getMonthlyReport(month, pondId);
        return ApiResponse.ok("查询成功").put("report", report);
    }

    @GetMapping("/analysis")
    @ApiOperation(value = "获取分析报表", notes = "按时间范围+塘口获取多维度分析数据")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "range", value = "时间范围: 30d/90d", paramType = "query", dataTypeClass = String.class),
        @ApiImplicitParam(name = "pondId", value = "塘口ID（可选）", paramType = "query", dataTypeClass = Integer.class)
    })
    public ApiResponse getAnalysis(@RequestParam(defaultValue = "30d") String range,
                                    @RequestParam(required = false) Integer pondId) {
        Map<String, Object> report = reportService.getAnalysisReport(range, pondId);
        return ApiResponse.ok("查询成功").put("report", report);
    }
}
