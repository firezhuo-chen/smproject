package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.pojo.StudentBasicInfo;
import is.smbackend.service.StudentBasicInfoService;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "学生基本信息管理")
@RestController
@RequestMapping("/studentBasicInfo")
public class StudentBasicInfoController {

    @Autowired
    private StudentBasicInfoService studentBasicInfoService;

    @Operation(summary = "获取所有学生基本信息")
    @GetMapping
    public Result<List<StudentBasicInfo>> getAll() {
        return Result.success(studentBasicInfoService.list());
    }

    @Operation(summary = "根据学生ID获取基本信息")
    @GetMapping("/{id}")
    public Result<StudentBasicInfo> getById(@PathVariable String id) {
        return Result.success(studentBasicInfoService.getById(id));
    }

    @Operation(summary = "分页查询学生基本信息")
    @GetMapping("/page")
    public Result<Page<StudentBasicInfo>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<StudentBasicInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StudentBasicInfo> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(StudentBasicInfo::getStuId, keyword)
                    .or().like(StudentBasicInfo::getName, keyword)
                    .or().like(StudentBasicInfo::getPhone, keyword);
        }
        return Result.success(studentBasicInfoService.page(page, wrapper));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody StudentBasicInfo studentBasicInfo) {
        return Result.success(studentBasicInfoService.save(studentBasicInfo));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody StudentBasicInfo studentBasicInfo) {
        return Result.success(studentBasicInfoService.updateById(studentBasicInfo));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(studentBasicInfoService.removeById(id));
    }
}
