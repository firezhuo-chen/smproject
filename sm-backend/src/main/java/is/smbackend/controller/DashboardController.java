package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import is.smbackend.dto.DashboardStats;
import is.smbackend.pojo.*;
import is.smbackend.response.Result;
import is.smbackend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘控制器
 */
@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private StudentBasicInfoService studentBasicInfoService;
    @Autowired
    private StudentStatusInfoService studentStatusInfoService;
    @Autowired
    private AwardService awardService;
    @Autowired
    private PunishmentService punishmentService;
    @Autowired
    private AppealService appealService;
    @Autowired
    private StatusChangeService statusChangeService;
    @Autowired
    private LeaveSchoolService leaveSchoolService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private LogService logService;
    @Autowired
    private UserStudentService userStudentService;
    @Autowired
    private UserAdvisorService userAdvisorService;
    @Autowired
    private UserAdminService userAdminService;

    @Operation(summary = "获取学生仪表盘数据")
    @GetMapping("/student/{stuId}")
    public Result<DashboardStats> getStudentDashboard(@PathVariable String stuId) {
        DashboardStats stats = new DashboardStats();
        Map<String, Long> counts = new LinkedHashMap<>();
        
        // 获取学生基本信息
        StudentBasicInfo basicInfo = studentBasicInfoService.getById(stuId);
        StudentStatusInfo statusInfo = studentStatusInfoService.getById(stuId);
        
        if (basicInfo != null && statusInfo != null) {
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("stuId", basicInfo.getStuId());
            userInfo.put("name", basicInfo.getName());
            userInfo.put("gender", basicInfo.getGender());
            userInfo.put("department", statusInfo.getDepartment());
            userInfo.put("major", statusInfo.getMajor());
            userInfo.put("className", statusInfo.getClassName());
            userInfo.put("academicStatus", statusInfo.getAcademicStatus());
            userInfo.put("warningLevel", statusInfo.getWarningLevel());
            userInfo.put("registerStatus", statusInfo.getRegisterStatus());
            stats.setUserInfo(userInfo);
        }
        
        // 统计数量
        LambdaQueryWrapper<Award> awardWrapper = new LambdaQueryWrapper<>();
        awardWrapper.eq(Award::getStuId, stuId);
        counts.put("awards", awardService.count(awardWrapper));
        
        LambdaQueryWrapper<Punishment> punishmentWrapper = new LambdaQueryWrapper<>();
        punishmentWrapper.eq(Punishment::getStuId, stuId).eq(Punishment::getPunishmentStatus, "已生效");
        counts.put("punishments", punishmentService.count(punishmentWrapper));
        
        LambdaQueryWrapper<Appeal> appealWrapper = new LambdaQueryWrapper<>();
        appealWrapper.eq(Appeal::getStuId, stuId).eq(Appeal::getAppealStatus, "受理中");
        counts.put("pendingAppeals", appealService.count(appealWrapper));
        
        LambdaQueryWrapper<Notice> noticeWrapper = new LambdaQueryWrapper<>();
        noticeWrapper.eq(Notice::getTargetUser, stuId);
        counts.put("notices", noticeService.count(noticeWrapper));
        
        stats.setCounts(counts);
        
        // 待办事项
        List<DashboardStats.TodoItem> todoList = new ArrayList<>();
        
        // 待审批的奖励申请
        LambdaQueryWrapper<Award> pendingAwardWrapper = new LambdaQueryWrapper<>();
        pendingAwardWrapper.eq(Award::getStuId, stuId).eq(Award::getAwardStatus, "审批中");
        List<Award> pendingAwards = awardService.list(pendingAwardWrapper);
        for (Award award : pendingAwards) {
            DashboardStats.TodoItem item = new DashboardStats.TodoItem();
            item.setId(award.getAwardId());
            item.setType("award");
            item.setTitle("奖励申请审批中");
            item.setDescription(award.getAwardName());
            item.setTime(award.getApplyDate() != null ? award.getApplyDate().toString() : "");
            item.setStatus("pending");
            item.setLink("/student/award");
            todoList.add(item);
        }
        
        // 待审核的学籍变动
        LambdaQueryWrapper<StatusChange> pendingChangeWrapper = new LambdaQueryWrapper<>();
        pendingChangeWrapper.eq(StatusChange::getStuId, stuId).eq(StatusChange::getApplyStatus, "待审核");
        List<StatusChange> pendingChanges = statusChangeService.list(pendingChangeWrapper);
        for (StatusChange change : pendingChanges) {
            DashboardStats.TodoItem item = new DashboardStats.TodoItem();
            item.setId(change.getChangeId());
            item.setType("statusChange");
            item.setTitle("学籍变动申请待审核");
            item.setDescription(change.getChangeType() + " - " + change.getChangeReason());
            item.setTime(change.getApplyDate() != null ? change.getApplyDate().toString() : "");
            item.setStatus("pending");
            item.setLink("/student/status-change");
            todoList.add(item);
        }
        
        stats.setTodoList(todoList);
        
        // 最新通知
        LambdaQueryWrapper<Notice> recentNoticeWrapper = new LambdaQueryWrapper<>();
        recentNoticeWrapper.eq(Notice::getTargetUser, stuId)
                .orderByDesc(Notice::getPublishTime)
                .last("LIMIT 5");
        List<Notice> recentNotices = noticeService.list(recentNoticeWrapper);
        List<Map<String, Object>> noticeList = recentNotices.stream().map(n -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("noticeId", n.getNoticeId());
            map.put("title", n.getTitle());
            map.put("noticeType", n.getNoticeType());
            map.put("publishTime", n.getPublishTime());
            map.put("priority", n.getPriority());
            return map;
        }).collect(Collectors.toList());
        stats.setNotices(noticeList);
        
        return Result.success(stats);
    }

    @Operation(summary = "获取辅导员仪表盘数据")
    @GetMapping("/advisor/{advisorId}")
    public Result<DashboardStats> getAdvisorDashboard(@PathVariable String advisorId) {
        DashboardStats stats = new DashboardStats();
        Map<String, Long> counts = new LinkedHashMap<>();
        
        // 获取辅导员信息
        UserAdvisor advisor = userAdvisorService.getById(advisorId);
        if (advisor != null) {
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("userId", advisor.getUserId());
            userInfo.put("userName", advisor.getUserName());
            stats.setUserInfo(userInfo);
        }
        
        // 获取负责的学生
        LambdaQueryWrapper<StudentStatusInfo> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(StudentStatusInfo::getAdvisorId, advisorId);
        List<StudentStatusInfo> myStudents = studentStatusInfoService.list(studentWrapper);
        List<String> myStudentIds = myStudents.stream().map(StudentStatusInfo::getStuId).collect(Collectors.toList());
        
        counts.put("totalStudents", (long) myStudents.size());
        
        // 统计待审批数量
        if (!myStudentIds.isEmpty()) {
            LambdaQueryWrapper<Award> pendingAwardWrapper = new LambdaQueryWrapper<>();
            pendingAwardWrapper.in(Award::getStuId, myStudentIds).eq(Award::getAdvisorStatus, "待审批");
            counts.put("pendingAwards", awardService.count(pendingAwardWrapper));
            
            LambdaQueryWrapper<Appeal> pendingAppealWrapper = new LambdaQueryWrapper<>();
            pendingAppealWrapper.in(Appeal::getStuId, myStudentIds).eq(Appeal::getAdvisorStatus, "待审理");
            counts.put("pendingAppeals", appealService.count(pendingAppealWrapper));
        } else {
            counts.put("pendingAwards", 0L);
            counts.put("pendingAppeals", 0L);
        }
        
        // 预警学生
        long warningCount = myStudents.stream()
                .filter(s -> s.getWarningLevel() != null && !"无".equals(s.getWarningLevel()))
                .count();
        counts.put("warningStudents", warningCount);
        
        stats.setCounts(counts);
        
        // 预警学生列表
        List<Map<String, Object>> warningStudentList = new ArrayList<>();
        for (StudentStatusInfo student : myStudents) {
            if (student.getWarningLevel() != null && !"无".equals(student.getWarningLevel())) {
                StudentBasicInfo basicInfo = studentBasicInfoService.getById(student.getStuId());
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("stuId", student.getStuId());
                map.put("name", basicInfo != null ? basicInfo.getName() : "");
                map.put("major", student.getMajor());
                map.put("className", student.getClassName());
                map.put("warningLevel", student.getWarningLevel());
                warningStudentList.add(map);
            }
        }
        stats.setWarningStudents(warningStudentList);
        
        // 待办事项
        List<DashboardStats.TodoItem> todoList = new ArrayList<>();
        if (!myStudentIds.isEmpty()) {
            // 待审批奖励
            LambdaQueryWrapper<Award> awardWrapper = new LambdaQueryWrapper<>();
            awardWrapper.in(Award::getStuId, myStudentIds).eq(Award::getAdvisorStatus, "待审批").last("LIMIT 5");
            List<Award> pendingAwards = awardService.list(awardWrapper);
            for (Award award : pendingAwards) {
                DashboardStats.TodoItem item = new DashboardStats.TodoItem();
                item.setId(award.getAwardId());
                item.setType("award");
                item.setTitle("奖励申请待审批");
                item.setDescription(award.getStuId() + " - " + award.getAwardName());
                item.setTime(award.getApplyDate() != null ? award.getApplyDate().toString() : "");
                item.setStatus("pending");
                item.setLink("/advisor/award-review");
                todoList.add(item);
            }
            
            // 待审理申诉
            LambdaQueryWrapper<Appeal> appealWrapper = new LambdaQueryWrapper<>();
            appealWrapper.in(Appeal::getStuId, myStudentIds).eq(Appeal::getAdvisorStatus, "待审理").last("LIMIT 5");
            List<Appeal> pendingAppeals = appealService.list(appealWrapper);
            for (Appeal appeal : pendingAppeals) {
                DashboardStats.TodoItem item = new DashboardStats.TodoItem();
                item.setId(appeal.getAppealId());
                item.setType("appeal");
                item.setTitle("申诉待审理");
                item.setDescription(appeal.getStuId() + " - " + appeal.getAppealReason());
                item.setTime(appeal.getAppealDate() != null ? appeal.getAppealDate().toString() : "");
                item.setStatus("pending");
                item.setLink("/advisor/appeal-review");
                todoList.add(item);
            }
        }
        stats.setTodoList(todoList);
        
        return Result.success(stats);
    }

    @Operation(summary = "获取管理员仪表盘数据")
    @GetMapping("/admin")
    public Result<DashboardStats> getAdminDashboard(
            @RequestParam(required = false) String adminId,
            @RequestParam(required = false) String role) {
        DashboardStats stats = new DashboardStats();
        Map<String, Long> counts = new LinkedHashMap<>();
        
        boolean isEducationAdmin = "教务管理员".equals(role);
        
        // 系统概览（仅教务管理员需要）
        if (isEducationAdmin) {
            counts.put("totalStudents", studentBasicInfoService.count());
            counts.put("totalAdvisors", userAdvisorService.count());
            counts.put("totalAdmins", userAdminService.count());
        }
        
        // 待审批统计
        if (isEducationAdmin) {
            LambdaQueryWrapper<Award> pendingAwardWrapper = new LambdaQueryWrapper<>();
            pendingAwardWrapper.eq(Award::getAdvisorStatus, "已通过").eq(Award::getAdminStatus, "待审批");
            counts.put("pendingAwards", awardService.count(pendingAwardWrapper));
            
            LambdaQueryWrapper<Punishment> pendingPunishmentWrapper = new LambdaQueryWrapper<>();
            pendingPunishmentWrapper.eq(Punishment::getAdminStatus, "待审批");
            counts.put("pendingPunishments", punishmentService.count(pendingPunishmentWrapper));
            
            LambdaQueryWrapper<Appeal> pendingAppealWrapper = new LambdaQueryWrapper<>();
            pendingAppealWrapper.eq(Appeal::getAdvisorStatus, "已通过").eq(Appeal::getAdminStatus, "待审理");
            counts.put("pendingAppeals", appealService.count(pendingAppealWrapper));
            
            LambdaQueryWrapper<StatusChange> pendingChangeWrapper = new LambdaQueryWrapper<>();
            pendingChangeWrapper.eq(StatusChange::getApplyStatus, "待审核");
            counts.put("pendingStatusChanges", statusChangeService.count(pendingChangeWrapper));
            
            // 教务管理员看所有待审离校
            LambdaQueryWrapper<LeaveSchool> pendingLeaveWrapper = new LambdaQueryWrapper<>();
            pendingLeaveWrapper.eq(LeaveSchool::getOverallStatus, "审核中");
            counts.put("pendingLeaves", leaveSchoolService.count(pendingLeaveWrapper));
            
            // 预警学生数
            LambdaQueryWrapper<StudentStatusInfo> warningWrapper = new LambdaQueryWrapper<>();
            warningWrapper.ne(StudentStatusInfo::getWarningLevel, "无")
                    .isNotNull(StudentStatusInfo::getWarningLevel);
            counts.put("warningStudents", studentStatusInfoService.count(warningWrapper));
        } else {
            // 非教务管理员：根据角色统计待审离校数量
            LambdaQueryWrapper<LeaveSchool> pendingLeaveWrapper = new LambdaQueryWrapper<>();
            pendingLeaveWrapper.eq(LeaveSchool::getOverallStatus, "审核中");
            
            // 根据角色筛选对应环节待审核的
            if ("宿管管理员".equals(role)) {
                pendingLeaveWrapper.eq(LeaveSchool::getDormitoryStatus, "待审核");
            } else if ("图书馆管理员".equals(role)) {
                pendingLeaveWrapper.eq(LeaveSchool::getLibraryStatus, "待审核");
            } else if ("财务处管理员".equals(role)) {
                pendingLeaveWrapper.eq(LeaveSchool::getFinanceStatus, "待审核");
            }
            counts.put("pendingLeaves", leaveSchoolService.count(pendingLeaveWrapper));
            
            // 统计该管理员提交的处分申请数量
            if (adminId != null) {
                LambdaQueryWrapper<Punishment> myPunishmentWrapper = new LambdaQueryWrapper<>();
                myPunishmentWrapper.eq(Punishment::getApplicantId, adminId);
                counts.put("myPunishmentApplications", punishmentService.count(myPunishmentWrapper));
            } else {
                counts.put("myPunishmentApplications", 0L);
            }
        }
        
        stats.setCounts(counts);
        
        // 待办事项
        List<DashboardStats.TodoItem> todoList = new ArrayList<>();
        
        if (isEducationAdmin) {
            // 教务管理员：待终审奖励
            LambdaQueryWrapper<Award> awardWrapper = new LambdaQueryWrapper<>();
            awardWrapper.eq(Award::getAdvisorStatus, "已通过").eq(Award::getAdminStatus, "待审批").last("LIMIT 3");
            List<Award> pendingAwards = awardService.list(awardWrapper);
            for (Award award : pendingAwards) {
                DashboardStats.TodoItem item = new DashboardStats.TodoItem();
                item.setId(award.getAwardId());
                item.setType("award");
                item.setTitle("奖励待终审");
                item.setDescription(award.getStuId() + " - " + award.getAwardName());
                item.setTime(award.getApplyDate() != null ? award.getApplyDate().toString() : "");
                item.setLink("/admin/award-review");
                todoList.add(item);
            }
            
            // 教务管理员：待审批处分
            LambdaQueryWrapper<Punishment> punishmentWrapper = new LambdaQueryWrapper<>();
            punishmentWrapper.eq(Punishment::getAdminStatus, "待审批").last("LIMIT 3");
            List<Punishment> pendingPunishments = punishmentService.list(punishmentWrapper);
            for (Punishment p : pendingPunishments) {
                DashboardStats.TodoItem item = new DashboardStats.TodoItem();
                item.setId(p.getPunishmentId());
                item.setType("punishment");
                item.setTitle("处分待审批");
                item.setDescription(p.getStuId() + " - " + p.getPunishmentType());
                item.setTime(p.getApplyDate() != null ? p.getApplyDate().toString() : "");
                item.setLink("/admin/punishment");
                todoList.add(item);
            }
            
            // 最近日志（仅教务管理员）
            LambdaQueryWrapper<Log> logWrapper = new LambdaQueryWrapper<>();
            logWrapper.orderByDesc(Log::getOperationTime).last("LIMIT 10");
            List<Log> recentLogs = logService.list(logWrapper);
            List<Map<String, Object>> logList = recentLogs.stream().map(log -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("logId", log.getLogId());
                map.put("userId", log.getUserId());
                map.put("userName", log.getUserName());
                map.put("operation", log.getOperation());
                map.put("operationDetail", log.getOperationDetail());
                map.put("operationTime", log.getOperationTime());
                map.put("result", log.getResult());
                return map;
            }).collect(Collectors.toList());
            stats.setLogs(logList);
            
            // 预警学生列表（仅教务管理员）
            LambdaQueryWrapper<StudentStatusInfo> warningStudentWrapper = new LambdaQueryWrapper<>();
            warningStudentWrapper.ne(StudentStatusInfo::getWarningLevel, "无")
                    .isNotNull(StudentStatusInfo::getWarningLevel)
                    .last("LIMIT 10");
            List<StudentStatusInfo> warningStudents = studentStatusInfoService.list(warningStudentWrapper);
            List<Map<String, Object>> warningList = new ArrayList<>();
            for (StudentStatusInfo student : warningStudents) {
                StudentBasicInfo basicInfo = studentBasicInfoService.getById(student.getStuId());
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("stuId", student.getStuId());
                map.put("name", basicInfo != null ? basicInfo.getName() : "");
                map.put("department", student.getDepartment());
                map.put("major", student.getMajor());
                map.put("warningLevel", student.getWarningLevel());
                warningList.add(map);
            }
            stats.setWarningStudents(warningList);
        } else {
            // 非教务管理员：待审核离校申请
            LambdaQueryWrapper<LeaveSchool> leaveWrapper = new LambdaQueryWrapper<>();
            leaveWrapper.eq(LeaveSchool::getOverallStatus, "审核中");
            if ("宿管管理员".equals(role)) {
                leaveWrapper.eq(LeaveSchool::getDormitoryStatus, "待审核");
            } else if ("图书馆管理员".equals(role)) {
                leaveWrapper.eq(LeaveSchool::getLibraryStatus, "待审核");
            } else if ("财务处管理员".equals(role)) {
                leaveWrapper.eq(LeaveSchool::getFinanceStatus, "待审核");
            }
            leaveWrapper.last("LIMIT 5");
            List<LeaveSchool> pendingLeaves = leaveSchoolService.list(leaveWrapper);
            for (LeaveSchool leave : pendingLeaves) {
                StudentBasicInfo student = studentBasicInfoService.getById(leave.getStuId());
                DashboardStats.TodoItem item = new DashboardStats.TodoItem();
                item.setId(leave.getLeaveId());
                item.setType("leave");
                item.setTitle("离校手续待审核");
                item.setDescription(leave.getStuId() + " - " + (student != null ? student.getName() : "") + " - " + leave.getLeaveType());
                item.setTime(leave.getApplyDate() != null ? leave.getApplyDate().toString() : "");
                item.setLink("/admin/leave-review");
                todoList.add(item);
            }
            
            // 非教务管理员：我提交的待审批处分
            if (adminId != null) {
                LambdaQueryWrapper<Punishment> myPunishmentWrapper = new LambdaQueryWrapper<>();
                myPunishmentWrapper.eq(Punishment::getApplicantId, adminId)
                        .eq(Punishment::getAdminStatus, "待审批")
                        .last("LIMIT 3");
                List<Punishment> myPunishments = punishmentService.list(myPunishmentWrapper);
                for (Punishment p : myPunishments) {
                    DashboardStats.TodoItem item = new DashboardStats.TodoItem();
                    item.setId(p.getPunishmentId());
                    item.setType("punishment");
                    item.setTitle("我的处分申请待审批");
                    item.setDescription(p.getStuId() + " - " + p.getPunishmentType());
                    item.setTime(p.getApplyDate() != null ? p.getApplyDate().toString() : "");
                    item.setLink("/admin/punishment");
                    todoList.add(item);
                }
            }
        }
        
        stats.setTodoList(todoList);
        
        return Result.success(stats);
    }
}
