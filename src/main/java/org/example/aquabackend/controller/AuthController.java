package org.example.aquabackend.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.example.aquabackend.dto.ApiResponse;
import org.example.aquabackend.dto.LoginRequest;
import org.example.aquabackend.dto.RegisterRequest;
import org.example.aquabackend.entity.User;
import org.example.aquabackend.service.UserService;
import org.example.aquabackend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
@Api(value = "认证管理", tags = "认证接口（公开，无需 Token）")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private DefaultKaptcha captchaProducer;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/captcha")
    @ApiOperation(value = "获取验证码", notes = "返回 Base64 格式的图片验证码，验证码存储在 Session 中，5分钟有效")
    public ResponseEntity<ApiResponse> getCaptcha(HttpSession session) throws IOException {
        String captchaText = captchaProducer.createText();
        session.setAttribute("captcha", captchaText);
        session.setMaxInactiveInterval(300);

        BufferedImage image = captchaProducer.createImage(captchaText);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        logger.info("Generated captcha for session: {}, captcha text: [{}] (for test)", session.getId(), captchaText);

        ApiResponse response = ApiResponse.ok("验证码生成成功")
                .put("captchaImage", "data:image/jpeg;base64," + base64Image)
                .put("captchaKey", session.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "使用用户名、密码和验证码登录，成功后返回 JWT Token")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = false, paramType = "header", dataTypeClass = String.class)
    })
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpSession session) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String captcha = loginRequest.getCaptcha();

        logger.info("Login attempt for user: {}", username);

        if (captcha != null && !captcha.isEmpty()) {
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
                logger.warn("Invalid captcha for user: {}", username);
                return ResponseEntity.badRequest().body(ApiResponse.fail("验证码错误"));
            }
        }

        User user = userService.findByUsername(username);
        if (user == null || !userService.checkPassword(password, user.getPassword())) {
            logger.warn("Invalid username or password for user: {}", username);
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户名或密码错误"));
        }

        if (Boolean.FALSE.equals(user.getEnabled())) {
            return ResponseEntity.status(403).body(ApiResponse.fail("账户已被禁用"));
        }
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            return ResponseEntity.status(403).body(ApiResponse.fail("账户已被锁定"));
        }
        if (Boolean.TRUE.equals(user.getAccountExpired())) {
            return ResponseEntity.status(403).body(ApiResponse.fail("账户已过期"));
        }

        String token = jwtUtil.generateToken(username);
        session.removeAttribute("captcha");

        logger.info("User {} logged in successfully, role: {}", username, user.getRole());

        ApiResponse response = ApiResponse.ok("登录成功")
                .put("token", token)
                .put("username", username)
                .put("role", user.getRole())
                .put("userId", user.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "注册新用户账号，注册成功后自动登录并返回 JWT Token")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                 HttpSession session) {
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String email = registerRequest.getEmail();
        String captcha = registerRequest.getCaptcha();

        logger.info("Registration attempt for user: {}", username);

        if (captcha != null && !captcha.isEmpty()) {
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("验证码错误"));
            }
        }

        if (userService.findByUsername(username) != null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户名已存在"));
        }

        User newUser = userService.register(username, password, email);
        String token = jwtUtil.generateToken(username);

        logger.info("User registered successfully: {}", username);

        ApiResponse response = ApiResponse.ok("注册成功")
                .put("token", token)
                .put("username", username)
                .put("userId", newUser.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @ApiOperation(value = "获取当前用户信息", notes = "获取当前已登录用户的详细信息（需携带 JWT Token）")
    public ResponseEntity<ApiResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.fail("未登录"));
        }

        User user = (User) authentication.getPrincipal();

        ApiResponse response = ApiResponse.ok("获取成功")
                .put("username", user.getUsername())
                .put("email", user.getEmail())
                .put("role", user.getRole())
                .put("userId", user.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "退出登录", notes = "销毁当前 Session（客户端需同时丢弃 Token）")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("退出登录成功"));
    }

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "管理员权限测试", notes = "用于测试当前用户是否具有 ADMIN 角色权限")
    public ResponseEntity<ApiResponse> adminTest() {
        return ResponseEntity.ok(ApiResponse.ok("您有管理员权限"));
    }
}
