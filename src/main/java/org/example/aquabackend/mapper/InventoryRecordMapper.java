package org.example.aquabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aquabackend.entity.InventoryRecord;

@Mapper
public interface InventoryRecordMapper extends BaseMapper<InventoryRecord> {
}
