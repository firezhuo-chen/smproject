package is.smbackend.service;

import is.smbackend.pojo.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通知助手类 - 用于审批状态变更时自动发送通知
 */
@Component
public class NotificationHelper {

    @Autowired
    private NoticeService noticeService;
    
    // 用于生成唯一ID的计数器
    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 生成通知ID - 时间戳 + 3位序列号，避免同一秒内主键冲突
     */
    private String generateNoticeId() {
        int seq = counter.incrementAndGet() % 1000;
        return "N" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
               + String.format("%03d", seq);
    }

    /**
     * 发送通知给学生
     */
    public void notifyStudent(String stuId, String title, String content, String noticeType, String publishUser, String publishUserId) {
        Notice notice = new Notice();
        notice.setNoticeId(generateNoticeId());
        notice.setTitle(title);
        notice.setContent(content);
        notice.setNoticeType(noticeType);
        notice.setTargetUser(stuId);
        notice.setTargetType("student");
        notice.setPublishUser(publishUser);
        notice.setPublishUserId(publishUserId);
        notice.setPublishTime(LocalDateTime.now());
        notice.setPriority("重要");
        notice.setIsRead(false);
        noticeService.save(notice);
    }

    /**
     * 奖励审批通知
     */
    public void notifyAwardApproval(String stuId, String awardName, String status, String reviewerRole, String reviewerId) {
        String title = "奖励申请" + (status.equals("已通过") ? "通过" : "未通过");
        String content = String.format("您申请的奖励「%s」已被%s%s。", 
            awardName, reviewerRole, status.equals("已通过") ? "审批通过" : "审批拒绝");
        notifyStudent(stuId, title, content, "奖励审批", reviewerRole, reviewerId);
    }

    /**
     * 奖励最终审批通知
     */
    public void notifyAwardFinalApproval(String stuId, String awardName, String finalStatus) {
        String title = "奖励申请" + (finalStatus.equals("已通过") ? "已通过" : "未通过");
        String content = String.format("您申请的奖励「%s」最终审批结果：%s。", awardName, finalStatus);
        notifyStudent(stuId, title, content, "奖励审批", "系统", null);
    }

    /**
     * 处分通知
     */
    public void notifyPunishment(String stuId, String punishmentType, String status, String reviewerRole, String reviewerId) {
        String title;
        String content;
        if (status.equals("已生效")) {
            title = "处分通知";
            content = String.format("您有一项「%s」处分已生效，请及时查看详情。", punishmentType);
        } else if (status.equals("已撤销")) {
            title = "处分撤销通知";
            content = String.format("您的「%s」处分已被撤销。", punishmentType);
        } else {
            title = "处分状态变更";
            content = String.format("您的「%s」处分状态已变更为：%s。", punishmentType, status);
        }
        notifyStudent(stuId, title, content, "处分通知", reviewerRole, reviewerId);
    }

    /**
     * 申诉审理通知
     */
    public void notifyAppealReview(String stuId, String status, String reviewerRole, String reviewerId) {
        String title = "申诉" + (status.equals("已通过") ? "通过" : "未通过");
        String content;
        if (status.equals("已通过")) {
            content = String.format("您的处分申诉已被%s审理通过，相关处分将被撤销。", reviewerRole);
        } else {
            content = String.format("您的处分申诉已被%s驳回。", reviewerRole);
        }
        notifyStudent(stuId, title, content, "申诉审理", reviewerRole, reviewerId);
    }

    /**
     * 申诉最终审理通知
     */
    public void notifyAppealFinalReview(String stuId, String finalStatus) {
        String title = "申诉最终结果";
        String content;
        if (finalStatus.equals("已通过")) {
            content = "您的处分申诉已通过最终审理，相关处分已被撤销。";
        } else {
            content = "您的处分申诉最终审理结果：未通过。";
        }
        notifyStudent(stuId, title, content, "申诉审理", "系统", null);
    }

    /**
     * 学籍变动审核通知
     */
    public void notifyStatusChangeReview(String stuId, String changeType, String status, String reviewerRole, String reviewerId) {
        String title = "学籍变动申请" + (status.equals("已通过") ? "通过" : "未通过");
        String content = String.format("您的「%s」申请已被%s%s。", 
            changeType, reviewerRole, status.equals("已通过") ? "审核通过" : "审核拒绝");
        notifyStudent(stuId, title, content, "学籍变动", reviewerRole, reviewerId);
    }

    /**
     * 学籍变动最终审核通知
     */
    public void notifyStatusChangeFinalReview(String stuId, String changeType, String finalStatus) {
        String title = "学籍变动申请" + (finalStatus.equals("已通过") ? "已通过" : "未通过");
        String content = String.format("您的「%s」申请最终审核结果：%s。", changeType, finalStatus);
        notifyStudent(stuId, title, content, "学籍变动", "系统", null);
    }

    /**
     * 离校审核通知
     */
    public void notifyLeaveSchoolReview(String stuId, String reviewStep, String status, String reviewerRole, String reviewerId) {
        String title = reviewStep + (status.equals("已通过") ? "通过" : "未通过");
        String content = String.format("您的离校申请%s已%s。", reviewStep, status.equals("已通过") ? "审核通过" : "被拒绝");
        notifyStudent(stuId, title, content, "离校审核", reviewerRole, reviewerId);
    }

    /**
     * 离校最终审核通知
     */
    public void notifyLeaveSchoolFinalReview(String stuId, String finalStatus) {
        String title = "离校申请" + (finalStatus.equals("已通过") ? "已通过" : "未通过");
        String content;
        if (finalStatus.equals("已通过")) {
            content = "您的离校申请已全部审核通过，请按时办理离校手续。";
        } else {
            content = "您的离校申请审核未通过，请查看详情了解原因。";
        }
        notifyStudent(stuId, title, content, "离校审核", "系统", null);
    }
}
