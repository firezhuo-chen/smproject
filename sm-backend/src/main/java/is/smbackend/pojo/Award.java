package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("award")
public class Award {
    @TableId
    private String awardId;
    private String stuId;
    private String awardType;
    private String awardLevel;
    private String awardName;
    private BigDecimal awardAmount;
    private String issueOrg;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate awardDate;
    private String advisorStatus;
    private String advisorOpinion;
    private String adminStatus;
    private String adminOpinion;
    private String awardStatus;
}
