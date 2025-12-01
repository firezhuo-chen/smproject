package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("student_status_info")
public class StudentStatusInfo {
    @TableId
    private String stuId;
    private String department;
    private String major;
    private String className;
    private String academicStatus;
    private LocalDate admissionDate;
    private LocalDate graduationDate;
    private String warningLevel;
    private String registerStatus;
    private String advisorId;
    private String advisor;
}
