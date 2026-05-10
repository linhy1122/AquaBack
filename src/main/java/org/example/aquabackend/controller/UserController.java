package org.example.aquabackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.dto.CreateUserRequest;
import org.example.aquabackend.entity.User;
import org.example.aquabackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/users")
@Api(value = "用户管理", tags = "用户管理接口")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 添加用户（公开接口，无需登录）
     */
    @PostMapping
    @ApiOperation(value = "添加用户", notes = "公开接口，无需认证。创建新用户，可指定用户名、密码、邮箱、角色和启用状态")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        String username = request.getUsername();
        logger.info("Admin creating user: {}", username);

        if (userService.findByUsername(username) != null) {
            logger.warn("Username already exists: {}", username);
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户名已存在"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        user.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        User createdUser = userService.createUser(user);

        logger.info("Admin created user: {} with role: {}", username, createdUser.getRole());

        return ResponseEntity.ok(
                ApiResponse.ok("用户创建成功")
                        .put("userId", createdUser.getId())
                        .put("username", createdUser.getUsername())
                        .put("email", createdUser.getEmail())
                        .put("role", createdUser.getRole())
                        .put("enabled", createdUser.getEnabled())
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "用户列表", notes = "获取所有用户的列表（已脱敏，不包含密码等敏感信息）")
    public ResponseEntity<ApiResponse> listUsers() {
        logger.info("Admin fetching user list");

        List<User> users = userService.findAll();
        List<UserInfo> userInfos = users.stream()
                .map(UserInfo::fromUser)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.ok("查询成功")
                        .put("users", userInfos)
                        .put("total", userInfos.size())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "用户详情", notes = "根据用户ID获取用户详细信息")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path", dataTypeClass = Long.class, example = "1")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        logger.info("Admin fetching user by id: {}", id);

        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户不存在"));
        }

        return ResponseEntity.ok(
                ApiResponse.ok("查询成功").put("user", UserInfo.fromUser(user))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "更新用户", notes = "管理员更新指定用户的用户名、邮箱、角色、启用状态或密码")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path", dataTypeClass = Long.class, example = "1")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody CreateUserRequest request) {
        logger.info("Admin updating user: {}", id);

        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户不存在"));
        }

        if (!existingUser.getUsername().equals(request.getUsername())) {
            if (userService.findByUsername(request.getUsername()) != null) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("用户名已存在"));
            }
        }

        existingUser.setUsername(request.getUsername());
        existingUser.setEmail(request.getEmail());
        existingUser.setRole(request.getRole() != null ? request.getRole() : existingUser.getRole());
        existingUser.setEnabled(request.getEnabled() != null ? request.getEnabled() : existingUser.getEnabled());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            userService.updatePassword(id, request.getPassword());
        }

        userService.updateUser(existingUser);

        logger.info("Admin updated user: {}", id);

        return ResponseEntity.ok(
                ApiResponse.ok("用户更新成功").put("user", UserInfo.fromUser(existingUser))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "删除用户", notes = "逻辑删除指定用户（将 deleted 标记置为 1）")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, paramType = "path", dataTypeClass = Long.class, example = "1")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        logger.info("Admin deleting user: {}", id);

        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户不存在或已被删除"));
        }

        return ResponseEntity.ok(ApiResponse.ok("用户删除成功"));
    }

    /**
     * 脱敏用户信息（不含密码）
     */
    @ApiModel(value = "用户信息（脱敏）", description = "用户信息，不包含密码等敏感字段")
    public static class UserInfo {

        @ApiModelProperty(value = "用户ID", example = "1")
        private Long id;

        @ApiModelProperty(value = "用户名", example = "admin")
        private String username;

        @ApiModelProperty(value = "邮箱", example = "admin@aqua.com")
        private String email;

        @ApiModelProperty(value = "用户角色", example = "ADMIN")
        private String role;

        @ApiModelProperty(value = "是否启用", example = "true")
        private Boolean enabled;

        @ApiModelProperty(value = "是否锁定", example = "false")
        private Boolean accountLocked;

        @ApiModelProperty(value = "创建时间")
        private LocalDateTime createTime;

        @ApiModelProperty(value = "更新时间")
        private LocalDateTime updateTime;

        public UserInfo() {}

        public static UserInfo fromUser(User user) {
            UserInfo info = new UserInfo();
            info.setId(user.getId());
            info.setUsername(user.getUsername());
            info.setEmail(user.getEmail());
            info.setRole(user.getRole());
            info.setEnabled(user.getEnabled());
            info.setAccountLocked(user.getAccountLocked());
            info.setCreateTime(user.getCreateTime());
            info.setUpdateTime(user.getUpdateTime());
            return info;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Boolean getAccountLocked() { return accountLocked; }
        public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

        public LocalDateTime getUpdateTime() { return updateTime; }
        public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    }
}
