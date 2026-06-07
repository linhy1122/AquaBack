package org.example.aquabackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.aquabackend.dto.AlarmHandleDTO;
import org.example.aquabackend.dto.AlarmSettingDTO;
import org.example.aquabackend.dto.AlarmThresholdDTO;
import org.example.aquabackend.entity.AlarmLog;
import org.example.aquabackend.entity.AlarmSetting;
import org.example.aquabackend.service.AlarmLogService;
import org.example.aquabackend.service.AlarmSettingService;
import org.example.aquabackend.service.AlarmThresholdService;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.vo.AlarmOverviewVO;
import org.example.aquabackend.vo.AlarmThresholdVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alarm")
@Api(value = "报警管理", tags = "报警管理中心（阈值/记录/设置）")
public class AlarmController {

    @Autowired
    private AlarmThresholdService alarmThresholdService;

    @Autowired
    private AlarmLogService alarmLogService;

    @Autowired
    private AlarmSettingService alarmSettingService;

    // ====== 概览 ======

    @GetMapping("/overview")
    @ApiOperation(value = "报警概览", notes = "报警中心顶部概览统计数据")
    public ApiResponse getOverview() {
        AlarmOverviewVO overview = alarmThresholdService.getOverview();
        return ApiResponse.ok("查询成功").put("overview", overview);
    }

    // ====== 阈值 ======

    @GetMapping("/thresholds")
    @ApiOperation(value = "获取阈值列表（含当前水质值）")
    public ApiResponse getThresholds(
            @ApiParam("塘口ID, 不传则返回所有") @RequestParam(required = false) Integer pondId) {
        List<AlarmThresholdVO> list = alarmThresholdService.getThresholdsWithCurrent(pondId);
        return ApiResponse.ok("查询成功").put("records", list);
    }

    @PutMapping("/thresholds/batch")
    @ApiOperation(value = "批量保存阈值", notes = "按塘口+指标 upsert，已存在的更新，不存在的插入")
    public ApiResponse batchSaveThresholds(@RequestBody List<AlarmThresholdDTO> list) {
        alarmThresholdService.batchSave(list);
        return ApiResponse.ok("保存成功");
    }

    // ====== 记录 ======

    @GetMapping("/records")
    @ApiOperation(value = "分页查询报警记录")
    public ApiResponse getRecords(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "20") int size,
            @ApiParam("塘口ID") @RequestParam(required = false) Integer pondId,
            @ApiParam("状态: unhandled/processing/handled/auto_recovered") @RequestParam(required = false) String status,
            @ApiParam("严重级别: warning/critical") @RequestParam(required = false) String severity) {

        IPage<Map<String, Object>> result = alarmLogService.getRecords(page, size, pondId, status, severity);
        return ApiResponse.ok("查询成功")
                .put("records", result.getRecords())
                .put("total", result.getTotal())
                .put("page", result.getCurrent())
                .put("size", result.getSize());
    }

    @GetMapping("/records/recent")
    @ApiOperation(value = "获取最近报警记录", notes = "供仪表盘等组件使用")
    public ApiResponse getRecentRecords(@ApiParam("条数") @RequestParam(defaultValue = "5") int limit) {
        List<AlarmLog> list = alarmLogService.getRecentAlarms(limit);
        return ApiResponse.ok("查询成功").put("records", list);
    }

    @PostMapping("/records/{id}/handle")
    @ApiOperation(value = "处理报警", notes = "将报警标记为已处理")
    public ApiResponse handleAlarm(
            @ApiParam("报警ID") @PathVariable Integer id,
            @RequestBody AlarmHandleDTO dto) {
        AlarmLog log = alarmLogService.handleAlarm(id, dto);
        return ApiResponse.ok("处理成功").put("alarmId", log.getAlarmId());
    }

    // ====== 设置 ======

    @GetMapping("/settings")
    @ApiOperation(value = "获取报警方式设置")
    public ApiResponse getSettings(@ApiParam("塘口ID, 不传返回所有") @RequestParam(required = false) Integer pondId) {
        if (pondId != null) {
            AlarmSetting setting = alarmSettingService.getByPondId(pondId);
            return ApiResponse.ok("查询成功").put("records", setting != null ? List.of(setting) : List.of());
        }
        List<AlarmSetting> list = alarmSettingService.getAll();
        return ApiResponse.ok("查询成功").put("records", list);
    }

    @PutMapping("/settings")
    @ApiOperation(value = "保存报警方式设置", notes = "按塘口 upsert")
    public ApiResponse saveSettings(@RequestBody AlarmSettingDTO dto) {
        alarmSettingService.save(dto);
        return ApiResponse.ok("保存成功");
    }
}
