package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aquabackend.dto.AlarmHandleDTO;
import org.example.aquabackend.entity.AlarmLog;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.mapper.AlarmLogMapper;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.service.AlarmLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlarmLogServiceImpl implements AlarmLogService {

    private static final Logger logger = LoggerFactory.getLogger(AlarmLogServiceImpl.class);

    @Autowired
    private AlarmLogMapper alarmLogMapper;

    @Autowired
    private PondMapper pondMapper;

    @Override
    public IPage<Map<String, Object>> getRecords(int page, int size, Integer pondId, String status, String severity) {
        QueryWrapper<AlarmLog> qw = new QueryWrapper<>();
        if (pondId != null) qw.eq("pond_id", pondId);
        if (StringUtils.hasText(status)) qw.eq("status", status);
        if (StringUtils.hasText(severity)) qw.eq("severity", severity);
        qw.orderByDesc("created_at");

        Page<AlarmLog> alarmPage = new Page<>(page, size);
        IPage<AlarmLog> result = alarmLogMapper.selectPage(alarmPage, qw);

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(result.getRecords().stream().map(this::toMap).collect(Collectors.toList()));
        return resultPage;
    }

    @Override
    public AlarmLog getById(Integer alarmId) {
        return alarmLogMapper.selectById(alarmId);
    }

    @Override
    public AlarmLog handleAlarm(Integer alarmId, AlarmHandleDTO dto) {
        AlarmLog log = alarmLogMapper.selectById(alarmId);
        if (log == null) {
            throw new IllegalArgumentException("报警记录不存在: " + alarmId);
        }
        log.setStatus("handled");
        log.setHandleMethod(dto.getHandleMethod() != null ? dto.getHandleMethod() : "manual");
        log.setHandledBy(dto.getHandledBy());
        log.setHandledAt(LocalDateTime.now());
        log.setRemark(dto.getRemark());
        alarmLogMapper.updateById(log);
        logger.info("Handled alarm: alarmId={}, method={}, by={}", alarmId, log.getHandleMethod(), log.getHandledBy());
        return log;
    }

    @Override
    public long countActiveAlarms() {
        return alarmLogMapper.selectCount(
                new QueryWrapper<AlarmLog>().in("status", "unhandled", "processing")
        );
    }

    @Override
    public long countBySeverity(String severity) {
        return alarmLogMapper.selectCount(
                new QueryWrapper<AlarmLog>()
                        .eq("severity", severity)
                        .in("status", "unhandled", "processing")
        );
    }

    @Override
    public long countTodayAlarms() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        return alarmLogMapper.selectCount(
                new QueryWrapper<AlarmLog>().ge("created_at", todayStart)
        );
    }

    @Override
    public List<AlarmLog> getRecentAlarms(int limit) {
        return alarmLogMapper.selectList(
                new QueryWrapper<AlarmLog>()
                        .orderByDesc("created_at")
                        .last("LIMIT " + limit)
        );
    }

    private Map<String, Object> toMap(AlarmLog log) {
        Pond pond = pondMapper.selectById(log.getPondId());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("alarmId", log.getAlarmId());
        map.put("pondId", log.getPondId());
        map.put("pondCode", pond != null ? pond.getCode() : null);
        map.put("pondName", pond != null ? pond.getName() : "未知塘口");
        map.put("alarmItem", log.getAlarmItem());
        map.put("alarmValue", log.getAlarmValue());
        map.put("currentValue", log.getCurrentValue());
        map.put("thresholdMin", log.getThresholdMin());
        map.put("thresholdMax", log.getThresholdMax());
        map.put("severity", log.getSeverity());
        map.put("status", log.getStatus());
        map.put("handleMethod", log.getHandleMethod());
        map.put("handledBy", log.getHandledBy());
        map.put("handledAt", log.getHandledAt());
        map.put("triggerCount", log.getTriggerCount());
        map.put("lastTriggeredAt", log.getLastTriggeredAt());
        map.put("remark", log.getRemark());
        map.put("createdAt", log.getCreatedAt());
        return map;
    }
}
