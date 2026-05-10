package org.example.aquabackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.aquabackend.dto.FeedInStockDTO;
import org.example.aquabackend.dto.FeedOutStockDTO;
import org.example.aquabackend.entity.InventoryRecord;
import org.example.aquabackend.entity.Material;

import java.util.Map;

public interface FeedInventoryService {

    /**
     * 查询饲料库存列表（含实时库存状态和可用天数）
     */
    IPage<Map<String, Object>> listInventory(int page, int size, String name);

    /**
     * 饲料入库（事务：增加库存 + 写入流水）
     */
    Material inStock(FeedInStockDTO dto);

    /**
     * 饲料出库（事务：扣减库存 + 写入流水，校验库存不足）
     */
    Material outStock(FeedOutStockDTO dto);

    /**
     * 出入库流水明细
     */
    IPage<InventoryRecord> listRecords(int page, int size, Integer materialId);
}
