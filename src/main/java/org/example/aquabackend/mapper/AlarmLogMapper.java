package org.example.aquabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.aquabackend.entity.AlarmLog;

@Mapper
public interface AlarmLogMapper extends BaseMapper<AlarmLog> {
}
