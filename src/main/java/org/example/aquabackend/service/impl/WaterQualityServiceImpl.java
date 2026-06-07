package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.entity.WaterQualityData;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.mapper.WaterQualityDataMapper;
import org.example.aquabackend.service.AlarmEvaluator;
import org.example.aquabackend.service.WaterQualityService;
import org.example.aquabackend.vo.WaterQualitySnapshotVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WaterQualityServiceImpl implements WaterQualityService {

    private static final Logger logger = LoggerFactory.getLogger(WaterQualityServiceImpl.class);
    private static final int DEFAULT_HISTORY_LIMIT = 10;

    @Autowired
    private PondMapper pondMapper;

    @Autowired
    private WaterQualityDataMapper waterQualityDataMapper;

    @Autowired
    private AlarmEvaluator alarmEvaluator;

    @PostConstruct
    public void initWaterQualityData() {
        simulateWaterQuality();
    }

    @Override
    @Scheduled(fixedDelay = 30000)
    public void simulateWaterQuality() {
        List<Pond> ponds = pondMapper.selectList(
                new QueryWrapper<Pond>()
                        .eq("deleted", 0)
                        .orderByAsc("pond_id")
        );
        if (CollectionUtils.isEmpty(ponds)) {
            logger.debug("Skip water quality simulation because no ponds exist");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<WaterQualityData> batch = new ArrayList<>();
        for (Pond pond : ponds) {
            WaterQualityData latest = getLatestEntity(pond.getPondId());
            WaterQualityData generated = buildNextRecord(pond.getPondId(), latest, now);
            batch.add(generated);
            waterQualityDataMapper.insert(generated);
            alarmEvaluator.evaluate(generated);
        }
        logger.debug("Generated water quality records for {} ponds at {}", batch.size(), now);
    }

    @Override
    public List<WaterQualitySnapshotVO> getLatestSnapshots() {
        List<Pond> ponds = pondMapper.selectList(
                new QueryWrapper<Pond>()
                        .eq("deleted", 0)
                        .orderByAsc("pond_id")
        );
        List<WaterQualitySnapshotVO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(ponds)) {
            return result;
        }

        for (Pond pond : ponds) {
            WaterQualityData latest = getLatestEntity(pond.getPondId());
            if (latest != null) {
                result.add(toSnapshotVO(pond, latest));
            }
        }
        return result;
    }

    @Override
    public List<WaterQualitySnapshotVO> getRecentHistory(Integer pondId, Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? DEFAULT_HISTORY_LIMIT : Math.min(limit, 50);
        List<WaterQualityData> records = waterQualityDataMapper.selectList(
                new QueryWrapper<WaterQualityData>()
                        .eq("pond_id", pondId)
                        .orderByDesc("recorded_at")
                        .last("LIMIT " + actualLimit)
        );
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        Pond pond = pondMapper.selectById(pondId);
        String pondName = pond != null ? pond.getName() : "未知塘口";
        List<WaterQualitySnapshotVO> result = new ArrayList<>();
        for (WaterQualityData data : records) {
            result.add(toSnapshotVO(pondId, pondName, data));
        }
        result.sort(Comparator.comparing(WaterQualitySnapshotVO::getRecordedAt));
        return result;
    }

    private WaterQualityData getLatestEntity(Integer pondId) {
        return waterQualityDataMapper.selectOne(
                new QueryWrapper<WaterQualityData>()
                        .eq("pond_id", pondId)
                        .orderByDesc("recorded_at")
                        .last("LIMIT 1")
        );
    }

    private WaterQualityData buildNextRecord(Integer pondId, WaterQualityData latest, LocalDateTime recordedAt) {
        WaterQualityData data = new WaterQualityData();
        data.setPondId(pondId);
        data.setTemperature(nextValue(latest != null ? latest.getTemperature() : null, 24.0, 18.0, 32.0, 0.6));
        data.setPhValue(nextValue(latest != null ? latest.getPhValue() : null, 7.2, 6.2, 8.8, 0.08));
        data.setDissolvedOxygen(nextValue(latest != null ? latest.getDissolvedOxygen() : null, 6.2, 3.5, 9.0, 0.35));
        data.setAmmoniaNitrogen(nextValue(latest != null ? latest.getAmmoniaNitrogen() : null, 0.12, 0.0, 1.0, 0.03));
        data.setNitrite(nextValue(latest != null ? latest.getNitrite() : null, 0.05, 0.0, 0.5, 0.015));
        data.setTransparency(nextValue(latest != null ? latest.getTransparency() : null, 36.0, 15.0, 60.0, 1.5));
        data.setRecordedAt(recordedAt);
        return data;
    }

    private Double nextValue(Double previous, double baseline, double min, double max, double step) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double seed = previous != null ? previous : baseline + random.nextDouble(-1.0, 1.0);
        double drift = random.nextDouble(-step, step);
        double value = seed + drift;

        if (random.nextDouble() < 0.12) {
            double spike = random.nextDouble(0.0, Math.max(step * 4, 1.0));
            value += random.nextBoolean() ? spike : -spike;
        }

        return round(clamp(value, min, max), 2);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }

    private WaterQualitySnapshotVO toSnapshotVO(Pond pond, WaterQualityData data) {
        return toSnapshotVO(pond.getPondId(), pond.getName(), data);
    }

    private WaterQualitySnapshotVO toSnapshotVO(Integer pondId, String pondName, WaterQualityData data) {
        WaterQualitySnapshotVO vo = new WaterQualitySnapshotVO();
        BeanUtils.copyProperties(data, vo);
        vo.setPondId(pondId);
        vo.setPondName(pondName);
        return vo;
    }
}
