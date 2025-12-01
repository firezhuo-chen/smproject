package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.annotation.OperationLog;
import is.smbackend.dto.LeaveSchoolRequest;
import is.smbackend.pojo.LeaveSchool;
import is.smbackend.service.LeaveSchoolService;
import is.smbackend.service.NotificationHelper;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "离校管理")
@RestController
@RequestMapping("/leaveSchool")
public class LeaveSchoolController {

    @Autowired
    private LeaveSchoolService leaveSchoolService;

    @Autowired
    private NotificationHelper notificationHelper;

    @Operation(summary = "获取所有离校记录")
    @GetMapping
    public Result<List<LeaveSchool>> getAll() {
        LambdaQueryWrapper<LeaveSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LeaveSchool::getApplyDate);
        return Result.success(leaveSchoolService.list(wrapper));
    }

    @Operation(summary = "根据ID获取离校记录")
    @GetMapping("/{id}")
    public Result<LeaveSchool> getById(@PathVariable String id) {
        return Result.success(leaveSchoolService.getById(id));
    }

    @Operation(summary = "根据学生ID获取离校记录")
    @GetMapping("/student/{stuId}")
    public Result<List<LeaveSchool>> getByStuId(@PathVariable String stuId) {
        LambdaQueryWrapper<LeaveSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaveSchool::getStuId, stuId);
        wrapper.orderByDesc(LeaveSchool::getApplyDate);
        return Result.success(leaveSchoolService.list(wrapper));
    }

    @Operation(summary = "分页查询离校记录")
    @GetMapping("/page")
    public Result<Page<LeaveSchool>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String stuId,
            @RequestParam(required = false) String overallStatus) {
        Page<LeaveSchool> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LeaveSchool> wrapper = new LambdaQueryWrapper<>();
        if (stuId != null && !stuId.isEmpty()) {
            wrapper.eq(LeaveSchool::getStuId, stuId);
        }
        if (overallStatus != null && !overallStatus.isEmpty()) {
            wrapper.eq(LeaveSchool::getOverallStatus, overallStatus);
        }
        wrapper.orderByDesc(LeaveSchool::getApplyDate);
        return Result.success(leaveSchoolService.page(page, wrapper));
    }

    @Operation(summary = "新增离校记录")
    @OperationLog(operation = "新增", description = "新增离校申请")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody LeaveSchoolRequest request) {
        LeaveSchool leaveSchool = new LeaveSchool();
        BeanUtils.copyProperties(request, leaveSchool);
        return Result.success(leaveSchoolService.save(leaveSchool));
    }

    @Operation(summary = "更新离校记录")
    @OperationLog(operation = "更新", description = "更新离校信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody LeaveSchool leaveSchool) {
        // 获取原记录以比较状态变化
        LeaveSchool oldLeave = leaveSchoolService.getById(leaveSchool.getLeaveId());
        boolean result = leaveSchoolService.updateById(leaveSchool);
        
        // 状态变更时发送通知
        if (result && oldLeave != null) {
            // 宿管审核状态变更
            if (!oldLeave.getDormitoryStatus().equals(leaveSchool.getDormitoryStatus()) 
                && !"待审核".equals(leaveSchool.getDormitoryStatus())) {
                notificationHelper.notifyLeaveSchoolReview(
                    leaveSchool.getStuId(),
                    "宿管审核",
                    leaveSchool.getDormitoryStatus(),
                    "宿管处",
                    leaveSchool.getDormitoryReviewerId()
                );
            }
            // 图书馆审核状态变更
            if (!oldLeave.getLibraryStatus().equals(leaveSchool.getLibraryStatus()) 
                && !"待审核".equals(leaveSchool.getLibraryStatus())) {
                notificationHelper.notifyLeaveSchoolReview(
                    leaveSchool.getStuId(),
                    "图书馆审核",
                    leaveSchool.getLibraryStatus(),
                    "图书馆",
                    leaveSchool.getLibraryReviewerId()
                );
            }
            // 财务处审核状态变更
            if (!oldLeave.getFinanceStatus().equals(leaveSchool.getFinanceStatus()) 
                && !"待审核".equals(leaveSchool.getFinanceStatus())) {
                notificationHelper.notifyLeaveSchoolReview(
                    leaveSchool.getStuId(),
                    "财务处审核",
                    leaveSchool.getFinanceStatus(),
                    "财务处",
                    leaveSchool.getFinanceReviewerId()
                );
            }
            // 教务处审核状态变更
            if (!oldLeave.getAdminStatus().equals(leaveSchool.getAdminStatus()) 
                && !"待审核".equals(leaveSchool.getAdminStatus())) {
                notificationHelper.notifyLeaveSchoolReview(
                    leaveSchool.getStuId(),
                    "教务处审核",
                    leaveSchool.getAdminStatus(),
                    "教务处",
                    leaveSchool.getAdminId()
                );
            }
            // 整体状态变更
            if (!oldLeave.getOverallStatus().equals(leaveSchool.getOverallStatus()) 
                && !"审核中".equals(leaveSchool.getOverallStatus())) {
                notificationHelper.notifyLeaveSchoolFinalReview(
                    leaveSchool.getStuId(),
                    leaveSchool.getOverallStatus()
                );
            }
        }
        return Result.success(result);
    }

    @Operation(summary = "删除离校记录")
    @OperationLog(operation = "删除", description = "删除离校记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(leaveSchoolService.removeById(id));
    }
}
