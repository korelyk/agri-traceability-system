package com.agritrace.dto;

import com.agritrace.entity.User;
import lombok.Data;

@Data
public class UserView {

    private String userId;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String userType;
    private String userTypeName;
    private String companyName;
    private String blockchainAddress;
    private boolean verified;
    private boolean active;
    private String role;

    public static UserView from(User user) {
        UserView view = new UserView();
        view.setUserId(user.getUserId());
        view.setUsername(user.getUsername());
        view.setRealName(user.getRealName());
        view.setEmail(user.getEmail());
        view.setPhone(user.getPhone());
        view.setUserType(user.getUserType());
        view.setUserTypeName(user.getUserTypeName());
        view.setCompanyName(user.getCompanyName());
        view.setBlockchainAddress(user.getBlockchainAddress());
        view.setVerified(user.isVerified());
        view.setActive(user.isActive());
        view.setRole(user.getRole());
        return view;
    }
}
