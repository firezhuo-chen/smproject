package is.smbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 离校手续申请请求
 */
@Data
public class LeaveSchoolRequest {
    
    private String leaveId;
    
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^U\\d{9}$", message = "学号格式错误，应为U开头加9位数字")
    private String stuId;
    
    @NotBlank(message = "离校类型不能为空")
    private String leaveType;
    
    @NotBlank(message = "离校原因不能为空")
    private String leaveReason;
    
    @NotNull(message = "申请日期不能为空")
    private LocalDate applyDate;
    
    @NotNull(message = "离校日期不能为空")
    private LocalDate leaveDate;
    
    private String dormitoryReviewerId;
    private String dormitoryStatus;
    private String libraryReviewerId;
    private String libraryStatus;
    private String financeReviewerId;
    private String financeStatus;
    private String adminId;
    private String adminStatus;
    private String overallStatus;
}
