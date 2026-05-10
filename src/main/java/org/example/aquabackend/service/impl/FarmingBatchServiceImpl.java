package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aquabackend.dto.StockingRecordDTO;
import org.example.aquabackend.entity.FarmingBatch;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.mapper.FarmingBatchMapper;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.service.FarmingBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FarmingBatchServiceImpl implements FarmingBatchService {

    private static final Logger logger = LoggerFactory.getLogger(FarmingBatchServiceImpl.class);

    @Autowired
    private FarmingBatchMapper farmingBatchMapper;

    @Autowired
    private PondMapper pondMapper;

    @Override
    public IPage<Map<String, Object>> list(int page, int size, String pondName, String species) {
        // 1. 如果按塘口名称搜索，先查出匹配的 pondId 列表
        Set<Integer> pondIds = null;
        if (StringUtils.hasText(pondName)) {
            QueryWrapper<Pond> pondQw = new QueryWrapper<>();
            pondQw.like("name", pondName);
            List<Pond> ponds = pondMapper.selectList(pondQw);
            pondIds = ponds.stream().map(Pond::getPondId).collect(Collectors.toSet());
            if (pondIds.isEmpty()) {
                // 没有匹配的塘口，返回空
                return new Page<>(page, size, 0);
            }
        }

        // 2. 查询放养记录（未删除的）
        QueryWrapper<FarmingBatch> qw = new QueryWrapper<>();
        qw.eq("status", "active");

        if (pondIds != null) {
            qw.in("pond_id", pondIds);
        }
        if (StringUtils.hasText(species)) {
            qw.eq("species", species);
        }
        qw.orderByDesc("stock_date");

        Page<FarmingBatch> batchPage = new Page<>(page, size);
        IPage<FarmingBatch> result = farmingBatchMapper.selectPage(batchPage, qw);

        // 3. 关联塘口名称，构造返回结果
        List<Map<String, Object>> records = new ArrayList<>();
        for (FarmingBatch batch : result.getRecords()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("batchId", batch.getBatchId());
            map.put("pondId", batch.getPondId());
            map.put("breedId", batch.getBreedId());
            map.put("species", batch.getSpecies());
            map.put("stockCount", batch.getStockCount());
            map.put("currentNum", batch.getCurrentNum());
            map.put("avgSpec", batch.getAvgSpec());
            map.put("survivalRate", batch.getSurvivalRate());
            map.put("stockDate", batch.getStockDate());
            map.put("status", batch.getStatus());

            // 查询塘口名称
            Pond pond = pondMapper.selectById(batch.getPondId());
            map.put("pondName", pond != null ? pond.getName() : "未知");

            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public FarmingBatch add(StockingRecordDTO dto) {
        FarmingBatch batch = new FarmingBatch();
        batch.setPondId(dto.getPondId());
        batch.setBreedId(dto.getBreedId());
        batch.setSpecies(dto.getSpecies());
        batch.setStockCount(dto.getStockCount());
        batch.setCurrentNum(dto.getCurrentNum() != null ? dto.getCurrentNum() : dto.getStockCount());
        batch.setAvgSpec(dto.getAvgSpec());
        batch.setSurvivalRate(dto.getSurvivalRate());
        batch.setStockDate(dto.getStockDate());
        batch.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");

        farmingBatchMapper.insert(batch);
        logger.info("Created stocking record: batchId={}, pondId={}, species={}, count={}",
                batch.getBatchId(), batch.getPondId(), batch.getSpecies(), batch.getStockCount());
        return batch;
    }

    @Override
    public FarmingBatch update(StockingRecordDTO dto) {
        FarmingBatch batch = farmingBatchMapper.selectById(dto.getBatchId());
        if (batch == null) {
            throw new RuntimeException("放养记录不存在");
        }

        batch.setPondId(dto.getPondId());
        batch.setBreedId(dto.getBreedId());
        batch.setSpecies(dto.getSpecies());
        batch.setStockCount(dto.getStockCount());
        batch.setCurrentNum(dto.getCurrentNum());
        batch.setAvgSpec(dto.getAvgSpec());
        batch.setSurvivalRate(dto.getSurvivalRate());
        batch.setStockDate(dto.getStockDate());
        if (dto.getStatus() != null) {
            batch.setStatus(dto.getStatus());
        }

        farmingBatchMapper.updateById(batch);
        logger.info("Updated stocking record: batchId={}", batch.getBatchId());
        return batch;
    }

    @Override
    public boolean delete(Integer id) {
        FarmingBatch batch = farmingBatchMapper.selectById(id);
        if (batch == null) {
            return false;
        }
        // 逻辑删除：将状态改为 deleted
        batch.setStatus("deleted");
        int rows = farmingBatchMapper.updateById(batch);
        logger.info("Deleted stocking record: batchId={}", id);
        return rows > 0;
    }

    @Override
    public IPage<Map<String, Object>> listByPond(int page, int size, Integer pondId, String species) {
        // 1. 查询放养记录（未删除的），按塘口 ID 筛选
        QueryWrapper<FarmingBatch> qw = new QueryWrapper<>();
        qw.eq("status", "active");

        if (pondId != null) {
            qw.eq("pond_id", pondId);
        }
        if (StringUtils.hasText(species)) {
            qw.eq("species", species);
        }
        qw.orderByDesc("stock_date");

        Page<FarmingBatch> batchPage = new Page<>(page, size);
        IPage<FarmingBatch> result = farmingBatchMapper.selectPage(batchPage, qw);

        // 2. 关联塘口名称
        List<Map<String, Object>> records = new ArrayList<>();
        for (FarmingBatch batch : result.getRecords()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("batchId", batch.getBatchId());
            map.put("pondId", batch.getPondId());
            map.put("breedId", batch.getBreedId());
            map.put("species", batch.getSpecies());
            map.put("stockCount", batch.getStockCount());
            map.put("currentNum", batch.getCurrentNum());
            map.put("avgSpec", batch.getAvgSpec());
            map.put("survivalRate", batch.getSurvivalRate());
            map.put("stockDate", batch.getStockDate());
            map.put("status", batch.getStatus());

            Pond pond = pondMapper.selectById(batch.getPondId());
            map.put("pondName", pond != null ? pond.getName() : "未知");

            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }
}
