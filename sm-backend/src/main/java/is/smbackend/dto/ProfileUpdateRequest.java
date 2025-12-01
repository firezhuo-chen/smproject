package is.smbackend.dto;

import lombok.Data;

/**
 * 个人信息更新请求
 */
@Data
public class ProfileUpdateRequest {
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
}
