package is.smbackend.controller;

import is.smbackend.annotation.OperationLog;
import is.smbackend.dto.PasswordUpdateRequest;
import is.smbackend.dto.ProfileUpdateRequest;
import is.smbackend.exception.BusinessException;
import is.smbackend.pojo.UserAdmin;
import is.smbackend.pojo.UserAdvisor;
import is.smbackend.pojo.UserStudent;
import is.smbackend.response.Result;
import is.smbackend.service.UserAdminService;
import is.smbackend.service.UserAdvisorService;
import is.smbackend.service.UserStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 个人中心控制器
 */
@Tag(name = "个人中心")
@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserStudentService userStudentService;

    @Autowired
    private UserAdvisorService userAdvisorService;

    @Autowired
    private UserAdminService userAdminService;

    @Operation(summary = "获取用户信息")
    @GetMapping("/{userType}/{userId}")
    public Result<Map<String, Object>> getProfile(
            @PathVariable String userType,
            @PathVariable String userId) {
        
        Map<String, Object> profile = new LinkedHashMap<>();
        
        switch (userType) {
            case "student" -> {
                UserStudent user = userStudentService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                profile.put("userId", user.getUserId());
                profile.put("userName", user.getUserName());
                profile.put("phone", user.getPhone());
                profile.put("email", user.getEmail());
                profile.put("status", user.getStatus());
                profile.put("lastLoginTime", user.getLastLoginTime());
                profile.put("createTime", user.getCreateTime());
                profile.put("userType", "student");
                profile.put("userTypeLabel", "学生");
            }
            case "advisor" -> {
                UserAdvisor user = userAdvisorService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                profile.put("userId", user.getUserId());
                profile.put("userName", user.getUserName());
                profile.put("phone", user.getPhone());
                profile.put("email", user.getEmail());
                profile.put("status", user.getStatus());
                profile.put("lastLoginTime", user.getLastLoginTime());
                profile.put("createTime", user.getCreateTime());
                profile.put("userType", "advisor");
                profile.put("userTypeLabel", "辅导员");
            }
            case "admin" -> {
                UserAdmin user = userAdminService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                profile.put("userId", user.getUserId());
                profile.put("userName", user.getUserName());
                profile.put("role", user.getRole());  // 返回管理员角色
                profile.put("phone", user.getPhone());
                profile.put("email", user.getEmail());
                profile.put("status", user.getStatus());
                profile.put("lastLoginTime", user.getLastLoginTime());
                profile.put("createTime", user.getCreateTime());
                profile.put("userType", "admin");
                profile.put("userTypeLabel", "管理员");
            }
            default -> throw new BusinessException("无效的用户类型");
        }
        
        return Result.success(profile);
    }

    @Operation(summary = "更新联系方式")
    @OperationLog(operation = "更新", description = "更新个人联系方式")
    @PutMapping("/{userType}/{userId}")
    public Result<Boolean> updateProfile(
            @PathVariable String userType,
            @PathVariable String userId,
            @RequestBody ProfileUpdateRequest request) {
        
        switch (userType) {
            case "student" -> {
                UserStudent user = userStudentService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (request.getPhone() != null) {
                    user.setPhone(request.getPhone());
                }
                if (request.getEmail() != null) {
                    user.setEmail(request.getEmail());
                }
                userStudentService.updateById(user);
            }
            case "advisor" -> {
                UserAdvisor user = userAdvisorService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (request.getPhone() != null) {
                    user.setPhone(request.getPhone());
                }
                if (request.getEmail() != null) {
                    user.setEmail(request.getEmail());
                }
                userAdvisorService.updateById(user);
            }
            case "admin" -> {
                UserAdmin user = userAdminService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (request.getPhone() != null) {
                    user.setPhone(request.getPhone());
                }
                if (request.getEmail() != null) {
                    user.setEmail(request.getEmail());
                }
                userAdminService.updateById(user);
            }
            default -> throw new BusinessException("无效的用户类型");
        }
        
        return Result.success(true);
    }

    @Operation(summary = "修改密码")
    @OperationLog(operation = "更新", description = "修改登录密码")
    @PutMapping("/{userType}/{userId}/password")
    public Result<Boolean> updatePassword(
            @PathVariable String userType,
            @PathVariable String userId,
            @RequestBody PasswordUpdateRequest request) {
        
        if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            throw new BusinessException("请输入旧密码");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new BusinessException("请输入新密码");
        }
        if (request.getNewPassword().length() < 3) {
            throw new BusinessException("新密码长度不能少于3位");
        }
        
        switch (userType) {
            case "student" -> {
                UserStudent user = userStudentService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!request.getOldPassword().equals(user.getPasswd())) {
                    throw new BusinessException("旧密码错误");
                }
                user.setPasswd(request.getNewPassword());
                userStudentService.updateById(user);
            }
            case "advisor" -> {
                UserAdvisor user = userAdvisorService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!request.getOldPassword().equals(user.getPasswd())) {
                    throw new BusinessException("旧密码错误");
                }
                user.setPasswd(request.getNewPassword());
                userAdvisorService.updateById(user);
            }
            case "admin" -> {
                UserAdmin user = userAdminService.getById(userId);
                if (user == null) {
                    throw new BusinessException("用户不存在");
                }
                if (!request.getOldPassword().equals(user.getPasswd())) {
                    throw new BusinessException("旧密码错误");
                }
                user.setPasswd(request.getNewPassword());
                userAdminService.updateById(user);
            }
            default -> throw new BusinessException("无效的用户类型");
        }
        
        return Result.success(true);
    }
}
