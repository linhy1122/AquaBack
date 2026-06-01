package org.example.aquabackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.service.FeedingStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/feeding")
@Api(value = "投喂统计", tags = "投喂统计（今日/本月/塘口维度）")
public class FeedingStatsController {

    private static final Logger logger = LoggerFactory.getLogger(FeedingStatsController.class);

    @Autowired
    private FeedingStatsService feedingStatsService;

    @GetMapping("/stats")
    @ApiOperation(value = "获取投喂统计数据", notes = "返回今日投喂量、本月投喂量、各塘口投喂统计等")
    public ApiResponse getStats() {
        Map<String, Object> stats = feedingStatsService.getStats();
        return ApiResponse.ok("查询成功").put("stats", stats);
    }
}
