package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("student_basic_info")
public class StudentBasicInfo {
    @TableId
    private String stuId;
    private String name;
    private String gender;
    private String idCard;
    private LocalDate birthDate;
    private String nationality;
    private String nation;
    private String nativePlace;
    private String politicalStatus;
    private String phone;
}
