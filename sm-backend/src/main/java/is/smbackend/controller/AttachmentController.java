package is.smbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import is.smbackend.pojo.Attachment;
import is.smbackend.service.AttachmentService;
import is.smbackend.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Tag(name = "附件管理")
@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Operation(summary = "上传附件")
    @PostMapping("/upload")
    public Result<Attachment> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("relatedId") String relatedId,
            @RequestParam("relatedType") String relatedType,
            @RequestParam("uploadUserId") String uploadUserId) {
        
        if (file.isEmpty()) {
            return Result.failed("请选择要上传的文件");
        }

        // 检查文件大小（限制10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.failed("文件大小不能超过10MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") 
                && !contentType.equals("application/pdf")
                && !contentType.startsWith("application/msword")
                && !contentType.startsWith("application/vnd.openxmlformats"))) {
            return Result.failed("只支持图片、PDF和Word文档");
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir, relatedType);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 生成附件ID
            String attachmentId = "ATT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                    + String.format("%03d", (int)(Math.random() * 1000));

            // 保存附件记录
            Attachment attachment = new Attachment();
            attachment.setAttachmentId(attachmentId);
            attachment.setFileName(originalFilename);
            attachment.setFilePath(relatedType + "/" + newFilename);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(contentType);
            attachment.setRelatedId(relatedId);
            attachment.setRelatedType(relatedType);
            attachment.setUploadUserId(uploadUserId);
            attachment.setUploadTime(LocalDateTime.now());

            attachmentService.save(attachment);

            return Result.success(attachment);
        } catch (IOException e) {
            return Result.failed("文件上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据业务ID获取附件列表")
    @GetMapping("/list")
    public Result<List<Attachment>> getByRelated(
            @RequestParam("relatedId") String relatedId,
            @RequestParam("relatedType") String relatedType) {
        LambdaQueryWrapper<Attachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attachment::getRelatedId, relatedId)
               .eq(Attachment::getRelatedType, relatedType)
               .orderByDesc(Attachment::getUploadTime);
        return Result.success(attachmentService.list(wrapper));
    }

    @Operation(summary = "下载附件")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) {
        Attachment attachment = attachmentService.getById(id);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String encodedFilename = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                        .replaceAll("\\+", "%20");
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(attachment.getFileType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename*=UTF-8''" + encodedFilename)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "删除附件")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id) {
        Attachment attachment = attachmentService.getById(id);
        if (attachment == null) {
            return Result.failed("附件不存在");
        }

        try {
            // 删除物理文件
            Path filePath = Paths.get(uploadDir).resolve(attachment.getFilePath());
            Files.deleteIfExists(filePath);

            // 删除数据库记录
            attachmentService.removeById(id);
            return Result.success(true);
        } catch (IOException e) {
            return Result.failed("删除失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据ID获取附件信息")
    @GetMapping("/{id}")
    public Result<Attachment> getById(@PathVariable String id) {
        return Result.success(attachmentService.getById(id));
    }
}
