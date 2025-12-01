package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_advisor")
public class UserAdvisor {
    @TableId
    private String userId;
    private String userName;
    private String passwd;
    private String phone;
    private String email;
    private String status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
