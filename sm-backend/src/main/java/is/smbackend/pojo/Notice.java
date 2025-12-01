package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notice")
public class Notice {
    @TableId
    private String noticeId;
    private String title;
    private String content;
    private String noticeType;
    private String targetUser;
    private String targetType;  // student 或 advisor
    private String publishUser;
    private String publishUserId;
    private LocalDateTime publishTime;
    private String priority;
    private Boolean isRead;
    private LocalDateTime readTime;
}
