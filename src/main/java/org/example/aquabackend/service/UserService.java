package org.example.aquabackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.aquabackend.entity.User;
import org.example.aquabackend.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByUsername(String username) {
        logger.info("Searching for user: {}", username);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user != null) {
            logger.info("User found: {}", username);
        } else {
            logger.warn("User not found: {}", username);
        }
        return user;
    }

    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    public List<User> findAll() {
        return userMapper.selectList(null);
    }

    public User register(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("USER");
        user.setEnabled(true);
        userMapper.insert(user);
        logger.info("User registered: {}", username);
        return user;
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        logger.info("Password match result: {}", matches);
        return matches;
    }

    public void updatePassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userMapper.updateById(user);
            logger.info("Password updated for user: {}", user.getUsername());
        }
    }

    public boolean deleteUser(Long userId) {
        int result = userMapper.deleteById(userId);
        return result > 0;
    }
}
