package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.annotation.OperationLog;
import is.smbackend.pojo.StudentStatusInfo;
import is.smbackend.service.StudentStatusInfoService;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "学籍信息管理")
@RestController
@RequestMapping("/studentStatusInfo")
public class StudentStatusInfoController {

    @Autowired
    private StudentStatusInfoService studentStatusInfoService;

    @Operation(summary = "获取所有学籍信息")
    @GetMapping
    public Result<List<StudentStatusInfo>> getAll() {
        return Result.success(studentStatusInfoService.list());
    }

    @Operation(summary = "根据学生ID获取学籍信息")
    @GetMapping("/{id}")
    public Result<StudentStatusInfo> getById(@PathVariable String id) {
        return Result.success(studentStatusInfoService.getById(id));
    }

    @Operation(summary = "根据辅导员ID获取学生列表")
    @GetMapping("/advisor/{advisorId}")
    public Result<List<StudentStatusInfo>> getByAdvisorId(@PathVariable String advisorId) {
        LambdaQueryWrapper<StudentStatusInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentStatusInfo::getAdvisorId, advisorId);
        return Result.success(studentStatusInfoService.list(wrapper));
    }

    @Operation(summary = "分页查询学籍信息")
    @GetMapping("/page")
    public Result<Page<StudentStatusInfo>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String advisorId,
            @RequestParam(required = false) String academicStatus,
            @RequestParam(required = false) String department) {
        Page<StudentStatusInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StudentStatusInfo> wrapper = new LambdaQueryWrapper<>();
        if (advisorId != null && !advisorId.isEmpty()) {
            wrapper.eq(StudentStatusInfo::getAdvisorId, advisorId);
        }
        if (academicStatus != null && !academicStatus.isEmpty()) {
            wrapper.eq(StudentStatusInfo::getAcademicStatus, academicStatus);
        }
        if (department != null && !department.isEmpty()) {
            wrapper.like(StudentStatusInfo::getDepartment, department);
        }
        return Result.success(studentStatusInfoService.page(page, wrapper));
    }

    @Operation(summary = "新增学籍信息")
    @OperationLog(operation = "新增", description = "新增学籍信息")
    @PostMapping
    public Result<Boolean> save(@RequestBody StudentStatusInfo studentStatusInfo) {
        return Result.success(studentStatusInfoService.save(studentStatusInfo));
    }

    @Operation(summary = "更新学籍信息")
    @OperationLog(operation = "更新", description = "更新学籍信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody StudentStatusInfo studentStatusInfo) {
        return Result.success(studentStatusInfoService.updateById(studentStatusInfo));
    }

    @Operation(summary = "删除学籍信息")
    @OperationLog(operation = "删除", description = "删除学籍信息")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(studentStatusInfoService.removeById(id));
    }
}
