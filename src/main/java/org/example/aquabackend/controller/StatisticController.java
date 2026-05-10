package org.example.aquabackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.service.StatisticService;
import org.example.aquabackend.vo.PondStatisticVO;
import org.example.aquabackend.vo.StockingStatisticVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistic")
@Api(value = "统计数据", tags = "塘口/放养统计数据")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/pond")
    @ApiOperation(value = "塘口统计数据", notes = "返回塘口总数、使用中数、空闲数、总养殖规模")
    public ApiResponse getPondStatistic() {
        PondStatisticVO vo = statisticService.getPondStatistic();
        return ApiResponse.ok("查询成功")
                .put("totalCount", vo.getTotalCount())
                .put("inUseCount", vo.getInUseCount())
                .put("idleCount", vo.getIdleCount())
                .put("totalArea", vo.getTotalArea());
    }

    @GetMapping("/stocking")
    @ApiOperation(value = "放养统计数据", notes = "返回总放养量、当前存活量、平均存活率、总放养重量")
    public ApiResponse getStockingStatistic() {
        StockingStatisticVO vo = statisticService.getStockingStatistic();
        return ApiResponse.ok("查询成功")
                .put("totalStockingCount", vo.getTotalStockingCount())
                .put("totalCurrentNum", vo.getTotalCurrentNum())
                .put("avgSurvivalRate", vo.getAvgSurvivalRate())
                .put("totalWeight", vo.getTotalWeight());
    }
}
