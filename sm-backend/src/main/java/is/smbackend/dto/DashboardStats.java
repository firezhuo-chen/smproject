package is.smbackend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计数据
 */
@Data
public class DashboardStats {
    
    // ========== 通用统计 ==========
    /**
     * 统计数字（key: 名称, value: 数量）
     */
    private Map<String, Long> counts;
    
    /**
     * 待办事项列表
     */
    private List<TodoItem> todoList;
    
    /**
     * 最新通知
     */
    private List<Map<String, Object>> notices;
    
    /**
     * 最近日志
     */
    private List<Map<String, Object>> logs;
    
    /**
     * 预警学生列表（辅导员/管理员用）
     */
    private List<Map<String, Object>> warningStudents;
    
    /**
     * 用户信息
     */
    private Map<String, Object> userInfo;
    
    /**
     * 待办事项
     */
    @Data
    public static class TodoItem {
        private String id;
        private String type;
        private String title;
        private String description;
        private String time;
        private String status;
        private String link;
    }
}
