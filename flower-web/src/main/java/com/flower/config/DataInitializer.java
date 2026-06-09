package com.flower.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.entity.User;
import com.flower.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initAdminUser() {
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, "admin");
            User admin = userMapper.selectOne(wrapper);

            if (admin == null) {
                log.warn("Admin user not found, creating default admin (admin/123456)...");
                admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setPhone("13900000000");
                admin.setEmail("admin@flower.com");
                admin.setUserType("admin");
                admin.setStatus("1");
                userMapper.insert(admin);
                log.info("Default admin user created successfully.");
            } else {
                String rawPassword = "123456";
                if (!passwordEncoder.matches(rawPassword, admin.getPassword())) {
                    log.warn("Admin password hash mismatch, resetting to default...");
                    admin.setPassword(passwordEncoder.encode(rawPassword));
                    userMapper.updateById(admin);
                    log.info("Admin password reset successfully.");
                } else {
                    log.info("Admin user verified OK.");
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize admin user: {}", e.getMessage());
        }
    }
}
