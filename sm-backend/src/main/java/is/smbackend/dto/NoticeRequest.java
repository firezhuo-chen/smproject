package is.smbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知发布请求
 */
@Data
public class NoticeRequest {
    
    private String noticeId;
    
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 20, message = "通知标题不能超过20个字符")
    private String title;
    
    @NotBlank(message = "通知内容不能为空")
    private String content;
    
    @NotBlank(message = "通知类型不能为空")
    @Size(max = 20, message = "通知类型不能超过20个字符")
    private String noticeType;
    
    @NotBlank(message = "目标用户不能为空")
    private String targetUser;
    
    @NotBlank(message = "发布人不能为空")
    @Size(max = 20, message = "发布人不能超过20个字符")
    private String publishUser;
    
    private LocalDateTime publishTime;
    
    private String priority;
}
