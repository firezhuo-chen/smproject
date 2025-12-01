package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.annotation.OperationLog;
import is.smbackend.dto.StatusChangeRequest;
import is.smbackend.pojo.StatusChange;
import is.smbackend.service.StatusChangeService;
import is.smbackend.service.NotificationHelper;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "学籍变动管理")
@RestController
@RequestMapping("/statusChange")
public class StatusChangeController {

    @Autowired
    private StatusChangeService statusChangeService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Operation(summary = "获取所有学籍变动")
    @GetMapping
    public Result<List<StatusChange>> getAll() {
        LambdaQueryWrapper<StatusChange> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StatusChange::getApplyDate);
        return Result.success(statusChangeService.list(wrapper));
    }

    @Operation(summary = "根据ID获取学籍变动")
    @GetMapping("/{id}")
    public Result<StatusChange> getById(@PathVariable String id) {
        return Result.success(statusChangeService.getById(id));
    }

    @Operation(summary = "根据学生ID获取学籍变动列表")
    @GetMapping("/student/{stuId}")
    public Result<List<StatusChange>> getByStuId(@PathVariable String stuId) {
        LambdaQueryWrapper<StatusChange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StatusChange::getStuId, stuId);
        wrapper.orderByDesc(StatusChange::getApplyDate);
        return Result.success(statusChangeService.list(wrapper));
    }

    @Operation(summary = "分页查询学籍变动")
    @GetMapping("/page")
    public Result<Page<StatusChange>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String stuId,
            @RequestParam(required = false) String applyStatus) {
        Page<StatusChange> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StatusChange> wrapper = new LambdaQueryWrapper<>();
        if (stuId != null && !stuId.isEmpty()) {
            wrapper.eq(StatusChange::getStuId, stuId);
        }
        if (applyStatus != null && !applyStatus.isEmpty()) {
            wrapper.eq(StatusChange::getApplyStatus, applyStatus);
        }
        wrapper.orderByDesc(StatusChange::getApplyDate);
        return Result.success(statusChangeService.page(page, wrapper));
    }

    @Operation(summary = "新增学籍变动")
    @OperationLog(operation = "新增", description = "新增学籍变动申请")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody StatusChangeRequest request) {
        StatusChange statusChange = new StatusChange();
        BeanUtils.copyProperties(request, statusChange);
        return Result.success(statusChangeService.save(statusChange));
    }

    @Operation(summary = "更新学籍变动")
    @OperationLog(operation = "更新", description = "更新学籍变动信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody StatusChange statusChange) {
        // 获取原记录以比较状态变化
        StatusChange oldChange = statusChangeService.getById(statusChange.getChangeId());
        boolean result = statusChangeService.updateById(statusChange);
        
        // 状态变更时发送通知
        if (result && oldChange != null) {
            // 辅导员审核状态变更
            if (!oldChange.getAdvisorStatus().equals(statusChange.getAdvisorStatus()) 
                && !"待审核".equals(statusChange.getAdvisorStatus())) {
                notificationHelper.notifyStatusChangeReview(
                    statusChange.getStuId(),
                    statusChange.getChangeType(),
                    statusChange.getAdvisorStatus(),
                    "辅导员",
                    statusChange.getAdvisorId()
                );
            }
            // 教务审核状态变更
            if (!oldChange.getAdminStatus().equals(statusChange.getAdminStatus()) 
                && !"待审核".equals(statusChange.getAdminStatus())) {
                notificationHelper.notifyStatusChangeReview(
                    statusChange.getStuId(),
                    statusChange.getChangeType(),
                    statusChange.getAdminStatus(),
                    "教务处",
                    statusChange.getAdminId()
                );
            }
            // 最终状态变更
            if (!oldChange.getApplyStatus().equals(statusChange.getApplyStatus()) 
                && !"审核中".equals(statusChange.getApplyStatus())) {
                notificationHelper.notifyStatusChangeFinalReview(
                    statusChange.getStuId(),
                    statusChange.getChangeType(),
                    statusChange.getApplyStatus()
                );
            }
        }
        return Result.success(result);
    }

    @Operation(summary = "删除学籍变动")
    @OperationLog(operation = "删除", description = "删除学籍变动记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(statusChangeService.removeById(id));
    }
}
