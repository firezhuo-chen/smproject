package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.annotation.OperationLog;
import is.smbackend.dto.PunishmentRequest;
import is.smbackend.pojo.Punishment;
import is.smbackend.service.PunishmentService;
import is.smbackend.service.NotificationHelper;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "处分管理")
@RestController
@RequestMapping("/punishment")
public class PunishmentController {

    @Autowired
    private PunishmentService punishmentService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Operation(summary = "获取所有处分")
    @GetMapping
    public Result<List<Punishment>> getAll() {
        return Result.success(punishmentService.list());
    }

    @Operation(summary = "根据ID获取处分")
    @GetMapping("/{id}")
    public Result<Punishment> getById(@PathVariable String id) {
        return Result.success(punishmentService.getById(id));
    }

    @Operation(summary = "根据学生ID获取处分列表")
    @GetMapping("/student/{stuId}")
    public Result<List<Punishment>> getByStuId(@PathVariable String stuId) {
        LambdaQueryWrapper<Punishment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Punishment::getStuId, stuId);
        return Result.success(punishmentService.list(wrapper));
    }

    @Operation(summary = "分页查询处分")
    @GetMapping("/page")
    public Result<Page<Punishment>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String stuId,
            @RequestParam(required = false) String punishmentStatus) {
        Page<Punishment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Punishment> wrapper = new LambdaQueryWrapper<>();
        if (stuId != null && !stuId.isEmpty()) {
            wrapper.eq(Punishment::getStuId, stuId);
        }
        if (punishmentStatus != null && !punishmentStatus.isEmpty()) {
            wrapper.eq(Punishment::getPunishmentStatus, punishmentStatus);
        }
        wrapper.orderByDesc(Punishment::getApplyDate);
        return Result.success(punishmentService.page(page, wrapper));
    }

    @Operation(summary = "新增处分")
    @OperationLog(operation = "新增", description = "新增处分记录")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody PunishmentRequest request) {
        Punishment punishment = new Punishment();
        BeanUtils.copyProperties(request, punishment);
        return Result.success(punishmentService.save(punishment));
    }

    @Operation(summary = "更新处分")
    @OperationLog(operation = "更新", description = "更新处分信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody Punishment punishment) {
        // 获取原记录以比较状态变化
        Punishment oldPunishment = punishmentService.getById(punishment.getPunishmentId());
        boolean result = punishmentService.updateById(punishment);
        
        // 状态变更时发送通知
        if (result && oldPunishment != null) {
            String oldStatus = oldPunishment.getPunishmentStatus();
            String newStatus = punishment.getPunishmentStatus();
            
            if (!oldStatus.equals(newStatus)) {
                // 处分生效或撤销时通知学生
                if ("已生效".equals(newStatus) || "已撤销".equals(newStatus)) {
                    notificationHelper.notifyPunishment(
                        punishment.getStuId(),
                        punishment.getPunishmentType(),
                        newStatus,
                        "教务处",
                        null
                    );
                }
            }
        }
        return Result.success(result);
    }

    @Operation(summary = "删除处分")
    @OperationLog(operation = "删除", description = "删除处分记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(punishmentService.removeById(id));
    }
}
