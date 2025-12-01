package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("status_change")
public class StatusChange {
    @TableId
    private String changeId;
    private String stuId;
    private String changeType;
    private String changeReason;
    private String currentSchool;
    private String currentCollege;
    private String currentMajor;
    private String targetSchool;
    private String targetCollege;
    private String targetMajor;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String advisorId;
    private String advisorStatus;
    private String advisorOpinion;
    private String adminId;
    private String adminStatus;
    private String adminOpinion;
    private String applyStatus;
}
