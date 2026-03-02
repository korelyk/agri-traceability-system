package com.agritrace.controller;

import com.agritrace.entity.Product;
import com.agritrace.entity.TraceRecord;
import com.agritrace.entity.User;
import com.agritrace.service.TraceabilityService;
import com.agritrace.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 溯源系统REST API控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TraceabilityController {
    
    @Autowired
    private TraceabilityService traceabilityService;
    
    @Autowired
    private UserService userService;
    
    // ==================== 产品管理 ====================
    
    /**
     * 注册新产品
     */
    @PostMapping("/products/register")
    public ResponseEntity<?> registerProduct(@RequestBody Map<String, String> request) {
        try {
            Product product = traceabilityService.registerProduct(
                    request.get("productName"),
                    request.get("productCategory"),
                    request.get("producerId"),
                    request.get("origin"),
                    request.get("description")
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "产品注册成功");
            response.put("data", product);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "产品注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 添加溯源记录
     */
    @PostMapping("/trace/add")
    public ResponseEntity<?> addTraceRecord(@RequestBody Map<String, String> request) {
        try {
            TraceRecord record = traceabilityService.addTraceRecord(
                    request.get("productId"),
                    request.get("operationType"),
                    request.get("operatorId"),
                    request.get("location"),
                    request.get("operationDetail"),
                    request.get("environmentData")
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "溯源记录添加成功");
            response.put("data", record);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "添加溯源记录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 查询产品溯源信息
     */
    @GetMapping("/trace/{productId}")
    public ResponseEntity<?> traceProduct(@PathVariable String productId) {
        try {
            Map<String, Object> result = traceabilityService.traceProduct(productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "查询溯源信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 扫描二维码查询
     */
    @GetMapping("/trace/qr/{productId}")
    public ResponseEntity<?> traceByQRCode(@PathVariable String productId) {
        return traceProduct(productId);
    }
    
    // ==================== 用户管理 ====================
    
    /**
     * 用户注册
     */
    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            User user = userService.registerUser(
                    request.get("username"),
                    request.get("password"),
                    request.get("realName"),
                    request.get("userType"),
                    request.get("companyName"),
                    request.get("email"),
                    request.get("phone")
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "用户注册成功");
            response.put("data", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "用户注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            Map<String, Object> result = userService.login(
                    request.get("username"),
                    request.get("password")
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取所有用户
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据类型获取用户
     */
    @GetMapping("/users/type/{userType}")
    public ResponseEntity<?> getUsersByType(@PathVariable String userType) {
        List<User> users = userService.getUsersByType(userType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users);
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== 统计信息 ====================
    
    /**
     * 获取系统统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        Map<String, Object> stats = traceabilityService.getSystemStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取区块链统计信息
     */
    @GetMapping("/blockchain/statistics")
    public ResponseEntity<?> getBlockchainStatistics() {
        Map<String, Object> stats = traceabilityService.getBlockchainStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== 验证接口 ====================
    
    /**
     * 验证区块
     */
    @GetMapping("/verify/block/{blockHash}")
    public ResponseEntity<?> verifyBlock(@PathVariable String blockHash) {
        boolean isValid = traceabilityService.verifyBlock(blockHash);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("valid", isValid);
        response.put("message", isValid ? "区块验证通过" : "区块验证失败");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 验证交易
     */
    @GetMapping("/verify/transaction/{transactionId}")
    public ResponseEntity<?> verifyTransaction(@PathVariable String transactionId) {
        boolean isValid = traceabilityService.verifyTransaction(transactionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("valid", isValid);
        response.put("message", isValid ? "交易验证通过" : "交易验证失败");
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== 健康检查 ====================
    
    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "AgriTraceability System");
        health.put("version", "1.0.0");
        health.put("blockchainValid", traceabilityService.getBlockchainStatistics().get("chainValid"));
        
        return ResponseEntity.ok(health);
    }
}
