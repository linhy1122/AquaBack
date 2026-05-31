package org.example.aquabackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.dto.DeviceBatchCreateDTO;
import org.example.aquabackend.dto.DeviceDTO;
import org.example.aquabackend.dto.DeviceUpdateDTO;
import org.example.aquabackend.entity.Device;
import org.example.aquabackend.service.DeviceService;
import org.example.aquabackend.vo.DeviceLogVO;
import org.example.aquabackend.vo.DeviceSnapshotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/device")
@Api(value = "Device Monitoring", tags = "Pond Device Management")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/list")
    @ApiOperation(value = "List devices", notes = "Supports pond, type, status and keyword filters")
    public ApiResponse list(
            @ApiParam("Page number") @RequestParam(defaultValue = "1") int page,
            @ApiParam("Page size") @RequestParam(defaultValue = "10") int size,
            @ApiParam("Pond ID") @RequestParam(required = false) Integer pondId,
            @ApiParam("Device type") @RequestParam(required = false) String deviceType,
            @ApiParam("Device status") @RequestParam(required = false) String status,
            @ApiParam("Keyword") @RequestParam(required = false) String keyword) {
        IPage<Map<String, Object>> result = deviceService.list(page, size, pondId, deviceType, status, keyword);
        return ApiResponse.ok("Query success")
                .put("records", result.getRecords())
                .put("total", result.getTotal())
                .put("page", result.getCurrent())
                .put("size", result.getSize());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Get device detail")
    public ApiResponse detail(@ApiParam("Device ID") @PathVariable Integer id) {
        Device device = deviceService.getById(id);
        if (device == null) {
            return ApiResponse.fail("Device not found");
        }
        return ApiResponse.ok("Query success").put("device", device);
    }

    @PostMapping("/add")
    @ApiOperation(value = "Add device")
    public ApiResponse add(@Valid @RequestBody DeviceDTO dto) {
        Device device = deviceService.add(dto);
        return ApiResponse.ok("Created successfully").put("deviceId", device.getDeviceId());
    }

    @PostMapping("/batch-add")
    @ApiOperation(value = "Batch add devices")
    public ApiResponse batchAdd(@Valid @RequestBody DeviceBatchCreateDTO dto) {
        List<Device> devices = deviceService.batchAdd(dto);
        return ApiResponse.ok("Batch add success")
                .put("count", devices.size())
                .put("deviceIds", devices.stream().map(Device::getDeviceId).collect(Collectors.toList()));
    }

    @PutMapping("/update")
    @ApiOperation(value = "Update device")
    public ApiResponse update(@Valid @RequestBody DeviceUpdateDTO dto) {
        Device device = deviceService.update(dto);
        return ApiResponse.ok("Updated successfully").put("deviceId", device.getDeviceId());
    }

    @PatchMapping("/{id}/status")
    @ApiOperation(value = "Update device status")
    public ApiResponse updateStatus(
            @ApiParam("Device ID") @PathVariable Integer id,
            @ApiParam("Status (on/off/error)") @RequestParam String status) {
        Device device = deviceService.updateStatus(id, status);
        return ApiResponse.ok("Status updated")
                .put("deviceId", device.getDeviceId())
                .put("status", device.getStatus());
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete device")
    public ApiResponse delete(@ApiParam("Device ID") @PathVariable Integer id) {
        boolean flag = deviceService.delete(id);
        return flag ? ApiResponse.ok("Deleted successfully") : ApiResponse.fail("Device not found");
    }

    @GetMapping("/latest")
    @ApiOperation(value = "Get latest device snapshots", notes = "Optional pond filter; returns all devices if omitted")
    public ApiResponse latest(@ApiParam("Pond ID") @RequestParam(required = false) Integer pondId) {
        List<DeviceSnapshotVO> records = deviceService.getLatestSnapshots(pondId);
        return ApiResponse.ok("Query success")
                .put("records", records)
                .put("updatedAt", LocalDateTime.now());
    }

    @GetMapping("/history/{pondId}")
    @ApiOperation(value = "Get device history", notes = "Returns recent logs for the given pond")
    public ApiResponse history(
            @ApiParam("Pond ID") @PathVariable Integer pondId,
            @ApiParam("Limit") @RequestParam(defaultValue = "20") Integer limit) {
        List<DeviceLogVO> records = deviceService.getHistory(pondId, limit);
        return ApiResponse.ok("Query success")
                .put("records", records)
                .put("updatedAt", LocalDateTime.now());
    }
}
