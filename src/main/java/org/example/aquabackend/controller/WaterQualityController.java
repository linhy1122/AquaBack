package org.example.aquabackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.service.WaterQualityService;
import org.example.aquabackend.vo.WaterQualitySnapshotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/water-quality")
@Api(value = "水质监测", tags = "水质监测实时数据")
public class WaterQualityController {

    @Autowired
    private WaterQualityService waterQualityService;

    @GetMapping("/latest")
    @ApiOperation(value = "获取所有塘口最新水质数据", notes = "返回每个塘口最新一条模拟水质记录")
    public ApiResponse getLatestSnapshots() {
        List<WaterQualitySnapshotVO> records = waterQualityService.getLatestSnapshots();
        return ApiResponse.ok("查询成功")
                .put("records", records)
                .put("updatedAt", LocalDateTime.now());
    }

    @GetMapping("/history/{pondId}")
    @ApiOperation(value = "获取塘口最近水质历史", notes = "按时间倒序返回指定塘口最近若干条模拟记录")
    public ApiResponse getRecentHistory(
            @ApiParam("塘口ID") @PathVariable Integer pondId,
            @ApiParam("返回条数") @RequestParam(defaultValue = "10") Integer limit) {
        List<WaterQualitySnapshotVO> records = waterQualityService.getRecentHistory(pondId, limit);
        return ApiResponse.ok("查询成功")
                .put("records", records)
                .put("updatedAt", LocalDateTime.now());
    }
}
