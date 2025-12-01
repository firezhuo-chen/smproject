package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.annotation.OperationLog;
import is.smbackend.dto.AppealRequest;
import is.smbackend.pojo.Appeal;
import is.smbackend.service.AppealService;
import is.smbackend.service.NotificationHelper;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "申诉管理")
@RestController
@RequestMapping("/appeal")
public class AppealController {

    @Autowired
    private AppealService appealService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Operation(summary = "获取所有申诉")
    @GetMapping
    public Result<List<Appeal>> getAll() {
        return Result.success(appealService.list());
    }

    @Operation(summary = "根据ID获取申诉")
    @GetMapping("/{id}")
    public Result<Appeal> getById(@PathVariable String id) {
        return Result.success(appealService.getById(id));
    }

    @Operation(summary = "根据学生ID获取申诉列表")
    @GetMapping("/student/{stuId}")
    public Result<List<Appeal>> getByStuId(@PathVariable String stuId) {
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appeal::getStuId, stuId);
        return Result.success(appealService.list(wrapper));
    }

    @Operation(summary = "分页查询申诉")
    @GetMapping("/page")
    public Result<Page<Appeal>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String stuId,
            @RequestParam(required = false) String appealStatus) {
        Page<Appeal> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        if (stuId != null && !stuId.isEmpty()) {
            wrapper.eq(Appeal::getStuId, stuId);
        }
        if (appealStatus != null && !appealStatus.isEmpty()) {
            wrapper.eq(Appeal::getAppealStatus, appealStatus);
        }
        wrapper.orderByDesc(Appeal::getAppealDate);
        return Result.success(appealService.page(page, wrapper));
    }

    @Operation(summary = "新增申诉")
    @OperationLog(operation = "新增", description = "新增申诉记录")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody AppealRequest request) {
        Appeal appeal = new Appeal();
        BeanUtils.copyProperties(request, appeal);
        return Result.success(appealService.save(appeal));
    }

    @Operation(summary = "更新申诉")
    @OperationLog(operation = "更新", description = "更新申诉信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody Appeal appeal) {
        // 获取原记录以比较状态变化
        Appeal oldAppeal = appealService.getById(appeal.getAppealId());
        boolean result = appealService.updateById(appeal);
        
        // 状态变更时发送通知
        if (result && oldAppeal != null) {
            // 辅导员审理状态变更
            if (!oldAppeal.getAdvisorStatus().equals(appeal.getAdvisorStatus()) 
                && !"待审理".equals(appeal.getAdvisorStatus())) {
                notificationHelper.notifyAppealReview(
                    appeal.getStuId(),
                    appeal.getAdvisorStatus(),
                    "辅导员",
                    null
                );
            }
            // 教务审理状态变更
            if (!oldAppeal.getAdminStatus().equals(appeal.getAdminStatus()) 
                && !"待审理".equals(appeal.getAdminStatus())) {
                notificationHelper.notifyAppealReview(
                    appeal.getStuId(),
                    appeal.getAdminStatus(),
                    "教务处",
                    null
                );
            }
            // 最终状态变更
            if (!oldAppeal.getAppealStatus().equals(appeal.getAppealStatus()) 
                && !"受理中".equals(appeal.getAppealStatus())) {
                notificationHelper.notifyAppealFinalReview(
                    appeal.getStuId(),
                    appeal.getAppealStatus()
                );
            }
        }
        return Result.success(result);
    }

    @Operation(summary = "删除申诉")
    @OperationLog(operation = "删除", description = "删除申诉记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(appealService.removeById(id));
    }
}
