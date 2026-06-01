package org.example.aquabackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.entity.FeedingPlan;
import org.example.aquabackend.service.FeedingPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feeding")
@Api(value = "投喂计划管理", tags = "投喂计划管理（生成/执行/取消/记录）")
public class FeedingPlanController {

    private static final Logger logger = LoggerFactory.getLogger(FeedingPlanController.class);

    @Autowired
    private FeedingPlanService feedingPlanService;

    @GetMapping("/plans")
    @ApiOperation(value = "查询投喂计划列表", notes = "纯查询接口，不自动生成；返回完整计划快照")
    @ApiImplicitParam(name = "pondId", value = "塘口ID（可选）", paramType = "query", dataTypeClass = Integer.class)
    public ApiResponse getPlans(@RequestParam(required = false) Integer pondId) {
        List<FeedingPlan> plans = feedingPlanService.getPlans(pondId, null);
        List<Map<String, Object>> result = plans.stream().map(this::toPlanMap).collect(java.util.stream.Collectors.toList());
        return ApiResponse.ok("查询成功").put("plans", result).put("total", result.size());
    }

    @PostMapping("/plans/generate")
    @ApiOperation(value = "重新生成投喂计划", notes = "遍历所有有效塘口，逐塘口生成投喂建议快照")
    public ApiResponse generatePlans() {
        List<FeedingPlan> plans = feedingPlanService.generatePlans();
        List<Map<String, Object>> result = plans.stream().map(this::toPlanMap).collect(java.util.stream.Collectors.toList());

        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("pending", 0);
        summary.put("no_batch", 0);
        summary.put("data_missing", 0);
        summary.put("no_feed", 0);
        for (FeedingPlan plan : plans) {
            summary.merge(plan.getStatus(), 1, Integer::sum);
        }

        return ApiResponse.ok("生成成功")
                .put("plans", result)
                .put("total", result.size())
                .put("generatedAt", java.time.LocalDateTime.now().toString())
                .put("summary", summary);
    }

    @PostMapping("/plans/{id}/execute")
    @ApiOperation(value = "执行单条投喂计划", notes = "扣减库存、写入投喂日志、标记计划已执行")
    @ApiImplicitParam(name = "id", value = "计划ID", required = true, paramType = "path", dataTypeClass = Integer.class)
    public ApiResponse executePlan(@PathVariable Integer id,
                                    @RequestParam(defaultValue = "系统") String operator) {
        try {
            FeedingPlan plan = feedingPlanService.executePlan(id, operator);
            return ApiResponse.ok("执行成功").put("plan", toPlanMap(plan));
        } catch (RuntimeException e) {
            logger.warn("Execute plan {} failed: {}", id, e.getMessage());
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/plans/execute-all")
    @ApiOperation(value = "批量执行所有待执行计划", notes = "依次执行所有待执行计划，库存不足时跳过")
    public ApiResponse executeAllPlans(@RequestParam(defaultValue = "系统") String operator) {
        List<FeedingPlan> executed = feedingPlanService.executeAllPlans(operator);
        return ApiResponse.ok("批量执行完成").put("executedCount", executed.size());
    }

    @PostMapping("/plans/{id}/cancel")
    @ApiOperation(value = "作废投喂计划", notes = "将指定计划标记为已作废")
    @ApiImplicitParam(name = "id", value = "计划ID", required = true, paramType = "path", dataTypeClass = Integer.class)
    public ApiResponse cancelPlan(@PathVariable Integer id) {
        try {
            FeedingPlan plan = feedingPlanService.cancelPlan(id);
            return ApiResponse.ok("已作废").put("plan", toPlanMap(plan));
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/logs")
    @ApiOperation(value = "查询投喂执行记录", notes = "返回投喂日志列表，附带塘口和饲料快照")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pondId", value = "塘口ID（可选）", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "size", value = "每页条数", defaultValue = "50", paramType = "query", dataTypeClass = Integer.class)
    })
    public ApiResponse getLogs(
            @RequestParam(required = false) Integer pondId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<Map<String, Object>> logs = feedingPlanService.getFeedingLogs(pondId, page, size);
        return ApiResponse.ok("查询成功")
                .put("records", logs)
                .put("total", logs.size())
                .put("page", page)
                .put("size", size);
    }

    private Map<String, Object> toPlanMap(FeedingPlan plan) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("planId", plan.getPlanId());
        map.put("pondId", plan.getPondId());
        map.put("pondCode", plan.getPondCode());
        map.put("pondName", plan.getPondName());
        map.put("batchId", plan.getBatchId());
        map.put("materialId", plan.getMaterialId());
        map.put("materialName", plan.getMaterialName());
        map.put("stockWeight", plan.getStockWeight());
        map.put("feedRate", plan.getFeedRate());
        map.put("suggestedAmount", plan.getSuggestedAmount());
        map.put("actualAmount", plan.getActualAmount());
        map.put("status", plan.getStatus());
        map.put("generatedAt", plan.getGeneratedAt());
        map.put("executedAt", plan.getExecutedAt());
        map.put("operator", plan.getOperator());
        map.put("calcReason", plan.getCalcReason());
        map.put("factorsJson", plan.getFactorsJson());
        return map;
    }
}
