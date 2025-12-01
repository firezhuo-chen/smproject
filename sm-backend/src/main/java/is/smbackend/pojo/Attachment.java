package is.smbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("attachment")
public class Attachment {
    @TableId
    private String attachmentId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String relatedId;
    private String relatedType;  // award, punishment, statusChange, leaveSchool
    private String uploadUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;
}
