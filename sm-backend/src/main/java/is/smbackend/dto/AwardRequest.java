package is.smbackend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 奖励申请请求
 */
@Data
public class AwardRequest {
    
    private String awardId;
    
    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^U\\d{9}$", message = "学号格式错误，应为U开头加9位数字")
    private String stuId;
    
    @NotBlank(message = "奖励类型不能为空")
    private String awardType;
    
    @NotBlank(message = "奖励等级不能为空")
    private String awardLevel;
    
    @NotBlank(message = "奖励名称不能为空")
    @Size(max = 50, message = "奖励名称不能超过50个字符")
    private String awardName;
    
    @DecimalMin(value = "0.00", message = "奖金金额不能为负数")
    private BigDecimal awardAmount;
    
    @NotBlank(message = "颁发单位不能为空")
    @Size(max = 20, message = "颁发单位不能超过20个字符")
    private String issueOrg;
    
    @NotNull(message = "申请日期不能为空")
    private LocalDate applyDate;
    
    @NotNull(message = "获奖日期不能为空")
    private LocalDate awardDate;
    
    private String advisorStatus;
    private String adminStatus;
    private String awardStatus;
}
