package org.example.aquabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.aquabackend.entity.Breed;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BreedMapper extends BaseMapper<Breed> {
}
