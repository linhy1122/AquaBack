package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.entity.FarmingBatch;
import org.example.aquabackend.entity.FeedingLog;
import org.example.aquabackend.entity.InventoryRecord;
import org.example.aquabackend.entity.Material;
import org.example.aquabackend.mapper.FarmingBatchMapper;
import org.example.aquabackend.mapper.FeedingLogMapper;
import org.example.aquabackend.mapper.InventoryRecordMapper;
import org.example.aquabackend.mapper.MaterialMapper;
import org.example.aquabackend.service.DashboardService;
import org.example.aquabackend.vo.DashboardVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private FarmingBatchMapper farmingBatchMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private FeedingLogMapper feedingLogMapper;

    @Override
    public DashboardVO getSummary() {
        DashboardVO vo = DashboardVO.builder().build();

        // 存储总量 = SUM(stock_count)
        Long totalStock = getTotalStock();
        vo.setTotalStock(totalStock != null ? totalStock : 0L);

        // 较上月增长率
        vo.setStockGrowthRate(calculateGrowthRate());

        // 饲料总库存 = SUM(stock_qty) WHERE category = '饲料'
        Double feedStock = getFeedStock();
        vo.setFeedStockKg(feedStock != null ? feedStock : 0.0);

        // 饲料可用天数 = 总库存 / 日均消耗
        vo.setFeedAvailableDays(calculateAvailableDays(feedStock));

        // 本月消耗饲料 = SUM(amount) FROM feeding_logs WHERE MONTH = 本月
        Double monthlyConsumed = getMonthlyFeedConsumed();
        vo.setMonthlyFeedConsumed(monthlyConsumed != null ? monthlyConsumed : 0.0);

        return vo;
    }

    private Long getTotalStock() {
        try {
            QueryWrapper<FarmingBatch> qw = new QueryWrapper<>();
            qw.eq("status", "active");
            qw.select("COALESCE(SUM(stock_count), 0)");
            Object obj = farmingBatchMapper.selectObjs(qw).stream().findFirst().orElse(0L);
            return obj != null ? Long.parseLong(obj.toString()) : 0L;
        } catch (Exception e) {
            logger.error("Error calculating total stock", e);
            return 0L;
        }
    }

    private Double calculateGrowthRate() {
        try {
            LocalDate now = LocalDate.now();
            int thisYear = now.getYear();
            int thisMonth = now.getMonthValue();

            // 本月总量
            QueryWrapper<FarmingBatch> thisMonthQw = new QueryWrapper<>();
            thisMonthQw.eq("status", "active");
            thisMonthQw.select("COALESCE(SUM(stock_count), 0)");
            thisMonthQw.apply("YEAR(stock_date) = {0} AND MONTH(stock_date) = {1}", thisYear, thisMonth);
            Object thisMonthObj = farmingBatchMapper.selectObjs(thisMonthQw).stream().findFirst().orElse(0L);
            double thisMonthTotal = thisMonthObj != null ? Double.parseDouble(thisMonthObj.toString()) : 0;

            // 上月总量
            LocalDate lastMonth = now.minusMonths(1);
            int lastYear = lastMonth.getYear();
            int lastMonthVal = lastMonth.getMonthValue();

            QueryWrapper<FarmingBatch> lastMonthQw = new QueryWrapper<>();
            lastMonthQw.eq("status", "active");
            lastMonthQw.select("COALESCE(SUM(stock_count), 0)");
            lastMonthQw.apply("YEAR(stock_date) = {0} AND MONTH(stock_date) = {1}", lastYear, lastMonthVal);
            Object lastMonthObj = farmingBatchMapper.selectObjs(lastMonthQw).stream().findFirst().orElse(0L);
            double lastMonthTotal = lastMonthObj != null ? Double.parseDouble(lastMonthObj.toString()) : 0;

            if (lastMonthTotal > 0) {
                return Math.round((thisMonthTotal - lastMonthTotal) / lastMonthTotal * 100 * 10.0) / 10.0;
            }
            return 0.0;
        } catch (Exception e) {
            logger.error("Error calculating growth rate", e);
            return 0.0;
        }
    }

    private Double getFeedStock() {
        try {
            QueryWrapper<Material> qw = new QueryWrapper<>();
            qw.eq("category", "饲料");
            qw.select("COALESCE(SUM(stock_qty), 0)");
            Object obj = materialMapper.selectObjs(qw).stream().findFirst().orElse(0.0);
            return obj != null ? Double.parseDouble(obj.toString()) : 0.0;
        } catch (Exception e) {
            logger.error("Error calculating feed stock", e);
            return 0.0;
        }
    }

    private Integer calculateAvailableDays(Double totalStock) {
        if (totalStock == null || totalStock <= 0) return 0;

        try {
            // 近7天的日均消耗
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            QueryWrapper<FeedingLog> qw = new QueryWrapper<>();
            qw.select("COALESCE(SUM(amount), 0)");
            qw.ge("feed_time", sevenDaysAgo.atStartOfDay());
            Object obj = feedingLogMapper.selectObjs(qw).stream().findFirst().orElse(0.0);
            double weeklyConsume = obj != null ? Double.parseDouble(obj.toString()) : 0;

            double dailyConsume = weeklyConsume / 7.0;
            if (dailyConsume > 0) {
                return (int) Math.floor(totalStock / dailyConsume);
            }
            return 0;
        } catch (Exception e) {
            logger.error("Error calculating available days", e);
            return 0;
        }
    }

    private Double getMonthlyFeedConsumed() {
        try {
            LocalDate now = LocalDate.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
            LocalDateTime startOfNextMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();

            QueryWrapper<FeedingLog> qw = new QueryWrapper<>();
            qw.select("COALESCE(SUM(amount), 0)");
            qw.ge("feed_time", startOfMonth);
            qw.lt("feed_time", startOfNextMonth);
            Object obj = feedingLogMapper.selectObjs(qw).stream().findFirst().orElse(0.0);
            return obj != null ? Double.parseDouble(obj.toString()) : 0.0;
        } catch (Exception e) {
            logger.error("Error calculating monthly feed consumed", e);
            return 0.0;
        }
    }
}
