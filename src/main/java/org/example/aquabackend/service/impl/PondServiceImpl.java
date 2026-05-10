package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aquabackend.dto.PondDTO;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.service.PondService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PondServiceImpl implements PondService {

    private static final Logger logger = LoggerFactory.getLogger(PondServiceImpl.class);

    @Autowired
    private PondMapper pondMapper;

    @Override
    public IPage<Map<String, Object>> list(int page, int size, String name, String status) {
        QueryWrapper<Pond> qw = new QueryWrapper<>();
        qw.eq("deleted", 0); // 只查询未删除的

        if (StringUtils.hasText(name)) {
            qw.like("name", name);
        }
        if (StringUtils.hasText(status)) {
            qw.eq("status", status);
        }
        qw.orderByDesc("created_at");

        Page<Pond> pondPage = new Page<>(page, size);
        IPage<Pond> result = pondMapper.selectPage(pondPage, qw);

        // 构造返回的 Map 列表
        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(result.getRecords().stream().map(this::toMap).collect(java.util.stream.Collectors.toList()));
        return resultPage;
    }

    @Override
    public Pond add(PondDTO dto) {
        Pond pond = new Pond();
        BeanUtils.copyProperties(dto, pond);
        pond.setDeleted(0);
        pond.setCreatedAt(LocalDateTime.now());

        pondMapper.insert(pond);
        logger.info("Created pond: pondId={}, name={}", pond.getPondId(), pond.getName());
        return pond;
    }

    @Override
    public Pond update(PondDTO dto) {
        Pond pond = pondMapper.selectById(dto.getPondId());
        if (pond == null) {
            throw new RuntimeException("塘口不存在");
        }
        BeanUtils.copyProperties(dto, pond);
        pondMapper.updateById(pond);
        logger.info("Updated pond: pondId={}", pond.getPondId());
        return pond;
    }

    @Override
    public boolean delete(Integer id) {
        Pond pond = pondMapper.selectById(id);
        if (pond == null) {
            return false;
        }
        pond.setDeleted(1);
        int rows = pondMapper.updateById(pond);
        logger.info("Soft-deleted pond: pondId={}", id);
        return rows > 0;
    }

    @Override
    public Pond getById(Integer id) {
        return pondMapper.selectById(id);
    }

    private Map<String, Object> toMap(Pond pond) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("pondId", pond.getPondId());
        map.put("code", pond.getCode());
        map.put("name", pond.getName());
        map.put("area", pond.getArea());
        map.put("depth", pond.getDepth());
        map.put("waterSource", pond.getWaterSource());
        map.put("location", pond.getLocation());
        map.put("status", pond.getStatus());
        map.put("manager", pond.getManager());
        map.put("createdAt", pond.getCreatedAt());
        return map;
    }
}
