package com.agritrace.service;

import com.agritrace.config.JwtUtil;
import com.agritrace.crypto.DigitalSignature;
import com.agritrace.dto.UserView;
import com.agritrace.entity.User;
import com.agritrace.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DigitalSignature digitalSignature;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private KeySecurityService keySecurityService;

    public User registerUser(String username, String password, String realName,
            String userType, String companyName, String email, String phone) {
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, username))) {
            throw new RuntimeException("用户名已存在");
        }

        User user = User.create(username, password, realName, userType);
        user.setPassword(passwordEncoder.encode(password));
        user.setCompanyName(companyName);
        user.setEmail(email);
        user.setPhone(phone);

        Map<String, String> keys = DigitalSignature.KeyPairGenerator.generateKeyPair();
        user.setKeyPair(keys.get("publicKey"), keySecurityService.encrypt(keys.get("privateKey")));
        user.setBlockchainAddress(digitalSignature.generateAddress(keys.get("publicKey")));

        userMapper.insert(user);
        return user;
    }

    public Map<String, Object> login(String username, String password) {
        User user = Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)))
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!matchesPassword(password, user)) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.isActive()) {
            throw new RuntimeException("账户已被禁用");
        }

        upgradePasswordIfNeeded(user, password);
        upgradePrivateKeyIfNeeded(user);

        user.updateLastLogin();
        userMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.generateToken(user));
        result.put("user", toUserView(user));
        return result;
    }

    public User getUserById(String userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User getUserByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public List<UserView> getAllUsers() {
        return userMapper.selectList(null).stream()
                .map(this::toUserView)
                .toList();
    }

    public List<UserView> getUsersByType(String userType) {
        return userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getUserType, userType)).stream()
                .map(this::toUserView)
                .toList();
    }

    public User updateUser(String userId, Map<String, String> updates) {
        User user = getUserById(userId);

        if (updates.containsKey("realName")) {
            user.setRealName(updates.get("realName"));
        }
        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone(updates.get("phone"));
        }
        if (updates.containsKey("companyName")) {
            user.setCompanyName(updates.get("companyName"));
        }

        userMapper.updateById(user);
        return user;
    }

    public User verifyUser(String userId) {
        User user = getUserById(userId);
        user.setVerified(true);
        userMapper.updateById(user);
        return user;
    }

    public User toggleUserStatus(String userId) {
        User user = getUserById(userId);
        user.setActive(!user.isActive());
        userMapper.updateById(user);
        return user;
    }

    public void deleteUser(String userId) {
        userMapper.deleteById(userId);
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("verifiedUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::isVerified, true)));
        stats.put("activeUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::isActive, true)));

        Map<String, Long> byType = new HashMap<>();
        for (User user : userMapper.selectList(null)) {
            String type = user.getUserType();
            byType.put(type, byType.getOrDefault(type, 0L) + 1);
        }
        stats.put("usersByType", byType);

        return stats;
    }

    public UserView toUserView(User user) {
        return UserView.from(user);
    }

    private boolean matchesPassword(String rawPassword, User user) {
        String storedPassword = user.getPassword();
        if (storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }
        if (isEncodedPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private void upgradePasswordIfNeeded(User user, String rawPassword) {
        if (!isEncodedPassword(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
    }

    private void upgradePrivateKeyIfNeeded(User user) {
        if (user.getPrivateKey() != null && !user.getPrivateKey().isBlank()
                && !keySecurityService.isEncrypted(user.getPrivateKey())) {
            user.setPrivateKey(keySecurityService.encrypt(user.getPrivateKey()));
        }
    }

    private boolean isEncodedPassword(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
