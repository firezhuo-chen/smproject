package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.annotation.OperationLog;
import is.smbackend.dto.AwardRequest;
import is.smbackend.pojo.Award;
import is.smbackend.service.AwardService;
import is.smbackend.service.NotificationHelper;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "奖励管理")
@RestController
@RequestMapping("/award")
public class AwardController {

    @Autowired
    private AwardService awardService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Operation(summary = "获取所有奖励")
    @GetMapping
    public Result<List<Award>> getAll() {
        return Result.success(awardService.list());
    }

    @Operation(summary = "根据ID获取奖励")
    @GetMapping("/{id}")
    public Result<Award> getById(@PathVariable String id) {
        return Result.success(awardService.getById(id));
    }

    @Operation(summary = "根据学生ID获取奖励列表")
    @GetMapping("/student/{stuId}")
    public Result<List<Award>> getByStuId(@PathVariable String stuId) {
        LambdaQueryWrapper<Award> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Award::getStuId, stuId);
        return Result.success(awardService.list(wrapper));
    }

    @Operation(summary = "分页查询奖励")
    @GetMapping("/page")
    public Result<Page<Award>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String stuId,
            @RequestParam(required = false) String awardStatus) {
        Page<Award> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Award> wrapper = new LambdaQueryWrapper<>();
        if (stuId != null && !stuId.isEmpty()) {
            wrapper.eq(Award::getStuId, stuId);
        }
        if (awardStatus != null && !awardStatus.isEmpty()) {
            wrapper.eq(Award::getAwardStatus, awardStatus);
        }
        wrapper.orderByDesc(Award::getApplyDate);
        return Result.success(awardService.page(page, wrapper));
    }

    @Operation(summary = "新增奖励")
    @OperationLog(operation = "新增", description = "新增奖励申请")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody AwardRequest request) {
        Award award = new Award();
        BeanUtils.copyProperties(request, award);
        return Result.success(awardService.save(award));
    }

    @Operation(summary = "更新奖励")
    @OperationLog(operation = "更新", description = "更新奖励信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody Award award) {
        // 获取原记录以比较状态变化
        Award oldAward = awardService.getById(award.getAwardId());
        boolean result = awardService.updateById(award);
        
        // 状态变更时发送通知
        if (result && oldAward != null) {
            // 辅导员审批状态变更
            if (!oldAward.getAdvisorStatus().equals(award.getAdvisorStatus()) 
                && !"待审批".equals(award.getAdvisorStatus())) {
                notificationHelper.notifyAwardApproval(
                    award.getStuId(), 
                    award.getAwardName(), 
                    award.getAdvisorStatus(), 
                    "辅导员", 
                    null
                );
            }
            // 教务审批状态变更
            if (!oldAward.getAdminStatus().equals(award.getAdminStatus()) 
                && !"待审批".equals(award.getAdminStatus())) {
                notificationHelper.notifyAwardApproval(
                    award.getStuId(), 
                    award.getAwardName(), 
                    award.getAdminStatus(), 
                    "教务处", 
                    null
                );
            }
            // 最终状态变更
            if (!oldAward.getAwardStatus().equals(award.getAwardStatus()) 
                && !"审批中".equals(award.getAwardStatus())) {
                notificationHelper.notifyAwardFinalApproval(
                    award.getStuId(), 
                    award.getAwardName(), 
                    award.getAwardStatus()
                );
            }
        }
        return Result.success(result);
    }

    @Operation(summary = "删除奖励")
    @OperationLog(operation = "删除", description = "删除奖励记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(awardService.removeById(id));
    }
}
