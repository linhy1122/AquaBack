package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.entity.FeedingLog;
import org.example.aquabackend.entity.Material;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.mapper.FeedingLogMapper;
import org.example.aquabackend.mapper.MaterialMapper;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.service.FeedingStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedingStatsServiceImpl implements FeedingStatsService {

    @Autowired
    private FeedingLogMapper feedingLogMapper;

    @Autowired
    private PondMapper pondMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Override
    public Map<String, Object> getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        List<FeedingLog> allLogs = feedingLogMapper.selectList(
                new QueryWrapper<FeedingLog>().orderByDesc("feed_time"));

        List<FeedingLog> todayLogs = allLogs.stream()
                .filter(l -> l.getFeedTime() != null && !l.getFeedTime().isBefore(todayStart))
                .collect(Collectors.toList());

        List<FeedingLog> monthLogs = allLogs.stream()
                .filter(l -> l.getFeedTime() != null && !l.getFeedTime().isBefore(monthStart))
                .collect(Collectors.toList());

        double todayTotal = todayLogs.stream()
                .mapToDouble(l -> l.getActualAmount() != null ? l.getActualAmount() : 0).sum();

        double monthTotal = monthLogs.stream()
                .mapToDouble(l -> l.getActualAmount() != null ? l.getActualAmount() : 0).sum();

        double monthCost = 0;
        for (FeedingLog log : monthLogs) {
            if (log.getMaterialId() != null) {
                Material m = materialMapper.selectById(log.getMaterialId());
                if (m != null && m.getUnitPrice() != null && log.getActualAmount() != null) {
                    monthCost += log.getActualAmount() * m.getUnitPrice();
                }
            }
        }

        List<Pond> allPonds = pondMapper.selectList(
                new QueryWrapper<Pond>().eq("deleted", 0));
        Map<Integer, Pond> pondMap = allPonds.stream()
                .collect(Collectors.toMap(Pond::getPondId, p -> p));

        Map<Integer, List<FeedingLog>> byPond = monthLogs.stream()
                .collect(Collectors.groupingBy(
                    l -> resolvePondId(l),
                    Collectors.toList()
                ));

        List<Map<String, Object>> pondStats = new ArrayList<>();
        for (Pond pond : allPonds) {
            List<FeedingLog> logs = byPond.getOrDefault(pond.getPondId(), Collections.emptyList());
            double total = logs.stream().mapToDouble(l -> l.getActualAmount() != null ? l.getActualAmount() : 0).sum();
            long daysWithFeeding = logs.stream()
                    .filter(l -> l.getFeedTime() != null)
                    .map(l -> l.getFeedTime().toLocalDate())
                    .distinct().count();

            Map<String, Object> ps = new LinkedHashMap<>();
            ps.put("pondId", pond.getPondId());
            ps.put("pondName", pond.getName());
            ps.put("pondCode", pond.getCode());
            ps.put("feedTimes", logs.size());
            ps.put("totalAmount", Math.round(total * 100.0) / 100.0);
            ps.put("dailyAvg", daysWithFeeding > 0 ? Math.round(total / daysWithFeeding * 100.0) / 100.0 : 0);
            pondStats.add(ps);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayTotal", Math.round(todayTotal * 100.0) / 100.0);
        result.put("monthTotal", Math.round(monthTotal * 100.0) / 100.0);
        result.put("monthCost", Math.round(monthCost * 100.0) / 100.0);
        result.put("dailyAvg", monthLogs.isEmpty() ? 0 : Math.round(monthTotal / Math.max(1, LocalDate.now().getDayOfMonth()) * 100.0) / 100.0);
        result.put("pondStats", pondStats);
        return result;
    }

    private Integer resolvePondId(FeedingLog log) {
        if (log.getPondId() != null) return log.getPondId();
        return 0;
    }
}
