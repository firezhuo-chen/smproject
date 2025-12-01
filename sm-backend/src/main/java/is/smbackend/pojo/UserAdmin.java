package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_admin")
public class UserAdmin {
    @TableId
    private String userId;
    private String userName;
    private String passwd;
    private String role;  // 管理员角色：宿管管理员、图书馆管理员、财务处管理员、教务管理员
    private String phone;
    private String email;
    private String status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
