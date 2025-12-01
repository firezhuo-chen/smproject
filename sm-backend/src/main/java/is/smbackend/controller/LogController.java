package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.pojo.Log;
import is.smbackend.service.LogService;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    private LogService logService;

    @Operation(summary = "获取所有日志")
    @GetMapping
    public Result<List<Log>> getAll() {
        LambdaQueryWrapper<Log> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Log::getOperationTime);
        return Result.success(logService.list(wrapper));
    }

    @Operation(summary = "根据ID获取日志")
    @GetMapping("/{id}")
    public Result<Log> getById(@PathVariable String id) {
        return Result.success(logService.getById(id));
    }

    @Operation(summary = "根据用户ID获取日志")
    @GetMapping("/user/{userId}")
    public Result<List<Log>> getByUserId(@PathVariable String userId) {
        LambdaQueryWrapper<Log> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Log::getUserId, userId);
        wrapper.orderByDesc(Log::getOperationTime);
        return Result.success(logService.list(wrapper));
    }

    @Operation(summary = "分页查询日志")
    @GetMapping("/page")
    public Result<Page<Log>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String operation) {
        Page<Log> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Log> wrapper = new LambdaQueryWrapper<>();
        if (userId != null && !userId.isEmpty()) {
            wrapper.eq(Log::getUserId, userId);
        }
        if (operation != null && !operation.isEmpty()) {
            wrapper.eq(Log::getOperation, operation);
        }
        wrapper.orderByDesc(Log::getOperationTime);
        return Result.success(logService.page(page, wrapper));
    }

    @Operation(summary = "新增日志")
    @PostMapping
    public Result<Boolean> save(@RequestBody Log log) {
        return Result.success(logService.save(log));
    }

    @Operation(summary = "删除日志")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(logService.removeById(id));
    }
}
