package is.smbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 处分申请请求
 */
@Data
public class PunishmentRequest {
    
    private String punishmentId;
    
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^U\\d{9}$", message = "学号格式错误，应为U开头加9位数字")
    private String stuId;
    
    @NotBlank(message = "处分类型不能为空")
    private String punishmentType;
    
    @NotBlank(message = "处分原因不能为空")
    private String punishmentReason;
    
    @NotBlank(message = "决定单位不能为空")
    @Size(max = 20, message = "决定单位不能超过20个字符")
    private String issueOrg;
    
    @NotNull(message = "申请日期不能为空")
    private LocalDate applyDate;
    
    @NotNull(message = "生效日期不能为空")
    private LocalDate punishmentDate;
    
    private String applicantId;
    private String applicantRole;
    private String adminStatus;
    private String punishmentStatus;
}
