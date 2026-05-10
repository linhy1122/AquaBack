package org.example.aquabackend.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aquabackend.dto.FeedInStockDTO;
import org.example.aquabackend.dto.FeedOutStockDTO;
import org.example.aquabackend.entity.InventoryRecord;
import org.example.aquabackend.entity.Material;
import org.example.aquabackend.mapper.InventoryRecordMapper;
import org.example.aquabackend.mapper.MaterialMapper;
import org.example.aquabackend.service.FeedInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FeedInventoryServiceImpl implements FeedInventoryService {

    private static final Logger logger = LoggerFactory.getLogger(FeedInventoryServiceImpl.class);

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private InventoryRecordMapper inventoryRecordMapper;

    @Override
    public IPage<Map<String, Object>> listInventory(int page, int size, String name) {
        QueryWrapper<Material> qw = new QueryWrapper<>();
        qw.eq("category", "饲料");
        if (StringUtils.hasText(name)) {
            qw.like("name", name);
        }
        qw.orderByDesc("material_id");

        Page<Material> materialPage = new Page<>(page, size);
        IPage<Material> result = materialMapper.selectPage(materialPage, qw);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Material material : result.getRecords()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("materialId", material.getMaterialId());
            map.put("name", material.getName());
            map.put("category", material.getCategory());
            map.put("unit", material.getUnit());
            map.put("unitPrice", material.getUnitPrice());
            map.put("currentStock", material.getStockQty());

            // 库存状态：实时计算，不持久化
            // 此处使用 stockQty 作为当前库存，用 unitPrice 作为 safeStock 参考
            // 实际项目中应添加 safeStock 字段，这里用 stockQty >= 100 作为"充足"判断
            Double stock = material.getStockQty() != null ? material.getStockQty() : 0;
            String status = stock >= 100 ? "充足" : "偏低";
            map.put("stockStatus", status);

            // 预计可用天数：假设日均消耗10kg，实际应从 feeding_logs 计算
            // 简化处理：按日均消耗10kg计算
            double dailyConsume = 10.0;
            int availableDays = dailyConsume > 0 ? (int) Math.floor(stock / dailyConsume) : 0;
            map.put("availableDays", availableDays);

            records.add(map);
        }

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Material inStock(FeedInStockDTO dto) {
        // 1. 查找或创建物料
        Material material = null;

        if (dto.getMaterialId() != null) {
            // 按 ID 查找
            material = materialMapper.selectById(dto.getMaterialId());
            if (material == null && dto.getMaterialName() != null) {
                // ID 不存在但有名称，用名称新创建物料
                material = createMaterial(dto.getMaterialName());
            }
        } else if (dto.getMaterialName() != null) {
            // 按名称查找，不存在则创建
            QueryWrapper<Material> qw = new QueryWrapper<>();
            qw.eq("name", dto.getMaterialName());
            material = materialMapper.selectOne(qw);
            if (material == null) {
                material = createMaterial(dto.getMaterialName());
            }
        }

        if (material == null) {
            throw new RuntimeException("请指定物料ID或饲料名称");
        }

        // 2. 增加库存
        Double oldStock = material.getStockQty() != null ? material.getStockQty() : 0;
        material.setStockQty(oldStock + dto.getQuantity());
        material.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : material.getUnitPrice());
        materialMapper.updateById(material);

        // 3. 写入入库流水
        InventoryRecord record = new InventoryRecord();
        record.setMaterialId(material.getMaterialId());
        record.setType("IN");
        record.setQuantity(dto.getQuantity());
        record.setTotalCost(dto.getUnitPrice() != null ? dto.getQuantity() * dto.getUnitPrice() : 0);
        record.setRecordDate(LocalDateTime.now());
        inventoryRecordMapper.insert(record);

        logger.info("Feed in-stock: materialId={}, name={}, quantity={}, newStock={}",
                material.getMaterialId(), material.getName(), dto.getQuantity(), material.getStockQty());
        return material;
    }

    /**
     * 按名称创建一个新的物料记录（类别=饲料）
     */
    private Material createMaterial(String name) {
        Material m = new Material();
        m.setName(name);
        m.setCategory("饲料");
        m.setUnit("kg");
        m.setStockQty(0.0);
        materialMapper.insert(m);
        logger.info("Auto-created material: materialId={}, name={}", m.getMaterialId(), name);
        return m;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Material outStock(FeedOutStockDTO dto) {
        // 1. 检查物料是否存在
        Material material = materialMapper.selectById(dto.getMaterialId());
        if (material == null) {
            throw new RuntimeException("物料不存在");
        }

        // 2. 校验库存是否充足
        Double currentStock = material.getStockQty() != null ? material.getStockQty() : 0;
        if (dto.getQuantity() > currentStock) {
            throw new RuntimeException(
                    String.format("库存不足！当前库存: %.2f %s, 出库数量: %.2f %s",
                            currentStock, material.getUnit() != null ? material.getUnit() : "kg",
                            dto.getQuantity(), material.getUnit() != null ? material.getUnit() : "kg"));
        }

        // 3. 扣减库存
        material.setStockQty(currentStock - dto.getQuantity());
        materialMapper.updateById(material);

        // 4. 写入出库流水
        InventoryRecord record = new InventoryRecord();
        record.setMaterialId(dto.getMaterialId());
        record.setBatchId(dto.getBatchId());
        record.setType("OUT");
        record.setQuantity(dto.getQuantity());
        record.setTotalCost(null); // 出库暂不计算成本
        record.setRecordDate(LocalDateTime.now());
        inventoryRecordMapper.insert(record);

        logger.info("Feed out-stock: materialId={}, quantity={}, batchId={}, newStock={}",
                dto.getMaterialId(), dto.getQuantity(), dto.getBatchId(), material.getStockQty());
        return material;
    }

    @Override
    public IPage<InventoryRecord> listRecords(int page, int size, Integer materialId) {
        QueryWrapper<InventoryRecord> qw = new QueryWrapper<>();
        if (materialId != null) {
            qw.eq("material_id", materialId);
        }
        qw.orderByDesc("record_date");

        Page<InventoryRecord> recordPage = new Page<>(page, size);
        return inventoryRecordMapper.selectPage(recordPage, qw);
    }
}
