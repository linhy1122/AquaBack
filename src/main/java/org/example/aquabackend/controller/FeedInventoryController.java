package org.example.aquabackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.dto.FeedInStockDTO;
import org.example.aquabackend.dto.FeedOutStockDTO;
import org.example.aquabackend.entity.InventoryRecord;
import org.example.aquabackend.entity.Material;
import org.example.aquabackend.service.FeedInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/feed")
@Api(value = "饲料库存管理", tags = "饲料库存管理（入库/出库/明细）")
public class FeedInventoryController {

    private static final Logger logger = LoggerFactory.getLogger(FeedInventoryController.class);

    @Autowired
    private FeedInventoryService feedInventoryService;

    @GetMapping("/inventory")
    @ApiOperation(value = "查询饲料库存列表", notes = "返回饲料库存明细（含实时库存状态和预计可用天数）")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "size", value = "每页条数", defaultValue = "10", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "name", value = "饲料名称（模糊搜索）", paramType = "query", dataTypeClass = String.class)
    })
    public ApiResponse listInventory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        logger.info("Query feed inventory: page={}, size={}, name={}", page, size, name);
        IPage<Map<String, Object>> result = feedInventoryService.listInventory(page, size, name);
        return ApiResponse.ok("查询成功")
                .put("records", result.getRecords())
                .put("total", result.getTotal())
                .put("page", result.getCurrent())
                .put("size", result.getSize());
    }

    @PostMapping("/inStock")
    @ApiOperation(value = "饲料入库", notes = "入库操作（事务：增加库存 + 写入入库流水）")
    public ApiResponse inStock(@Valid @RequestBody FeedInStockDTO dto) {
        logger.info("Feed inStock: materialId={}, quantity={}", dto.getMaterialId(), dto.getQuantity());
        Material material = feedInventoryService.inStock(dto);
        return ApiResponse.ok("入库成功")
                .put("materialId", material.getMaterialId())
                .put("materialName", material.getName())
                .put("currentStock", material.getStockQty());
    }

    @PostMapping("/outStock")
    @ApiOperation(value = "饲料出库", notes = "出库操作（事务：扣减库存 + 写出库流水，校验库存不足）")
    public ApiResponse outStock(@Valid @RequestBody FeedOutStockDTO dto) {
        logger.info("Feed outStock: materialId={}, quantity={}, batchId={}", dto.getMaterialId(), dto.getQuantity(), dto.getBatchId());
        try {
            Material material = feedInventoryService.outStock(dto);
            return ApiResponse.ok("出库成功")
                    .put("materialId", material.getMaterialId())
                    .put("materialName", material.getName())
                    .put("currentStock", material.getStockQty());
        } catch (RuntimeException e) {
            logger.warn("Feed outStock failed: {}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/records")
    @ApiOperation(value = "出入库流水明细", notes = "查询饲料出入库流水记录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "size", value = "每页条数", defaultValue = "10", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "materialId", value = "物料ID（可选筛选）", paramType = "query", dataTypeClass = Integer.class)
    })
    public ApiResponse listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer materialId) {
        logger.info("Query feed records: page={}, size={}, materialId={}", page, size, materialId);
        IPage<InventoryRecord> result = feedInventoryService.listRecords(page, size, materialId);
        return ApiResponse.ok("查询成功")
                .put("records", result.getRecords())
                .put("total", result.getTotal())
                .put("page", result.getCurrent())
                .put("size", result.getSize());
    }
}
