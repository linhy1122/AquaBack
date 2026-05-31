package org.example.aquabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aquabackend.entity.WaterQualityData;

@Mapper
public interface WaterQualityDataMapper extends BaseMapper<WaterQualityData> {
}
