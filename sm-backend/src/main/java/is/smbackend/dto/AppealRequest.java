package is.smbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 申诉请求
 */
@Data
public class AppealRequest {
    
    private String appealId;
    
    @NotBlank(message = "处分编号不能为空")
    private String punishmentId;
    
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^U\\d{9}$", message = "学号格式错误，应为U开头加9位数字")
    private String stuId;
    
    @NotBlank(message = "申诉理由不能为空")
    private String appealReason;
    
    @NotNull(message = "申诉日期不能为空")
    private LocalDate appealDate;
    
    private String advisorStatus;
    private String adminStatus;
    private String appealStatus;
}
