package org.example.aquabackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.dto.StockingRecordDTO;
import org.example.aquabackend.entity.FarmingBatch;
import org.example.aquabackend.service.FarmingBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/stocking")
@Api(value = "放养记录管理", tags = "放养记录管理（增删改查）")
public class StockingRecordController {

    private static final Logger logger = LoggerFactory.getLogger(StockingRecordController.class);

    @Autowired
    private FarmingBatchService farmingBatchService;

    @GetMapping("/list")
    @ApiOperation(value = "分页查询放养记录", notes = "支持塘口名称模糊搜索 + 品种名称筛选")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "size", value = "每页条数", defaultValue = "10", paramType = "query", dataTypeClass = Integer.class),
        @ApiImplicitParam(name = "pondName", value = "塘口名称（模糊搜索）", paramType = "query", dataTypeClass = String.class),
        @ApiImplicitParam(name = "species", value = "品种名称（精准筛选）", paramType = "query", dataTypeClass = String.class)
    })
    public ApiResponse list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String pondName,
            @RequestParam(required = false) String species) {
        logger.info("Query stocking records: page={}, size={}, pondName={}, species={}", page, size, pondName, species);
        IPage<Map<String, Object>> result = farmingBatchService.list(page, size, pondName, species);
        return ApiResponse.ok("查询成功")
                .put("records", result.getRecords())
                .put("total", result.getTotal())
                .put("page", result.getCurrent())
                .put("size", result.getSize());
    }

    @GetMapping("/listByPond")
    @ApiOperation(value = "按塘口分页查询放养记录", notes = "支持塘口ID筛选、品种名称筛选")
    public ApiResponse listByPond(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer pondId,
            @RequestParam(required = false) String species) {
        IPage<Map<String, Object>> result = farmingBatchService.listByPond(page, size, pondId, species);
        return ApiResponse.ok("查询成功")
                .put("records", result.getRecords())
                .put("total", result.getTotal())
                .put("page", result.getCurrent())
                .put("size", result.getSize());
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增放养记录", notes = "新增一条放养记录")
    public ApiResponse add(@Valid @RequestBody StockingRecordDTO dto) {
        logger.info("Add stocking record: pondId={}, species={}, count={}", dto.getPondId(), dto.getSpecies(), dto.getStockCount());
        FarmingBatch batch = farmingBatchService.add(dto);
        return ApiResponse.ok("添加成功")
                .put("batchId", batch.getBatchId());
    }

    @PutMapping("/update")
    @ApiOperation(value = "编辑放养记录", notes = "编辑已存在的放养记录")
    public ApiResponse update(@Valid @RequestBody StockingRecordDTO dto) {
        logger.info("Update stocking record: batchId={}", dto.getBatchId());
        FarmingBatch batch = farmingBatchService.update(dto);
        return ApiResponse.ok("更新成功")
                .put("batchId", batch.getBatchId());
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除放养记录", notes = "逻辑删除放养记录")
    @ApiImplicitParam(name = "id", value = "批次ID", required = true, paramType = "path", dataTypeClass = Integer.class)
    public ApiResponse delete(@PathVariable Integer id) {
        logger.info("Delete stocking record: batchId={}", id);
        boolean deleted = farmingBatchService.delete(id);
        if (!deleted) {
            return ApiResponse.fail("放养记录不存在");
        }
        return ApiResponse.ok("删除成功");
    }
}
