package is.smbackend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import is.smbackend.annotation.OperationLog;
import is.smbackend.pojo.Log;
import is.smbackend.service.LogService;
import is.smbackend.util.JwtUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 操作日志切面 - 记录详细的操作信息和变更内容
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private LogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 切入点：所有带有 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(is.smbackend.annotation.OperationLog)")
    public void operationLogPointcut() {}

    /**
     * 环绕通知
     */
    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLog operationLog = signature.getMethod().getAnnotation(OperationLog.class);
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        String userId = "system";
        String userName = "系统";
        String userType = "system";
        String ipAddress = "";
        String userAgent = "";
        String requestUrl = "";
        String requestMethod = "";
        
        if (attributes != null) {
            request = attributes.getRequest();
            ipAddress = getClientIp(request);
            userAgent = request.getHeader("User-Agent");
            requestUrl = request.getRequestURI();
            requestMethod = request.getMethod();
            
            // 从 Authorization 请求头获取 JWT Token 并解析用户信息
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtil.validateToken(token)) {
                        userId = jwtUtil.getUserId(token);
                        userName = jwtUtil.getUserName(token);
                        userType = jwtUtil.getUserType(token);
                    }
                } catch (Exception e) {
                    // Token 解析失败，使用默认值
                }
            }
        }
        
        // 获取方法参数和目标ID
        String params = "";
        String targetId = "";
        String newValue = "";
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                Object firstArg = args[0];
                params = objectMapper.writeValueAsString(firstArg);
                
                // 尝试获取目标ID
                targetId = extractTargetId(firstArg);
                
                // 如果需要记录变更，保存新值
                if (operationLog.recordChange()) {
                    newValue = params;
                    if (newValue.length() > 2000) {
                        newValue = newValue.substring(0, 2000) + "...";
                    }
                }
                
                // 限制参数长度
                if (params.length() > 500) {
                    params = params.substring(0, 500) + "...";
                }
            }
        } catch (Exception e) {
            params = "参数解析失败";
        }
        
        // 执行方法
        boolean success = true;
        Object result = null;
        String errorMessage = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            success = false;
            errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 500) {
                errorMessage = errorMessage.substring(0, 500);
            }
            throw e;
        } finally {
            // 记录日志
            try {
                Log log = new Log();
                log.setLogId(generateLogId());
                log.setUserId(userId);
                log.setUserName(userName);
                log.setUserType(userType);
                log.setOperation(operationLog.operation());
                
                String detail = operationLog.description();
                if (detail.isEmpty()) {
                    detail = signature.getDeclaringTypeName() + "." + signature.getName();
                }
                detail = detail + " | 参数: " + params;
                log.setOperationDetail(detail);
                
                // 设置目标类型和ID
                if (!operationLog.targetType().isEmpty()) {
                    log.setTargetType(operationLog.targetType());
                }
                if (!targetId.isEmpty()) {
                    log.setTargetId(targetId);
                }
                
                // 设置变更值
                if (operationLog.recordChange() && !newValue.isEmpty()) {
                    log.setNewValue(newValue);
                }
                
                // 设置请求信息
                log.setIpAddress(ipAddress);
                log.setUserAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
                log.setRequestUrl(requestUrl);
                log.setRequestMethod(requestMethod);
                
                log.setOperationTime(LocalDateTime.now());
                log.setResult(success);
                log.setErrorMessage(errorMessage);
                
                logService.save(log);
            } catch (Exception e) {
                // 日志记录失败不影响业务
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(ip)) {
                ip = "127.0.0.1";
            }
        }
        // 多个代理时，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 尝试从对象中提取ID
     */
    private String extractTargetId(Object obj) {
        if (obj == null) return "";
        
        // 如果是字符串，直接返回
        if (obj instanceof String) {
            return (String) obj;
        }
        
        // 尝试通过反射获取常见的ID字段
        try {
            String[] idFields = {"id", "awardId", "punishmentId", "appealId", "changeId", "leaveId", "noticeId", "stuId", "userId"};
            for (String fieldName : idFields) {
                try {
                    Method getter = obj.getClass().getMethod("get" + capitalize(fieldName));
                    Object value = getter.invoke(obj);
                    if (value != null) {
                        return value.toString();
                    }
                } catch (NoSuchMethodException ignored) {
                    // 继续尝试下一个字段
                }
            }
        } catch (Exception e) {
            // 忽略反射异常
        }
        return "";
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * 生成日志ID
     */
    private String generateLogId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return "LOG" + timestamp + uuid;
    }
}
