package com.agritrace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;

    @TableField("username")
    private String username;

    @TableField("password")
    @JsonIgnore
    private String password;

    @TableField("real_name")
    private String realName;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("user_type")
    private String userType;

    @TableField("company_name")
    private String companyName;

    @TableField("company_code")
    private String companyCode;

    @TableField("business_license")
    private String businessLicense;

    @TableField("address")
    private String address;

    @TableField("region_code")
    private String regionCode;

    @TableField("public_key")
    private String publicKey;

    @TableField("private_key")
    @JsonIgnore
    private String privateKey;

    @TableField("blockchain_address")
    private String blockchainAddress;

    @TableField("certificate_hash")
    private String certificateHash;

    @TableField("is_verified")
    private boolean verified;

    @TableField("is_active")
    private boolean active;

    @TableField("role")
    private String role;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("last_login")
    private LocalDateTime lastLogin;

    public User() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.verified = false;
    }

    public static User create(String username, String password, String realName, String userType) {
        User user = new User();
        user.setUserId(java.util.UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setUserType(userType);
        user.setRole("OPERATOR");
        return user;
    }

    public void setKeyPair(String publicKey, String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public String getUserTypeName() {
        if (userType == null) {
            return "-";
        }
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
}
