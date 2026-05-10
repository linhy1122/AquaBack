package org.example.aquabackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.dto.PondDTO;
import org.example.aquabackend.entity.Pond;
import org.example.aquabackend.service.PondService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/pond")
@Api(value = "塘口管理", tags = "塘口管理（基础信息 CRUD）")
public class PondController {

    @Autowired
    private PondService pondService;

    @GetMapping("/list")
    @ApiOperation(value = "分页查询塘口列表", notes = "支持名称模糊搜索、状态筛选")
    public ApiResponse list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("塘口名称（模糊搜索）") @RequestParam(required = false) String name,
            @ApiParam("状态（1-使用中, 2-空闲, 3-维修）") @RequestParam(required = false) String status) {

        IPage<Map<String, Object>> result = pondService.list(page, size, name, status);
        return ApiResponse.ok("查询成功")
                .put("records", result.getRecords())
                .put("total", result.getTotal())
                .put("page", result.getCurrent())
                .put("size", result.getSize());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取塘口详情")
    public ApiResponse detail(@ApiParam("塘口ID") @PathVariable Integer id) {
        Pond pond = pondService.getById(id);
        if (pond == null) {
            return ApiResponse.fail("塘口不存在");
        }
        return ApiResponse.ok("查询成功").put("pond", pond);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增塘口")
    public ApiResponse add(@Valid @RequestBody PondDTO dto) {
        Pond pond = pondService.add(dto);
        return ApiResponse.ok("添加成功").put("pondId", pond.getPondId());
    }

    @PutMapping("/update")
    @ApiOperation(value = "编辑塘口")
    public ApiResponse update(@Valid @RequestBody PondDTO dto) {
        if (dto.getPondId() == null) {
            return ApiResponse.fail("编辑时 pondId 不能为空");
        }
        Pond pond = pondService.update(dto);
        return ApiResponse.ok("更新成功").put("pondId", pond.getPondId());
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除塘口（软删除）")
    public ApiResponse delete(@ApiParam("塘口ID") @PathVariable Integer id) {
        boolean flag = pondService.delete(id);
        if (flag) {
            return ApiResponse.ok("删除成功");
        }
        return ApiResponse.fail("塘口不存在");
    }
}
