package org.example.aquabackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aquabackend.dto.DeviceBatchCreateDTO;
import org.example.aquabackend.dto.DeviceDTO;
import org.example.aquabackend.dto.DeviceUpdateDTO;
import org.example.aquabackend.entity.Device;
import org.example.aquabackend.entity.DeviceOperationLog;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.mapper.DeviceMapper;
import org.example.aquabackend.mapper.DeviceOperationLogMapper;
import org.example.aquabackend.mapper.PondMapper;
import org.example.aquabackend.service.DeviceService;
import org.example.aquabackend.vo.DeviceLogVO;
import org.example.aquabackend.vo.DeviceSnapshotVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);
    private static final int DEFAULT_HISTORY_LIMIT = 20;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private DeviceOperationLogMapper deviceOperationLogMapper;

    @Autowired
    private PondMapper pondMapper;

    @PostConstruct
    public void initDeviceSnapshot() {
        simulateDevices();
    }

    @Override
    public IPage<Map<String, Object>> list(int page, int size, Integer pondId, String deviceType, String status, String keyword) {
        QueryWrapper<Device> qw = buildFilterWrapper(pondId, deviceType, status, keyword);
        qw.orderByDesc("last_update").orderByDesc("device_id");

        Page<Device> devicePage = new Page<>(page, size);
        IPage<Device> result = deviceMapper.selectPage(devicePage, qw);

        Map<Integer, String> pondNameMap = loadPondNameMap();
        List<Map<String, Object>> records = result.getRecords().stream()
                .map(device -> toMap(device, pondNameMap.get(device.getPondId())))
                .collect(Collectors.toList());

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Device getById(Integer id) {
        return deviceMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Device add(DeviceDTO dto) {
        Device device = buildDeviceFromDto(dto);
        device.setDeviceId(null);
        deviceMapper.insert(device);
        logger.info("Created device: deviceId={}, pondId={}, name={}", device.getDeviceId(), device.getPondId(), device.getDeviceName());
        return device;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Device> batchAdd(DeviceBatchCreateDTO dto) {
        List<Device> createdDevices = new ArrayList<>();
        for (DeviceDTO item : dto.getDevices()) {
            Device device = buildDeviceFromDto(item);
            device.setPondId(dto.getPondId());
            device.setDeviceId(null);
            deviceMapper.insert(device);
            createdDevices.add(device);
        }
        logger.info("Batch created {} devices for pondId={}", createdDevices.size(), dto.getPondId());
        return createdDevices;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Device update(DeviceUpdateDTO dto) {
        Device device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new RuntimeException("Device not found");
        }
        if (dto.getPondId() != null) {
            device.setPondId(dto.getPondId());
        }
        if (StringUtils.hasText(dto.getDeviceName())) {
            device.setDeviceName(dto.getDeviceName());
        }
        if (StringUtils.hasText(dto.getDeviceType())) {
            device.setDeviceType(normalizeDeviceType(dto.getDeviceType()));
        }
        if (StringUtils.hasText(dto.getStatus())) {
            device.setStatus(normalizeStatus(dto.getStatus()));
        }
        device.setLastUpdate(LocalDateTime.now());
        device.setLastHeartbeat(LocalDateTime.now());
        deviceMapper.updateById(device);
        return device;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            return false;
        }
        return deviceMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Device updateStatus(Integer id, String status) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new RuntimeException("Device not found");
        }
        device.setStatus(normalizeStatus(status));
        device.setLastUpdate(LocalDateTime.now());
        device.setLastHeartbeat(LocalDateTime.now());
        deviceMapper.updateById(device);
        return device;
    }

    @Override
    @Scheduled(fixedDelay = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void simulateDevices() {
        List<Device> devices = deviceMapper.selectList(new QueryWrapper<Device>().orderByAsc("device_id"));
        if (CollectionUtils.isEmpty(devices)) {
            logger.debug("Skip device simulation because no devices exist");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Device device : devices) {
            simulateOne(device, now);
        }
        logger.debug("Generated device snapshots for {} devices at {}", devices.size(), now);
    }

    @Override
    public List<DeviceSnapshotVO> getLatestSnapshots(Integer pondId) {
        QueryWrapper<Device> qw = new QueryWrapper<>();
        if (pondId != null) {
            qw.eq("pond_id", pondId);
        }
        qw.orderByAsc("pond_id").orderByAsc("device_id");
        List<Device> devices = deviceMapper.selectList(qw);
        if (CollectionUtils.isEmpty(devices)) {
            return new ArrayList<>();
        }
        Map<Integer, String> pondNameMap = loadPondNameMap();
        return devices.stream()
                .map(device -> toSnapshotVO(device, pondNameMap.get(device.getPondId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceLogVO> getHistory(Integer pondId, Integer limit) {
        int actualLimit = limit == null || limit <= 0 ? DEFAULT_HISTORY_LIMIT : Math.min(limit, 100);
        List<DeviceOperationLog> logs = deviceOperationLogMapper.selectList(
                new QueryWrapper<DeviceOperationLog>()
                        .eq("pond_id", pondId)
                        .orderByDesc("recorded_at")
                        .last("LIMIT " + actualLimit)
        );
        if (CollectionUtils.isEmpty(logs)) {
            return new ArrayList<>();
        }

        Map<Integer, Device> deviceMap = deviceMapper.selectList(new QueryWrapper<Device>().eq("pond_id", pondId))
                .stream()
                .collect(Collectors.toMap(Device::getDeviceId, item -> item));
        String pondName = resolvePondName(pondId);
        List<DeviceLogVO> result = new ArrayList<>();
        for (DeviceOperationLog log : logs) {
            Device device = deviceMap.get(log.getDeviceId());
            result.add(toLogVO(log, device, pondName));
        }
        result.sort(Comparator.comparing(DeviceLogVO::getRecordedAt));
        return result;
    }

    private void simulateOne(Device device, LocalDateTime recordedAt) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        boolean shouldRun = random.nextDouble() > 0.2;
        if ("error".equals(device.getStatus())) {
            shouldRun = random.nextDouble() > 0.7;
        }

        if (shouldRun) {
            if (!"on".equals(device.getStatus())) {
                device.setStatus("on");
            }
            double currentRuntime = device.getRuntimeMinutes() == null ? 0.0 : device.getRuntimeMinutes();
            device.setRuntimeMinutes(round(currentRuntime + random.nextDouble(0.12, 0.28), 2));
        } else if (random.nextDouble() < 0.1) {
            device.setStatus("error");
        } else {
            device.setStatus("off");
        }

        String actualType = StringUtils.hasText(device.getDeviceType())
                ? normalizeDeviceType(device.getDeviceType())
                : "aerator";
        double[] ranges = simulationRanges(actualType);
        double power = "on".equals(device.getStatus())
                ? round(randomBetween(random, ranges[0], ranges[1]), 2)
                : 0.0;
        double flow = "on".equals(device.getStatus())
                ? round(randomBetween(random, ranges[2], ranges[3]), 2)
                : 0.0;

        device.setPowerKw(power);
        device.setFlowRate(flow);
        device.setLastHeartbeat(recordedAt);
        device.setLastUpdate(recordedAt);
        deviceMapper.updateById(device);

        DeviceOperationLog log = new DeviceOperationLog();
        log.setDeviceId(device.getDeviceId());
        log.setPondId(device.getPondId());
        log.setStatus(device.getStatus());
        log.setRuntimeMinutes(device.getRuntimeMinutes());
        log.setPowerKw(device.getPowerKw());
        log.setFlowRate(device.getFlowRate());
        log.setRecordedAt(recordedAt);
        deviceOperationLogMapper.insert(log);
    }

    private Device buildDeviceFromDto(DeviceDTO dto) {
        Device device = new Device();
        BeanUtils.copyProperties(dto, device);
        device.setDeviceType(normalizeDeviceType(dto.getDeviceType()));
        device.setStatus(StringUtils.hasText(dto.getStatus()) ? normalizeStatus(dto.getStatus()) : "off");
        if (device.getRuntimeMinutes() == null) {
            device.setRuntimeMinutes(0.0);
        }
        if (device.getPowerKw() == null) {
            device.setPowerKw(0.0);
        }
        if (device.getFlowRate() == null) {
            device.setFlowRate(0.0);
        }
        device.setLastHeartbeat(LocalDateTime.now());
        device.setLastUpdate(LocalDateTime.now());
        return device;
    }

    private QueryWrapper<Device> buildFilterWrapper(Integer pondId, String deviceType, String status, String keyword) {
        QueryWrapper<Device> qw = new QueryWrapper<>();
        if (pondId != null) {
            qw.eq("pond_id", pondId);
        }
        if (StringUtils.hasText(deviceType)) {
            qw.eq("device_type", normalizeDeviceType(deviceType));
        }
        if (StringUtils.hasText(status)) {
            qw.eq("status", normalizeStatus(status));
        }
        if (StringUtils.hasText(keyword)) {
            qw.and(wrapper -> wrapper.like("device_name", keyword).or().like("device_type", keyword));
        }
        return qw;
    }

    private Map<Integer, String> loadPondNameMap() {
        List<Pond> ponds = pondMapper.selectList(new QueryWrapper<Pond>().eq("deleted", 0));
        if (CollectionUtils.isEmpty(ponds)) {
            return new HashMap<>();
        }
        return ponds.stream().collect(Collectors.toMap(Pond::getPondId, Pond::getName));
    }

    private String resolvePondName(Integer pondId) {
        Pond pond = pondMapper.selectById(pondId);
        return pond != null ? pond.getName() : "Unknown pond";
    }

    private Map<String, Object> toMap(Device device, String pondName) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("deviceId", device.getDeviceId());
        map.put("pondId", device.getPondId());
        map.put("pondName", pondName);
        map.put("deviceName", device.getDeviceName());
        map.put("deviceType", device.getDeviceType());
        map.put("status", device.getStatus());
        map.put("runtimeMinutes", device.getRuntimeMinutes());
        map.put("powerKw", device.getPowerKw());
        map.put("flowRate", device.getFlowRate());
        map.put("lastHeartbeat", device.getLastHeartbeat());
        map.put("lastUpdate", device.getLastUpdate());
        return map;
    }

    private DeviceSnapshotVO toSnapshotVO(Device device, String pondName) {
        return DeviceSnapshotVO.builder()
                .deviceId(device.getDeviceId())
                .pondId(device.getPondId())
                .pondName(pondName)
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .status(device.getStatus())
                .runtimeMinutes(device.getRuntimeMinutes())
                .powerKw(device.getPowerKw())
                .flowRate(device.getFlowRate())
                .lastHeartbeat(device.getLastHeartbeat())
                .lastUpdate(device.getLastUpdate())
                .build();
    }

    private DeviceLogVO toLogVO(DeviceOperationLog log, Device device, String pondName) {
        return DeviceLogVO.builder()
                .logId(log.getLogId())
                .deviceId(log.getDeviceId())
                .deviceName(device != null ? device.getDeviceName() : "Deleted device")
                .deviceType(device != null ? device.getDeviceType() : "unknown")
                .pondId(log.getPondId())
                .pondName(pondName)
                .status(log.getStatus())
                .runtimeMinutes(log.getRuntimeMinutes())
                .powerKw(log.getPowerKw())
                .flowRate(log.getFlowRate())
                .recordedAt(log.getRecordedAt())
                .build();
    }

    private double[] simulationRanges(String deviceType) {
        switch (deviceType) {
            case "pump":
                return new double[]{2.0, 4.8, 40.0, 120.0};
            case "feeder":
                return new double[]{0.3, 1.2, 5.0, 20.0};
            case "aerator":
            default:
                return new double[]{3.0, 6.5, 0.0, 0.0};
        }
    }

    private String normalizeDeviceType(String deviceType) {
        if (!StringUtils.hasText(deviceType)) {
            throw new IllegalArgumentException("Device type is required");
        }
        switch (deviceType.trim()) {
            case "aerator":
            case "增氧机":
                return "aerator";
            case "pump":
            case "水泵":
                return "pump";
            case "feeder":
            case "投喂机":
                return "feeder";
            default:
                throw new IllegalArgumentException("Unsupported device type: " + deviceType);
        }
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "off";
        }
        switch (status.trim()) {
            case "on":
            case "运行中":
                return "on";
            case "off":
            case "待机":
                return "off";
            case "error":
            case "故障":
                return "error";
            default:
                throw new IllegalArgumentException("Unsupported device status: " + status);
        }
    }

    private double randomBetween(ThreadLocalRandom random, double min, double max) {
        if (max <= min) {
            return min;
        }
        return random.nextDouble(min, max);
    }

    private double round(double value, int scale) {
        double factor = Math.pow(10, scale);
        return Math.round(value * factor) / factor;
    }
}
