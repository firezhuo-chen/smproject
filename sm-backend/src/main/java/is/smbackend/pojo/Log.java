package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("log")
public class Log {
    @TableId
    private String logId;
    private String userId;
    private String userName;
    private String userType;        // 用户类型：student/advisor/admin
    private String operation;       // 操作类型
    private String operationDetail; // 操作详情
    private String targetType;      // 操作对象类型：award/punishment/appeal等
    private String targetId;        // 操作对象ID
    private String oldValue;        // 变更前的值（JSON格式）
    private String newValue;        // 变更后的值（JSON格式）
    private String ipAddress;       // IP地址
    private String userAgent;       // 浏览器/设备信息
    private String requestUrl;      // 请求URL
    private String requestMethod;   // 请求方法：GET/POST/PUT/DELETE
    private LocalDateTime operationTime;
    private Boolean result;
    private String errorMessage;    // 错误信息（如果失败）
}
