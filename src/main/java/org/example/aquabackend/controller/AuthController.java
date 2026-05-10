package org.example.aquabackend.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
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
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private DefaultKaptcha captchaProducer;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取验证码图片（Base64 格式）
     */
    @GetMapping("/captcha")
    public ResponseEntity<ApiResponse> getCaptcha(HttpSession session) throws IOException {
        String captchaText = captchaProducer.createText();
        session.setAttribute("captcha", captchaText);
        session.setMaxInactiveInterval(300); // 5 分钟有效期

        BufferedImage image = captchaProducer.createImage(captchaText);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        logger.info("Generated captcha for session: {}", session.getId());

        ApiResponse response = ApiResponse.ok("验证码生成成功")
                .put("captchaImage", "data:image/jpeg;base64," + base64Image)
                .put("captchaKey", session.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * 用户登录（JSON 格式请求体）
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpSession session) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String captcha = loginRequest.getCaptcha();

        logger.info("Login attempt for user: {}", username);

        // 验证码校验（如果传了验证码）
        if (captcha != null && !captcha.isEmpty()) {
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
                logger.warn("Invalid captcha for user: {}", username);
                return ResponseEntity.badRequest().body(ApiResponse.fail("验证码错误"));
            }
        }

        // 查找用户
        User user = userService.findByUsername(username);
        if (user == null || !userService.checkPassword(password, user.getPassword())) {
            logger.warn("Invalid username or password for user: {}", username);
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户名或密码错误"));
        }

        // 检查账号状态
        if (Boolean.FALSE.equals(user.getEnabled())) {
            return ResponseEntity.status(403).body(ApiResponse.fail("账户已被禁用"));
        }
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            return ResponseEntity.status(403).body(ApiResponse.fail("账户已被锁定"));
        }
        if (Boolean.TRUE.equals(user.getAccountExpired())) {
            return ResponseEntity.status(403).body(ApiResponse.fail("账户已过期"));
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(username);

        // 登录成功后清除验证码
        session.removeAttribute("captcha");

        logger.info("User {} logged in successfully, role: {}", username, user.getRole());

        ApiResponse response = ApiResponse.ok("登录成功")
                .put("token", token)
                .put("username", username)
                .put("role", user.getRole())
                .put("userId", user.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                 HttpSession session) {
        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String email = registerRequest.getEmail();
        String captcha = registerRequest.getCaptcha();

        logger.info("Registration attempt for user: {}", username);

        // 验证码校验
        if (captcha != null && !captcha.isEmpty()) {
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("验证码错误"));
            }
        }

        // 检查用户名是否已存在
        if (userService.findByUsername(username) != null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户名已存在"));
        }

        // 注册用户
        User newUser = userService.register(username, password, email);

        // 自动登录（生成 token）
        String token = jwtUtil.generateToken(username);

        logger.info("User registered successfully: {}", username);

        ApiResponse response = ApiResponse.ok("注册成功")
                .put("token", token)
                .put("username", username)
                .put("userId", newUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
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

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("退出登录成功"));
    }

    /**
     * 管理员专用测试端点
     */
    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> adminTest() {
        return ResponseEntity.ok(ApiResponse.ok("您有管理员权限"));
    }
}
