package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.entity.AlarmLog;
import org.example.aquabackend.entity.AlarmThreshold;
import org.example.aquabackend.entity.WaterQualityData;
import org.example.aquabackend.mapper.AlarmLogMapper;
import org.example.aquabackend.mapper.AlarmThresholdMapper;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.service.AlarmEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmEvaluatorImpl implements AlarmEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(AlarmEvaluatorImpl.class);

    @Autowired
    private AlarmThresholdMapper alarmThresholdMapper;

    @Autowired
    private AlarmLogMapper alarmLogMapper;

    @Autowired
    private PondMapper pondMapper;

    @Override
    public void evaluate(WaterQualityData wqData) {
        Integer pondId = wqData.getPondId();
        List<AlarmThreshold> thresholds = alarmThresholdMapper.selectList(
                new QueryWrapper<AlarmThreshold>()
                        .eq("pond_id", pondId)
                        .eq("enabled", 1)
        );

        if (CollectionUtils.isEmpty(thresholds)) return;

        for (AlarmThreshold threshold : thresholds) {
            Double currentValue = getValue(wqData, threshold.getTargetParam());
            if (currentValue == null) continue;

            boolean isViolated = false;
            if (threshold.getMinValue() != null && currentValue < threshold.getMinValue()) {
                isViolated = true;
            } else if (threshold.getMaxValue() != null && currentValue > threshold.getMaxValue()) {
                isViolated = true;
            }

            if (isViolated) {
                handleViolation(wqData, threshold, currentValue);
            } else {
                handleRecovery(pondId, threshold.getTargetParam());
            }
        }
    }

    private void handleViolation(WaterQualityData wqData, AlarmThreshold threshold, Double currentValue) {
        Integer pondId = threshold.getPondId();
        String param = threshold.getTargetParam();

        AlarmLog open = alarmLogMapper.selectOne(
                new QueryWrapper<AlarmLog>()
                        .eq("pond_id", pondId)
                        .eq("alarm_item", param)
                        .in("status", "unhandled", "processing")
                        .last("LIMIT 1")
        );

        LocalDateTime now = LocalDateTime.now();
        String alarmValue = buildAlarmValue(param, currentValue, threshold);

        if (open != null) {
            open.setCurrentValue(currentValue);
            open.setThresholdMin(threshold.getMinValue());
            open.setThresholdMax(threshold.getMaxValue());
            open.setAlarmValue(alarmValue);
            open.setLastTriggeredAt(now);
            open.setTriggerCount(open.getTriggerCount() != null ? open.getTriggerCount() + 1 : 2);
            alarmLogMapper.updateById(open);
        } else {
            AlarmLog log = new AlarmLog();
            log.setPondId(pondId);
            log.setAlarmItem(param);
            log.setAlarmValue(alarmValue);
            log.setCurrentValue(currentValue);
            log.setThresholdMin(threshold.getMinValue());
            log.setThresholdMax(threshold.getMaxValue());
            log.setSeverity(threshold.getSeverity() != null ? threshold.getSeverity() : "warning");
            log.setStatus("unhandled");
            log.setTriggerCount(1);
            log.setLastTriggeredAt(now);
            log.setCreatedAt(now);
            alarmLogMapper.insert(log);

            logger.warn("Alarm triggered: pondId={}, param={}, value={}, threshold=[{}, {}]",
                    pondId, param, currentValue, threshold.getMinValue(), threshold.getMaxValue());
        }
    }

    private void handleRecovery(Integer pondId, String param) {
        List<AlarmLog> openAlarms = alarmLogMapper.selectList(
                new QueryWrapper<AlarmLog>()
                        .eq("pond_id", pondId)
                        .eq("alarm_item", param)
                        .in("status", "unhandled", "processing")
        );

        for (AlarmLog log : openAlarms) {
            log.setStatus("auto_recovered");
            log.setHandleMethod("auto_recovered");
            log.setHandledAt(LocalDateTime.now());
            alarmLogMapper.updateById(log);
            logger.info("Alarm auto-recovered: pondId={}, param={}", pondId, param);
        }
    }

    private String buildAlarmValue(String param, Double value, AlarmThreshold threshold) {
        String paramLabel;
        switch (param) {
            case "temperature": paramLabel = "水温"; break;
            case "ph": paramLabel = "pH值"; break;
            case "dissolvedOxygen": paramLabel = "溶解氧"; break;
            case "ammoniaNitrogen": paramLabel = "氨氮"; break;
            case "nitrite": paramLabel = "亚硝酸盐"; break;
            case "transparency": paramLabel = "透明度"; break;
            default: paramLabel = param; break;
        }
        String direction;
        if (threshold.getMinValue() != null && value < threshold.getMinValue()) {
            direction = "低于下限 " + threshold.getMinValue();
        } else if (threshold.getMaxValue() != null && value > threshold.getMaxValue()) {
            direction = "超过上限 " + threshold.getMaxValue();
        } else {
            direction = "异常";
        }
        return paramLabel + " " + value + " " + direction;
    }

    private Double getValue(WaterQualityData wq, String param) {
        switch (param) {
            case "temperature": return wq.getTemperature();
            case "ph": return wq.getPhValue();
            case "dissolvedOxygen": return wq.getDissolvedOxygen();
            case "ammoniaNitrogen": return wq.getAmmoniaNitrogen();
            case "nitrite": return wq.getNitrite();
            case "transparency": return wq.getTransparency();
            default: return null;
        }
    }
}
