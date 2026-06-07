package org.example.aquabackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.aquabackend.dto.AlarmHandleDTO;
import org.example.aquabackend.entity.AlarmLog;

import java.util.List;
import java.util.Map;

public interface AlarmLogService {

    IPage<Map<String, Object>> getRecords(int page, int size, Integer pondId, String status, String severity);

    AlarmLog getById(Integer alarmId);

    AlarmLog handleAlarm(Integer alarmId, AlarmHandleDTO dto);

    long countActiveAlarms();

    long countBySeverity(String severity);

    long countTodayAlarms();

    List<AlarmLog> getRecentAlarms(int limit);
}
