package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("appeal")
public class Appeal {
    @TableId
    private String appealId;
    private String punishmentId;
    private String stuId;
    private String appealReason;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appealDate;
    private String advisorStatus;
    private String advisorOpinion;
    private String adminStatus;
    private String adminOpinion;
    private String appealStatus;
}
