package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.dto.AlarmThresholdDTO;
import org.example.aquabackend.entity.AlarmThreshold;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.entity.WaterQualityData;
import org.example.aquabackend.mapper.AlarmThresholdMapper;
import org.example.aquabackend.mapper.AlarmLogMapper;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.mapper.WaterQualityDataMapper;
import org.example.aquabackend.service.AlarmThresholdService;
import org.example.aquabackend.vo.AlarmOverviewVO;
import org.example.aquabackend.vo.AlarmThresholdVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlarmThresholdServiceImpl implements AlarmThresholdService {

    private static final Logger logger = LoggerFactory.getLogger(AlarmThresholdServiceImpl.class);

    @Autowired
    private AlarmThresholdMapper alarmThresholdMapper;

    @Autowired
    private PondMapper pondMapper;

    @Autowired
    private WaterQualityDataMapper waterQualityDataMapper;

    @Autowired
    private AlarmLogMapper alarmLogMapper;

    @Override
    public List<AlarmThresholdVO> getThresholds(Integer pondId) {
        QueryWrapper<AlarmThreshold> qw = new QueryWrapper<>();
        if (pondId != null) {
            qw.eq("pond_id", pondId);
        }
        qw.orderByAsc("pond_id", "target_param");
        List<AlarmThreshold> list = alarmThresholdMapper.selectList(qw);
        return list.stream().map(t -> AlarmThresholdVO.builder()
                .thresholdId(t.getThresholdId())
                .pondId(t.getPondId())
                .targetParam(t.getTargetParam())
                .minValue(t.getMinValue())
                .maxValue(t.getMaxValue())
                .severity(t.getSeverity())
                .enabled(t.getEnabled())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void batchSave(List<AlarmThresholdDTO> list) {
        if (CollectionUtils.isEmpty(list)) return;

        for (AlarmThresholdDTO dto : list) {
            AlarmThreshold existing = alarmThresholdMapper.selectOne(
                    new QueryWrapper<AlarmThreshold>()
                            .eq("pond_id", dto.getPondId())
                            .eq("target_param", dto.getTargetParam())
            );

            if (existing != null) {
                existing.setMinValue(dto.getMinValue());
                existing.setMaxValue(dto.getMaxValue());
                existing.setSeverity(dto.getSeverity());
                existing.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
                alarmThresholdMapper.updateById(existing);
            } else {
                AlarmThreshold entity = new AlarmThreshold();
                BeanUtils.copyProperties(dto, entity);
                entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : 1);
                entity.setSeverity(dto.getSeverity() != null ? dto.getSeverity() : "warning");
                alarmThresholdMapper.insert(entity);
            }
        }
        logger.info("Batch saved {} alarm thresholds", list.size());
    }

    @Override
    public AlarmOverviewVO getOverview() {
        long totalPonds = pondMapper.selectCount(new QueryWrapper<Pond>().eq("deleted", 0));

        long configuredPonds = alarmThresholdMapper.selectObjs(
                new QueryWrapper<AlarmThreshold>().select("DISTINCT pond_id")
        ).size();

        long activeAlarms = alarmLogMapper.selectCount(
                new QueryWrapper<org.example.aquabackend.entity.AlarmLog>()
                        .in("status", "unhandled", "processing")
        );

        long criticalAlarms = alarmLogMapper.selectCount(
                new QueryWrapper<org.example.aquabackend.entity.AlarmLog>()
                        .eq("severity", "critical")
                        .in("status", "unhandled", "processing")
        );

        long warningAlarms = alarmLogMapper.selectCount(
                new QueryWrapper<org.example.aquabackend.entity.AlarmLog>()
                        .eq("severity", "warning")
                        .in("status", "unhandled", "processing")
        );

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayAlarms = alarmLogMapper.selectCount(
                new QueryWrapper<org.example.aquabackend.entity.AlarmLog>()
                        .ge("created_at", todayStart)
        );

        return AlarmOverviewVO.builder()
                .totalPonds(totalPonds)
                .configuredPonds(configuredPonds)
                .activeAlarms(activeAlarms)
                .criticalAlarms(criticalAlarms)
                .warningAlarms(warningAlarms)
                .todayAlarms(todayAlarms)
                .build();
    }

    @Override
    public List<AlarmThresholdVO> getThresholdsWithCurrent(Integer pondId) {
        List<Pond> ponds;
        if (pondId != null) {
            Pond p = pondMapper.selectById(pondId);
            ponds = p != null ? List.of(p) : List.of();
        } else {
            ponds = pondMapper.selectList(new QueryWrapper<Pond>().eq("deleted", 0));
        }

        Map<Integer, WaterQualityData> latestMap = ponds.stream()
                .map(p -> {
                    WaterQualityData d = waterQualityDataMapper.selectOne(
                            new QueryWrapper<WaterQualityData>()
                                    .eq("pond_id", p.getPondId())
                                    .orderByDesc("recorded_at")
                                    .last("LIMIT 1")
                    );
                    return Map.entry(p.getPondId(), d);
                })
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<AlarmThresholdVO> result = new ArrayList<>();

        for (Pond pond : ponds) {
            WaterQualityData wq = latestMap.get(pond.getPondId());

            if (wq == null) {
                for (String param : params()) {
                    result.add(buildVO(pond.getPondId(), param, null, null, null, "normal", "warning", 1));
                }
                continue;
            }

            List<AlarmThreshold> thresholds = alarmThresholdMapper.selectList(
                    new QueryWrapper<AlarmThreshold>().eq("pond_id", pond.getPondId())
            );
            Map<String, AlarmThreshold> thresholdMap = thresholds.stream()
                    .collect(Collectors.toMap(AlarmThreshold::getTargetParam, t -> t));

            for (String param : params()) {
                Double current = getCurrentValue(wq, param);
                AlarmThreshold th = thresholdMap.get(param);
                Double minVal = th != null ? th.getMinValue() : null;
                Double maxVal = th != null ? th.getMaxValue() : null;
                String severity = th != null ? th.getSeverity() : "warning";
                Integer enabled = th != null ? th.getEnabled() : 1;
                String status = evaluateStatus(current, minVal, maxVal, th);
                result.add(buildVO(pond.getPondId(), param, current, minVal, maxVal, status, severity, enabled));
            }
        }

        return result;
    }

    private List<String> params() {
        return List.of("temperature", "ph", "dissolvedOxygen", "ammoniaNitrogen", "nitrite", "transparency");
    }

    private Double getCurrentValue(WaterQualityData wq, String param) {
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

    private String evaluateStatus(Double current, Double minVal, Double maxVal, AlarmThreshold th) {
        if (th == null || th.getEnabled() == 0 || current == null) return "normal";
        if (minVal != null && current < minVal) return "critical";
        if (maxVal != null && current > maxVal) return "critical";
        return "normal";
    }

    private AlarmThresholdVO buildVO(Integer pondId, String targetParam, Double currentValue,
                                      Double minValue, Double maxValue, String status, String severity, Integer enabled) {
        return AlarmThresholdVO.builder()
                .pondId(pondId)
                .targetParam(targetParam)
                .currentValue(currentValue)
                .minValue(minValue)
                .maxValue(maxValue)
                .status(status)
                .severity(severity)
                .enabled(enabled)
                .build();
    }
}
