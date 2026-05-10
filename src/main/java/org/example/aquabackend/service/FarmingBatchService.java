package org.example.aquabackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.aquabackend.dto.StockingRecordDTO;
import org.example.aquabackend.entity.FarmingBatch;

import java.util.Map;

public interface FarmingBatchService {

    /**
     * 分页查询放养记录（支持塘口名称模糊搜索 + 品种筛选）
     */
    IPage<Map<String, Object>> list(int page, int size, String pondName, String species);

    /**
     * 新增放养记录
     */
    FarmingBatch add(StockingRecordDTO dto);

    /**
     * 编辑放养记录
     */
    FarmingBatch update(StockingRecordDTO dto);

    /**
     * 删除放养记录
     */
    boolean delete(Integer id);
}
