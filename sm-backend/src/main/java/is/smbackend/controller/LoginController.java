package is.smbackend.controller;

import is.smbackend.dto.LoginRequest;
import is.smbackend.dto.LoginResponse;
import is.smbackend.exception.BusinessException;
import is.smbackend.pojo.Log;
import is.smbackend.pojo.UserAdmin;
import is.smbackend.pojo.UserAdvisor;
import is.smbackend.pojo.UserStudent;
import is.smbackend.response.Result;
import is.smbackend.service.LogService;
import is.smbackend.service.UserAdminService;
import is.smbackend.service.UserAdvisorService;
import is.smbackend.service.UserStudentService;
import is.smbackend.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 登录控制器
 */
@Tag(name = "登录接口")
@RestController
public class LoginController {

    @Autowired
    private UserStudentService userStudentService;

    @Autowired
    private UserAdvisorService userAdvisorService;

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private LogService logService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new BusinessException("用户ID不能为空");
        }
        if (request.getPasswd() == null || request.getPasswd().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        if (request.getUserType() == null || request.getUserType().isEmpty()) {
            throw new BusinessException("用户类型不能为空");
        }

        LoginResponse response = new LoginResponse();

        switch (request.getUserType()) {
            case "student" -> {
                UserStudent user = userStudentService.getById(request.getUserId());
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!request.getPasswd().equals(user.getPasswd())) {
                    throw new BusinessException("密码错误");
                }
                // 更新最后登录时间
                user.setLastLoginTime(LocalDateTime.now());
                userStudentService.updateById(user);
                
                response.setUserId(user.getUserId());
                response.setUserName(user.getUserName());
                response.setUserType("student");
                response.setPhone(user.getPhone());
                response.setEmail(user.getEmail());
            }
            case "advisor" -> {
                UserAdvisor user = userAdvisorService.getById(request.getUserId());
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!request.getPasswd().equals(user.getPasswd())) {
                    throw new BusinessException("密码错误");
                }
                // 更新最后登录时间
                user.setLastLoginTime(LocalDateTime.now());
                userAdvisorService.updateById(user);
                
                response.setUserId(user.getUserId());
                response.setUserName(user.getUserName());
                response.setUserType("advisor");
                response.setPhone(user.getPhone());
                response.setEmail(user.getEmail());
            }
            case "admin" -> {
                UserAdmin user = userAdminService.getById(request.getUserId());
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!request.getPasswd().equals(user.getPasswd())) {
                    throw new BusinessException("密码错误");
                }
                // 更新最后登录时间
                user.setLastLoginTime(LocalDateTime.now());
                userAdminService.updateById(user);
                
                response.setUserId(user.getUserId());
                response.setUserName(user.getUserName());
                response.setUserType("admin");
                response.setPhone(user.getPhone());
                response.setEmail(user.getEmail());
                response.setRole(user.getRole());  // 返回管理员角色
            }
            default -> throw new BusinessException("无效的用户类型");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(response.getUserId(), response.getUserName(), response.getUserType());
        response.setToken(token);

        // 记录登录日志
        saveLoginLog(response.getUserId(), response.getUserName(), response.getUserType(), true, null, httpRequest);

        return Result.success(response);
    }

    /**
     * 记录登录日志（包含IP、设备等详细信息）
     */
    private void saveLoginLog(String userId, String userName, String userType, boolean success, String errorMessage, HttpServletRequest request) {
        try {
            Log log = new Log();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            log.setLogId("LOG" + timestamp + uuid);
            log.setUserId(userId);
            log.setUserName(userName);
            log.setUserType(userType);
            log.setOperation("登录");
            
            // 获取IP地址
            String ipAddress = getClientIp(request);
            log.setIpAddress(ipAddress);
            
            // 获取User-Agent（浏览器/设备信息）
            String userAgent = request.getHeader("User-Agent");
            log.setUserAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
            
            // 解析设备信息
            String deviceInfo = parseDeviceInfo(userAgent);
            
            // 设置详细信息
            String detail = String.format("用户登录系统 | IP: %s | 设备: %s", ipAddress, deviceInfo);
            log.setOperationDetail(detail);
            
            log.setRequestUrl("/login");
            log.setRequestMethod("POST");
            log.setOperationTime(LocalDateTime.now());
            log.setResult(success);
            log.setErrorMessage(errorMessage);
            
            logService.save(log);
        } catch (Exception e) {
            // 日志记录失败不影响登录
            e.printStackTrace();
        }
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
     * 解析User-Agent获取设备信息
     */
    private String parseDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知设备";
        }
        
        String os = "未知系统";
        String browser = "未知浏览器";
        
        // 解析操作系统
        if (userAgent.contains("Windows NT 10")) {
            os = "Windows 10";
        } else if (userAgent.contains("Windows NT 6.3")) {
            os = "Windows 8.1";
        } else if (userAgent.contains("Windows NT 6.1")) {
            os = "Windows 7";
        } else if (userAgent.contains("Mac OS X")) {
            os = "macOS";
        } else if (userAgent.contains("Linux")) {
            if (userAgent.contains("Android")) {
                os = "Android";
            } else {
                os = "Linux";
            }
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
        }
        
        // 解析浏览器
        if (userAgent.contains("Edg/")) {
            browser = "Edge";
        } else if (userAgent.contains("Chrome/") && !userAgent.contains("Edg/")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox/")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) {
            browser = "Safari";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) {
            browser = "IE";
        }
        
        return os + " / " + browser;
    }
}
