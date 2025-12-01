package is.smbackend.dto;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 密码
     */
    private String passwd;
    
    /**
     * 用户类型：student, advisor, admin
     */
    private String userType;
}
