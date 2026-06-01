package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.entity.*;
import org.example.aquabackend.mapper.*;
import org.example.aquabackend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired private PondMapper pondMapper;
    @Autowired private FarmingBatchMapper farmingBatchMapper;
    @Autowired private FeedingLogMapper feedingLogMapper;
    @Autowired private MaterialMapper materialMapper;
    @Autowired private InventoryRecordMapper inventoryRecordMapper;
    @Autowired private ExtraCostMapper extraCostMapper;
    @Autowired private ProfitProjectionMapper profitProjectionMapper;

    @Override
    public Map<String, Object> getDailyReport(String dateStr, Integer pondId) {
        LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23, 59, 59);

        List<Pond> ponds = pondMapper.selectList(new QueryWrapper<Pond>().eq("deleted", 0));
        List<FarmingBatch> batches = farmingBatchMapper.selectList(new QueryWrapper<FarmingBatch>().eq("status", "active"));

        List<FeedingLog> dayFeeding = feedingLogMapper.selectList(
                new QueryWrapper<FeedingLog>().ge("feed_time", dayStart).le("feed_time", dayEnd));
        List<InventoryRecord> dayInRecords = inventoryRecordMapper.selectList(
                new QueryWrapper<InventoryRecord>().eq("type", "in").ge("record_date", dayStart).le("record_date", dayEnd));
        List<InventoryRecord> dayOutRecords = inventoryRecordMapper.selectList(
                new QueryWrapper<InventoryRecord>().eq("type", "out").ge("record_date", dayStart).le("record_date", dayEnd));
        List<ExtraCost> dayCosts = extraCostMapper.selectList(new QueryWrapper<ExtraCost>().eq("record_date", date));

        if (pondId != null) {
            batches = batches.stream().filter(b -> b.getPondId().equals(pondId)).collect(Collectors.toList());
            dayFeeding = dayFeeding.stream().filter(l -> pondId.equals(l.getPondId())).collect(Collectors.toList());
        }

        double todayFeed = dayFeeding.stream().mapToDouble(l -> l.getActualAmount() != null ? l.getActualAmount() : 0).sum();
        double todayIn = dayInRecords.stream().mapToDouble(r -> r.getQuantity() != null ? r.getQuantity() : 0).sum();
        double todayOut = dayOutRecords.stream().mapToDouble(r -> r.getQuantity() != null ? r.getQuantity() : 0).sum();
        double todayExtraCost = dayCosts.stream().mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0).sum();
        double todayFeedCost = dayFeeding.stream().filter(l -> l.getMaterialId() != null)
                .mapToDouble(l -> { Material m = materialMapper.selectById(l.getMaterialId());
                    return (m != null && m.getUnitPrice() != null && l.getActualAmount() != null) ? l.getActualAmount() * m.getUnitPrice() : 0; }).sum();

        long activePonds = batches.stream().map(FarmingBatch::getPondId).distinct().count();
        long totalStock = batches.stream().mapToLong(b -> b.getStockCount() != null ? b.getStockCount() : 0).sum();
        long totalCurrent = batches.stream().mapToLong(b -> b.getCurrentNum() != null ? b.getCurrentNum() : 0).sum();

        List<Map<String, Object>> pondSeries = new ArrayList<>();
        for (FarmingBatch b : batches) {
            Pond p = ponds.stream().filter(x -> x.getPondId().equals(b.getPondId())).findFirst().orElse(null);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("pondId", b.getPondId()); item.put("pondCode", p != null ? p.getCode() : null);
            item.put("pondName", p != null ? p.getName() : "未知"); item.put("species", b.getSpecies());
            item.put("stockCount", b.getStockCount()); item.put("currentNum", b.getCurrentNum());
            item.put("survivalRate", b.getSurvivalRate());
            pondSeries.add(item);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("pondTotal", ponds.size()); summary.put("activePonds", activePonds);
        summary.put("totalStock", totalStock); summary.put("totalCurrent", totalCurrent);
        summary.put("todayFeed", Math.round(todayFeed * 100.0) / 100.0);
        summary.put("todayIn", Math.round(todayIn * 100.0) / 100.0);
        summary.put("todayOut", Math.round(todayOut * 100.0) / 100.0);
        summary.put("todayFeedCost", Math.round(todayFeedCost * 100.0) / 100.0);
        summary.put("todayExtraCost", Math.round(todayExtraCost * 100.0) / 100.0);
        summary.put("todayTotalCost", Math.round((todayFeedCost + todayExtraCost) * 100.0) / 100.0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary); result.put("pondSeries", pondSeries);
        result.put("date", date.toString()); result.put("generatedAt", LocalDateTime.now().toString());
        return result;
    }

    @Override
    public Map<String, Object> getMonthlyReport(String monthStr, Integer pondId) {
        YearMonth ym = monthStr != null ? YearMonth.parse(monthStr) : YearMonth.now();
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();
        LocalDateTime startDT = monthStart.atStartOfDay();
        LocalDateTime endDT = monthEnd.atTime(23, 59, 59);

        List<Pond> ponds = pondMapper.selectList(new QueryWrapper<Pond>().eq("deleted", 0));
        List<FarmingBatch> batches = farmingBatchMapper.selectList(new QueryWrapper<FarmingBatch>().eq("status", "active"));
        List<FeedingLog> monthFeeding = feedingLogMapper.selectList(
                new QueryWrapper<FeedingLog>().ge("feed_time", startDT).le("feed_time", endDT));
        List<InventoryRecord> monthIn = inventoryRecordMapper.selectList(
                new QueryWrapper<InventoryRecord>().eq("type", "in").ge("record_date", startDT).le("record_date", endDT));
        List<InventoryRecord> monthOut = inventoryRecordMapper.selectList(
                new QueryWrapper<InventoryRecord>().eq("type", "out").ge("record_date", startDT).le("record_date", endDT));
        List<ExtraCost> monthCosts = extraCostMapper.selectList(
                new QueryWrapper<ExtraCost>().ge("record_date", monthStart).le("record_date", monthEnd));

        if (pondId != null) {
            monthFeeding = monthFeeding.stream().filter(l -> pondId.equals(l.getPondId())).collect(Collectors.toList());
        }

        double monthFeed = monthFeeding.stream().mapToDouble(l -> l.getActualAmount() != null ? l.getActualAmount() : 0).sum();
        double monthInQty = monthIn.stream().mapToDouble(r -> r.getQuantity() != null ? r.getQuantity() : 0).sum();
        double monthOutQty = monthOut.stream().mapToDouble(r -> r.getQuantity() != null ? r.getQuantity() : 0).sum();
        double monthFeedCost = monthFeeding.stream().filter(l -> l.getMaterialId() != null)
                .mapToDouble(l -> { Material m = materialMapper.selectById(l.getMaterialId());
                    return (m != null && m.getUnitPrice() != null && l.getActualAmount() != null) ? l.getActualAmount() * m.getUnitPrice() : 0; }).sum();
        double monthExtraCost = monthCosts.stream().mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0).sum();

        List<Map<String, Object>> dailyFeedTrend = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate day = monthStart.withDayOfMonth(d);
            LocalDateTime ds = day.atStartOfDay(), de = day.atTime(23,59,59);
            double dayFeed = feedingLogMapper.selectList(
                new QueryWrapper<FeedingLog>().ge("feed_time", ds).le("feed_time", de))
                .stream().mapToDouble(l -> l.getActualAmount() != null ? l.getActualAmount() : 0).sum();
            Map<String, Object> dp = new LinkedHashMap<>();
            dp.put("date", day.toString()); dp.put("feedAmount", Math.round(dayFeed * 100.0) / 100.0);
            dailyFeedTrend.add(dp);
        }

        List<Map<String, Object>> pondRank = new ArrayList<>();
        Map<Integer, Double> feedByPond = monthFeeding.stream()
                .filter(l -> l.getPondId() != null)
                .collect(Collectors.groupingBy(FeedingLog::getPondId,
                    Collectors.summingDouble(l -> l.getActualAmount() != null ? l.getActualAmount() : 0)));
        for (Map.Entry<Integer, Double> e : feedByPond.entrySet()) {
            Pond p = ponds.stream().filter(x -> x.getPondId().equals(e.getKey())).findFirst().orElse(null);
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("pondId", e.getKey()); r.put("pondCode", p != null ? p.getCode() : null);
            r.put("pondName", p != null ? p.getName() : "未知");
            r.put("feedAmount", Math.round(e.getValue() * 100.0) / 100.0);
            pondRank.add(r);
        }
        pondRank.sort((a, b) -> Double.compare((Double) b.get("feedAmount"), (Double) a.get("feedAmount")));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("monthFeed", Math.round(monthFeed * 100.0) / 100.0);
        summary.put("monthIn", Math.round(monthInQty * 100.0) / 100.0);
        summary.put("monthOut", Math.round(monthOutQty * 100.0) / 100.0);
        summary.put("monthFeedCost", Math.round(monthFeedCost * 100.0) / 100.0);
        summary.put("monthExtraCost", Math.round(monthExtraCost * 100.0) / 100.0);
        summary.put("monthTotalCost", Math.round((monthFeedCost + monthExtraCost) * 100.0) / 100.0);
        summary.put("dailyAvg", monthFeed / Math.max(1, ym.lengthOfMonth()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary); result.put("dailyFeedTrend", dailyFeedTrend);
        result.put("pondRank", pondRank); result.put("month", ym.toString());
        result.put("generatedAt", LocalDateTime.now().toString());
        return result;
    }

    @Override
    public Map<String, Object> getAnalysisReport(String range, Integer pondId) {
        int days = "90d".equals(range) ? 90 : 30;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);

        List<Pond> ponds = pondMapper.selectList(new QueryWrapper<Pond>().eq("deleted", 0));
        List<FarmingBatch> batches = farmingBatchMapper.selectList(new QueryWrapper<FarmingBatch>().eq("status", "active"));
        List<FeedingLog> logs = feedingLogMapper.selectList(
                new QueryWrapper<FeedingLog>().ge("feed_time", start.atStartOfDay()));
        if (pondId != null) {
            logs = logs.stream().filter(l -> pondId.equals(l.getPondId())).collect(Collectors.toList());
        }

        List<ProfitProjection> projections = profitProjectionMapper.selectList(null);
        List<ExtraCost> extraCosts = extraCostMapper.selectList(
                new QueryWrapper<ExtraCost>().ge("record_date", start));

        List<Map<String, Object>> pondRadar = new ArrayList<>();
        for (FarmingBatch b : batches) {
            Pond p = ponds.stream().filter(x -> x.getPondId().equals(b.getPondId())).findFirst().orElse(null);
            double feedTotal = logs.stream().filter(l -> b.getPondId().equals(l.getPondId()))
                    .mapToDouble(l -> l.getActualAmount() != null ? l.getActualAmount() : 0).sum();
            double feedCost = logs.stream().filter(l -> b.getPondId().equals(l.getPondId()) && l.getMaterialId() != null)
                    .mapToDouble(l -> { Material m = materialMapper.selectById(l.getMaterialId());
                        return (m != null && m.getUnitPrice() != null && l.getActualAmount() != null) ? l.getActualAmount() * m.getUnitPrice() : 0; }).sum();
            double extraCost = extraCosts.stream().filter(c -> c.getBatchId() != null && c.getBatchId().equals(b.getBatchId()))
                    .mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0).sum();
            ProfitProjection proj = projections.stream().filter(x -> x.getBatchId() != null && x.getBatchId().equals(b.getBatchId())).findFirst().orElse(null);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("pondId", b.getPondId()); item.put("pondCode", p != null ? p.getCode() : null);
            item.put("pondName", p != null ? p.getName() : "未知"); item.put("species", b.getSpecies());
            item.put("currentNum", b.getCurrentNum()); item.put("survivalRate", b.getSurvivalRate());
            item.put("avgSpec", b.getAvgSpec()); item.put("feedAmount", Math.round(feedTotal * 100.0) / 100.0);
            item.put("feedCost", Math.round(feedCost * 100.0) / 100.0);
            item.put("extraCost", Math.round(extraCost * 100.0) / 100.0);
            if (proj != null && b.getCurrentNum() != null) {
                double survRate = proj.getExpSurvivalRate() != null ? proj.getExpSurvivalRate() : (b.getSurvivalRate() != null ? b.getSurvivalRate() : 80.0);
                double avgW = proj.getExpAvgWeight() != null ? proj.getExpAvgWeight() : (b.getAvgSpec() != null ? b.getAvgSpec() : 500.0);
                double price = proj.getTargetPrice() != null ? proj.getTargetPrice() : 20.0;
                double expectedSurvive = b.getCurrentNum() * survRate / 100.0;
                double productWt = expectedSurvive * avgW / 1000.0;
                double revenue = productWt * price;
                double profit = revenue - feedCost - extraCost;
                item.put("expRevenue", Math.round(revenue * 100.0) / 100.0);
                item.put("expProfit", Math.round(profit * 100.0) / 100.0);
            }
            pondRadar.add(item);
        }

        List<Map<String, Object>> conclusions = new ArrayList<>();
        for (Map<String, Object> pr : pondRadar) {
            Double profit = (Double) pr.get("expProfit");
            if (profit != null && profit < 0) {
                conclusions.add(Map.of("type", "danger", "message",
                    pr.get("pondName") + " 预计亏损 " + Math.abs(Math.round(profit)) + " 元，建议关注"));
            }
            Double sr = (Double) pr.get("survivalRate");
            if (sr != null && sr < 60) {
                conclusions.add(Map.of("type", "warning", "message",
                    pr.get("pondName") + " 存活率仅 " + sr + "%，建议排查"));
            }
        }
        conclusions.sort((a, b) -> ((String) a.get("type")).compareTo((String) b.get("type")));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pondRadar", pondRadar);
        result.put("conclusions", conclusions);
        result.put("range", range != null ? range : "30d");
        result.put("generatedAt", LocalDateTime.now().toString());
        return result;
    }
}
