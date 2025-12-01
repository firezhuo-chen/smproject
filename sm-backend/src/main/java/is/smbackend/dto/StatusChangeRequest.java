package is.smbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学籍变动申请请求
 */
@Data
public class StatusChangeRequest {
    
    private String changeId;
    
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^U\\d{9}$", message = "学号格式错误，应为U开头加9位数字")
    private String stuId;
    
    @NotBlank(message = "变动类型不能为空")
    private String changeType;
    
    @NotBlank(message = "变动原因不能为空")
    private String changeReason;
    
    @NotBlank(message = "当前学校不能为空")
    @Size(max = 20, message = "学校名称不能超过20个字符")
    private String currentSchool;
    
    @NotBlank(message = "当前学院不能为空")
    @Size(max = 20, message = "学院名称不能超过20个字符")
    private String currentCollege;
    
    @NotBlank(message = "当前专业不能为空")
    @Size(max = 20, message = "专业名称不能超过20个字符")
    private String currentMajor;
    
    @Size(max = 20, message = "目标学校名称不能超过20个字符")
    private String targetSchool;
    
    @Size(max = 20, message = "目标学院名称不能超过20个字符")
    private String targetCollege;
    
    @Size(max = 20, message = "目标专业名称不能超过20个字符")
    private String targetMajor;
    
    @NotNull(message = "申请日期不能为空")
    private LocalDate applyDate;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String advisorId;
    private String advisorStatus;
    private String advisorOpinion;
    private String adminId;
    private String adminStatus;
    private String adminOpinion;
    private String applyStatus;
}
