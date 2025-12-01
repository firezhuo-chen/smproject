package is.smbackend.dto;

import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class PasswordUpdateRequest {
    /**
     * 旧密码
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
}
