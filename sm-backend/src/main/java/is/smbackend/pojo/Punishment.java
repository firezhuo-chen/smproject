package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("punishment")
public class Punishment {
    @TableId
    private String punishmentId;
    private String stuId;
    private String punishmentType;
    private String punishmentReason;
    private String issueOrg;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate punishmentDate;
    private String applicantId;    // 申请人ID
    private String applicantRole;  // 申请人角色
    private String adminStatus;
    private String adminOpinion;
    private String punishmentStatus;
}
