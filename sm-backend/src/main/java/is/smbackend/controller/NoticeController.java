package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import is.smbackend.annotation.OperationLog;
import is.smbackend.dto.NoticeRequest;
import is.smbackend.pojo.Notice;
import is.smbackend.service.NoticeService;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "通知管理")
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Operation(summary = "获取所有通知")
    @GetMapping
    public Result<List<Notice>> getAll() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notice::getPublishTime);
        return Result.success(noticeService.list(wrapper));
    }

    @Operation(summary = "根据ID获取通知")
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable String id) {
        return Result.success(noticeService.getById(id));
    }

    @Operation(summary = "根据目标用户获取通知列表")
    @GetMapping("/user/{userId}")
    public Result<List<Notice>> getByUserId(@PathVariable String userId) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getTargetUser, userId);
        wrapper.orderByDesc(Notice::getPublishTime);
        return Result.success(noticeService.list(wrapper));
    }

    @Operation(summary = "分页查询通知")
    @GetMapping("/page")
    public Result<Page<Notice>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String targetUser,
            @RequestParam(required = false) String noticeType) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (targetUser != null && !targetUser.isEmpty()) {
            wrapper.eq(Notice::getTargetUser, targetUser);
        }
        if (noticeType != null && !noticeType.isEmpty()) {
            wrapper.eq(Notice::getNoticeType, noticeType);
        }
        wrapper.orderByDesc(Notice::getPublishTime);
        return Result.success(noticeService.page(page, wrapper));
    }

    @Operation(summary = "新增通知")
    @OperationLog(operation = "新增", description = "发布系统通知")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody NoticeRequest request) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(request, notice);
        return Result.success(noticeService.save(notice));
    }

    @Operation(summary = "更新通知")
    @OperationLog(operation = "更新", description = "更新通知信息")
    @PutMapping
    public Result<Boolean> update(@RequestBody Notice notice) {
        return Result.success(noticeService.updateById(notice));
    }

    @Operation(summary = "删除通知")
    @OperationLog(operation = "删除", description = "删除系统通知")
    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(noticeService.removeById(id));
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable String id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.failed("通知不存在");
        }
        notice.setIsRead(true);
        notice.setReadTime(LocalDateTime.now());
        return Result.success(noticeService.updateById(notice));
    }

    @Operation(summary = "批量标记通知为已读")
    @PutMapping("/read-all")
    public Result<Boolean> markAllAsRead(@RequestParam String userId) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getTargetUser, userId)
               .eq(Notice::getIsRead, false);
        List<Notice> unreadList = noticeService.list(wrapper);
        for (Notice notice : unreadList) {
            notice.setIsRead(true);
            notice.setReadTime(LocalDateTime.now());
            noticeService.updateById(notice);
        }
        return Result.success(true);
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread-count/{userId}")
    public Result<Long> getUnreadCount(@PathVariable String userId) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getTargetUser, userId)
               .eq(Notice::getIsRead, false);
        return Result.success(noticeService.count(wrapper));
    }
}
