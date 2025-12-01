package is.smbackend.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 用户类型
     */
    private String userType;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 管理员角色（仅管理员有值）
     */
    private String role;
    
    /**
     * JWT Token
     */
    private String token;
}
