package com.agritrace.service;

import com.agritrace.crypto.DigitalSignature;
import com.agritrace.entity.User;
import com.agritrace.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户服务类
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DigitalSignature digitalSignature;

    /**
     * 用户注册
     */
    public User registerUser(String username, String password, String realName,
            String userType, String companyName, String email, String phone) {
        // 检查用户名是否已存在
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, username))) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建用户
        User user = User.create(username, password, realName, userType);
        user.setCompanyName(companyName);
        user.setEmail(email);
        user.setPhone(phone);

        // 生成RSA密钥对
        Map<String, String> keys = DigitalSignature.KeyPairGenerator.generateKeyPair();
        user.setKeyPair(keys.get("publicKey"), keys.get("privateKey"));

        // 生成区块链地址
        String address = digitalSignature.generateAddress(keys.get("publicKey"));
        user.setBlockchainAddress(address);

        // 保存用户
        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        User user = Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password)))
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!user.isActive()) {
            throw new RuntimeException("账户已被禁用");
        }

        // 更新登录时间
        user.updateLastLogin();
        userMapper.updateById(user);

        // 构建返回数据（不包含私钥）
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("userType", user.getUserType());
        result.put("userTypeName", user.getUserTypeName());
        result.put("companyName", user.getCompanyName());
        result.put("blockchainAddress", user.getBlockchainAddress());
        result.put("publicKey", user.getPublicKey());
        result.put("verified", user.isVerified());
        result.put("role", user.getRole());

        return result;
    }

    /**
     * 根据ID获取用户
     */
    public User getUserById(String userId) {
        return Optional.ofNullable(userMapper.selectById(userId))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)))
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    /**
     * 根据类型获取用户
     */
    public List<User> getUsersByType(String userType) {
        return userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getUserType, userType));
    }

    /**
     * 更新用户信息
     */
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

    /**
     * 验证用户
     */
    public User verifyUser(String userId) {
        User user = getUserById(userId);
        user.setVerified(true);
        userMapper.updateById(user);
        return user;
    }

    /**
     * 禁用/启用用户
     */
    public User toggleUserStatus(String userId) {
        User user = getUserById(userId);
        user.setActive(!user.isActive());
        userMapper.updateById(user);
        return user;
    }

    /**
     * 删除用户
     */
    public void deleteUser(String userId) {
        userMapper.deleteById(userId);
    }

    /**
     * 获取用户统计信息
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("verifiedUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::isVerified, true)));
        stats.put("activeUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::isActive, true)));

        // 按类型统计
        Map<String, Long> byType = new HashMap<>();
        for (User user : userMapper.selectList(null)) {
            String type = user.getUserType();
            byType.put(type, byType.getOrDefault(type, 0L) + 1);
        }
        stats.put("usersByType", byType);

        return stats;
    }
}
