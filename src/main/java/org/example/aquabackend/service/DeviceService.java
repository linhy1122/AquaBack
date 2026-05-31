package org.example.aquabackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.aquabackend.dto.DeviceBatchCreateDTO;
import org.example.aquabackend.dto.DeviceDTO;
import org.example.aquabackend.dto.DeviceUpdateDTO;
import org.example.aquabackend.entity.Device;
import org.example.aquabackend.vo.DeviceLogVO;
import org.example.aquabackend.vo.DeviceSnapshotVO;

import java.util.List;
import java.util.Map;

public interface DeviceService {

    IPage<Map<String, Object>> list(int page, int size, Integer pondId, String deviceType, String status, String keyword);

    Device getById(Integer id);

    Device add(DeviceDTO dto);

    List<Device> batchAdd(DeviceBatchCreateDTO dto);

    Device update(DeviceUpdateDTO dto);

    boolean delete(Integer id);

    Device updateStatus(Integer id, String status);

    void simulateDevices();

    List<DeviceSnapshotVO> getLatestSnapshots(Integer pondId);

    List<DeviceLogVO> getHistory(Integer pondId, Integer limit);
}
