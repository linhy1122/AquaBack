package org.example.aquabackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.aquabackend.dto.PondDTO;
import org.example.aquabackend.entity.Pond;

import java.util.Map;

public interface PondService {

    /**
     * 分页查询塘口列表（支持名称模糊搜索、状态筛选）
     */
    IPage<Map<String, Object>> list(int page, int size, String name, String status);

    /**
     * 新增塘口
     */
    Pond add(PondDTO dto);

    /**
     * 编辑塘口
     */
    Pond update(PondDTO dto);

    /**
     * 软删除塘口
     */
    boolean delete(Integer id);

    /**
     * 获取塘口详情
     */
    Pond getById(Integer id);
}
