package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("leave_school")
public class LeaveSchool {
    @TableId
    private String leaveId;
    private String stuId;
    private String leaveType;
    private String leaveReason;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveDate;
    private String dormitoryReviewerId;
    private String dormitoryStatus;
    private String dormitoryOpinion;
    private String libraryReviewerId;
    private String libraryStatus;
    private String libraryOpinion;
    private String financeReviewerId;
    private String financeStatus;
    private String financeOpinion;
    private String adminId;
    private String adminStatus;
    private String adminOpinion;
    private String overallStatus;
}
