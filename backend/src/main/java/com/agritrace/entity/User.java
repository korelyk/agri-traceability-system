package com.agritrace.entity;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 系统用户（生产者、加工商、物流商、销售商、管理员等）
 */
@Data
@TableName( "users")
public class User {
    
        @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;              // 用户ID
    
    @TableField("username")
    private String username;            // 用户名
    
    @TableField("password")
    private String password;            // 密码（加密存储）
    
    @TableField("real_name")
    private String realName;            // 真实姓名
    
    @TableField("email")
    private String email;               // 邮箱
    
    @TableField("phone")
    private String phone;               // 电话
    
    @TableField("user_type")
    private String userType;            // 用户类型
    
    @TableField("company_name")
    private String companyName;         // 公司名称
    
    @TableField("company_code")
    private String companyCode;         // 公司编码
    
    @TableField("business_license")
    private String businessLicense;     // 营业执照号
    
    @TableField("address")
    private String address;             // 地址
    
    @TableField("region_code")
    private String regionCode;          // 地区编码
    
    @TableField("public_key")
    private String publicKey;           // RSA公钥
    
    @TableField("private_key")
    private String privateKey;          // RSA私钥（加密存储）
    
    @TableField("blockchain_address")
    private String blockchainAddress;   // 区块链地址
    
    @TableField("certificate_hash")
    private String certificateHash;     // 证书哈希
    
    @TableField("is_verified")
    private boolean verified;           // 是否已认证
    
    @TableField("is_active")
    private boolean active;             // 是否激活
    
    @TableField("role")
    private String role;                // 角色（ADMIN/OPERATOR/VIEWER）
    
    @TableField("created_at")
    private LocalDateTime createdAt;    // 创建时间
    
    @TableField("last_login")
    private LocalDateTime lastLogin;    // 最后登录时间
    
    public User() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.verified = false;
    }
    
    /**
     * 创建用户
     */
    public static User create(String username, String password, 
                               String realName, String userType) {
        User user = new User();
        user.setUserId(java.util.UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setUserType(userType);
        user.setRole("OPERATOR");
        return user;
    }
    
    /**
     * 设置密钥对
     */
    public void setKeyPair(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
    
    /**
     * 更新登录时间
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    /**
     * 获取用户类型中文名称
     */
    public String getUserTypeName() {
        switch (userType) {
            case "PRODUCER":
                return "生产者";
            case "PROCESSOR":
                return "加工商";
            case "LOGISTICS":
                return "物流商";
            case "RETAILER":
                return "销售商";
            case "INSPECTOR":
                return "检测机构";
            case "ADMIN":
                return "管理员";
            default:
                return userType;
        }
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", userType='" + userType + '\'' +
                ", companyName='" + companyName + '\'' +
                ", verified=" + verified +
                ", active=" + active +
                '}';
    }
}
