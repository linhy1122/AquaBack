package org.example.aquabackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.aquabackend.entity.FarmingBatch;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.mapper.FarmingBatchMapper;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.service.StatisticService;
import org.example.aquabackend.vo.PondStatisticVO;
import org.example.aquabackend.vo.StockingStatisticVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticServiceImpl implements StatisticService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticServiceImpl.class);

    @Autowired
    private PondMapper pondMapper;

    @Autowired
    private FarmingBatchMapper farmingBatchMapper;

    @Override
    public PondStatisticVO getPondStatistic() {
        // 塘口总数（未删除）
        QueryWrapper<Pond> totalQw = new QueryWrapper<>();
        totalQw.eq("deleted", 0);
        Long totalCount = pondMapper.selectCount(totalQw);

        // 使用中 COUNT(WHERE status='1')
        QueryWrapper<Pond> inUseQw = new QueryWrapper<>();
        inUseQw.eq("deleted", 0).eq("status", "1");
        Long inUseCount = pondMapper.selectCount(inUseQw);

        // 空闲 COUNT(WHERE status='2')
        QueryWrapper<Pond> idleQw = new QueryWrapper<>();
        idleQw.eq("deleted", 0).eq("status", "2");
        Long idleCount = pondMapper.selectCount(idleQw);

        // 总规模 SUM(area)
        QueryWrapper<Pond> areaQw = new QueryWrapper<>();
        areaQw.eq("deleted", 0).select("COALESCE(SUM(area), 0)");
        Object areaObj = pondMapper.selectObjs(areaQw).stream().findFirst().orElse(0.0);
        Double totalArea = areaObj != null ? Double.parseDouble(areaObj.toString()) : 0.0;

        return PondStatisticVO.builder()
                .totalCount(totalCount)
                .inUseCount(inUseCount)
                .idleCount(idleCount)
                .totalArea(totalArea)
                .build();
    }

    @Override
    public StockingStatisticVO getStockingStatistic() {
        // 总放养量 SUM(stock_count) WHERE status='active'
        QueryWrapper<FarmingBatch> countQw = new QueryWrapper<>();
        countQw.eq("status", "active").select("COALESCE(SUM(stock_count), 0)");
        Object countObj = farmingBatchMapper.selectObjs(countQw).stream().findFirst().orElse(0L);
        Long totalStockingCount = countObj != null ? Long.parseLong(countObj.toString()) : 0L;

        // 当前存活量 SUM(current_num)
        QueryWrapper<FarmingBatch> currentQw = new QueryWrapper<>();
        currentQw.eq("status", "active").select("COALESCE(SUM(current_num), 0)");
        Object currentObj = farmingBatchMapper.selectObjs(currentQw).stream().findFirst().orElse(0L);
        Long totalCurrentNum = currentObj != null ? Long.parseLong(currentObj.toString()) : 0L;

        // 平均存活率 AVG(survival_rate)
        QueryWrapper<FarmingBatch> survivalQw = new QueryWrapper<>();
        survivalQw.eq("status", "active").select("COALESCE(AVG(survival_rate), 0)");
        Object survivalObj = farmingBatchMapper.selectObjs(survivalQw).stream().findFirst().orElse(0.0);
        Double avgSurvivalRate = survivalObj != null ? Double.parseDouble(survivalObj.toString()) : 0.0;
        avgSurvivalRate = Math.round(avgSurvivalRate * 10.0) / 10.0; // 保留一位小数

        // 总放养重量 SUM(stock_count * avg_spec / 1000)
        QueryWrapper<FarmingBatch> weightQw = new QueryWrapper<>();
        weightQw.eq("status", "active").select("COALESCE(SUM(stock_count * COALESCE(avg_spec, 0) / 1000.0), 0)");
        Object weightObj = farmingBatchMapper.selectObjs(weightQw).stream().findFirst().orElse(0.0);
        Double totalWeight = weightObj != null ? Double.parseDouble(weightObj.toString()) : 0.0;
        totalWeight = Math.round(totalWeight * 100.0) / 100.0; // 保留两位小数

        return StockingStatisticVO.builder()
                .totalStockingCount(totalStockingCount)
                .totalCurrentNum(totalCurrentNum)
                .avgSurvivalRate(avgSurvivalRate)
                .totalWeight(totalWeight)
                .build();
    }
}
