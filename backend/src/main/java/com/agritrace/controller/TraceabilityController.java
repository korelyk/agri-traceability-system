package com.agritrace.controller;

import com.agritrace.entity.Product;
import com.agritrace.entity.TraceRecord;
import com.agritrace.entity.User;
import com.agritrace.service.TraceabilityService;
import com.agritrace.service.UserService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TraceabilityController {

    @Autowired
    private TraceabilityService traceabilityService;

    @Autowired
    private UserService userService;

    private final Gson gson = new Gson();

    @PostMapping("/products/register")
    public ResponseEntity<?> registerProduct(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String producerId = firstNonBlank(
                    getString(request, "producerId"),
                    (String) httpRequest.getAttribute("currentUserId"));

            Product product = traceabilityService.registerProduct(
                    getString(request, "productName"),
                    getString(request, "productCategory"),
                    producerId,
                    getString(request, "origin"),
                    getString(request, "description"));

            return ResponseEntity.ok(success("产品注册成功", product));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("产品注册失败: " + ex.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        List<Product> products = traceabilityService.getAllProducts();
        return ResponseEntity.ok(success(products));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable String productId) {
        try {
            return ResponseEntity.ok(success(traceabilityService.getProductById(productId)));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("获取产品详情失败: " + ex.getMessage()));
        }
    }

    @PostMapping("/trace/add")
    public ResponseEntity<?> addTraceRecord(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String operatorId = firstNonBlank(
                    getString(request, "operatorId"),
                    (String) httpRequest.getAttribute("currentUserId"));

            TraceRecord record = traceabilityService.addTraceRecord(
                    getString(request, "productId"),
                    getString(request, "operationType"),
                    operatorId,
                    getString(request, "location"),
                    getString(request, "operationDetail"),
                    buildEnvironmentData(request),
                    getString(request, "qualityCheckResult"),
                    getString(request, "certificateNo"),
                    getString(request, "documentHash"));

            return ResponseEntity.ok(success("溯源记录添加成功", record));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("添加溯源记录失败: " + ex.getMessage()));
        }
    }

    @GetMapping("/trace/{productId}")
    public ResponseEntity<?> traceProduct(@PathVariable String productId) {
        try {
            return ResponseEntity.ok(success(traceabilityService.traceProduct(productId)));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("查询溯源信息失败: " + ex.getMessage()));
        }
    }

    @GetMapping("/trace/qr/{productId}")
    public ResponseEntity<?> traceByQRCode(@PathVariable String productId) {
        return traceProduct(productId);
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> request) {
        try {
            User user = userService.registerUser(
                    getString(request, "username"),
                    getString(request, "password"),
                    getString(request, "realName"),
                    getString(request, "userType"),
                    getString(request, "companyName"),
                    getString(request, "email"),
                    getString(request, "phone"));

            return ResponseEntity.ok(success("用户注册成功", userService.toUserView(user)));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("用户注册失败: " + ex.getMessage()));
        }
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = userService.login(
                    getString(request, "username"),
                    getString(request, "password"));

            return ResponseEntity.ok(success("登录成功", result));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("登录失败: " + ex.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(success(userService.getAllUsers()));
    }

    @GetMapping("/users/type/{userType}")
    public ResponseEntity<?> getUsersByType(@PathVariable String userType) {
        return ResponseEntity.ok(success(userService.getUsersByType(userType)));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId, HttpServletRequest request) {
        try {
            String currentUserId = (String) request.getAttribute("currentUserId");
            String currentUserRole = (String) request.getAttribute("currentUserRole");

            if (!"ADMIN".equals(currentUserRole)) {
                return ResponseEntity.status(403).body(error("仅管理员可删除用户"));
            }

            if (userId != null && userId.equals(currentUserId)) {
                return ResponseEntity.badRequest().body(error("不能删除当前登录用户"));
            }

            userService.deleteUser(userId);
            return ResponseEntity.ok(success("用户删除成功", null));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("用户删除失败: " + ex.getMessage()));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            String currentUserId = (String) httpRequest.getAttribute("currentUserId");
            String currentUserRole = (String) httpRequest.getAttribute("currentUserRole");

            if (!"ADMIN".equals(currentUserRole)) {
                return ResponseEntity.status(403).body(error("仅管理员可修改用户"));
            }

            Map<String, String> updates = new HashMap<>();
            copyIfPresent(request, updates, "realName");
            copyIfPresent(request, updates, "email");
            copyIfPresent(request, updates, "phone");
            copyIfPresent(request, updates, "companyName");
            copyIfPresent(request, updates, "userType");
            copyIfPresent(request, updates, "role");
            copyIfPresent(request, updates, "verified");
            copyIfPresent(request, updates, "active");

            if (userId != null && userId.equals(currentUserId)) {
                if ("false".equalsIgnoreCase(updates.get("active"))) {
                    return ResponseEntity.badRequest().body(error("不能停用当前登录用户"));
                }
                if (updates.containsKey("role") && !"ADMIN".equalsIgnoreCase(updates.get("role"))) {
                    return ResponseEntity.badRequest().body(error("不能取消当前登录用户的管理员角色"));
                }
                if (updates.containsKey("userType") && !"ADMIN".equalsIgnoreCase(updates.get("userType"))) {
                    return ResponseEntity.badRequest().body(error("不能修改当前登录用户的管理员类型"));
                }
            }

            User updatedUser = userService.updateUser(userId, updates);
            return ResponseEntity.ok(success("用户更新成功", userService.toUserView(updatedUser)));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("用户更新失败: " + ex.getMessage()));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(success(traceabilityService.getSystemStatistics()));
    }

    @GetMapping("/blockchain/statistics")
    public ResponseEntity<?> getBlockchainStatistics() {
        return ResponseEntity.ok(success(traceabilityService.getBlockchainStatistics()));
    }

    @GetMapping("/supervision/overview")
    public ResponseEntity<?> getSupervisionOverview() {
        return ResponseEntity.ok(success(traceabilityService.getSupervisionOverview()));
    }

    @GetMapping("/blockchain/blocks")
    public ResponseEntity<?> getBlocks() {
        return ResponseEntity.ok(success(traceabilityService.getAllBlocks()));
    }

    @PostMapping("/blockchain/rebuild")
    public ResponseEntity<?> rebuildBlockchain(HttpServletRequest request) {
        try {
            String currentUserRole = (String) request.getAttribute("currentUserRole");
            if (!"ADMIN".equals(currentUserRole)) {
                return ResponseEntity.status(403).body(error("仅管理员可执行区块链重建"));
            }

            Map<String, Object> result = traceabilityService.rebuildBlockchainFromDatabase();
            return ResponseEntity.ok(success("区块链与溯源数据重建成功", result));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("区块链重建失败: " + ex.getMessage()));
        }
    }

    @GetMapping("/verify/block/{blockHash}")
    public ResponseEntity<?> verifyBlock(@PathVariable String blockHash) {
        boolean valid = traceabilityService.verifyBlock(blockHash);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("valid", valid);
        response.put("message", valid ? "区块验证通过" : "区块验证失败");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify/transaction/{transactionId}")
    public ResponseEntity<?> verifyTransaction(@PathVariable String transactionId) {
        boolean valid = traceabilityService.verifyTransaction(transactionId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("valid", valid);
        response.put("message", valid ? "交易验证通过" : "交易验证失败");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify/transaction/detail/{transactionId}")
    public ResponseEntity<?> getTransactionVerificationDetail(@PathVariable String transactionId) {
        try {
            return ResponseEntity.ok(success(traceabilityService.getTransactionVerificationDetail(transactionId)));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("获取交易验签详情失败: " + ex.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "AgriTraceability System");
        health.put("version", "1.0.0");
        health.put("blockchainValid", traceabilityService.getBlockchainStatistics().get("chainValid"));
        return ResponseEntity.ok(health);
    }

    private Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> success(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    private String getString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private String buildEnvironmentData(Map<String, Object> request) {
        String environmentData = getString(request, "environmentData");
        if (environmentData != null && !environmentData.isBlank()) {
            return environmentData;
        }

        Object temperature = request.get("temperature");
        Object humidity = request.get("humidity");
        if (temperature == null && humidity == null) {
            return null;
        }

        Map<String, Object> payload = new HashMap<>();
        if (temperature != null) {
            payload.put("temperature", temperature);
        }
        if (humidity != null) {
            payload.put("humidity", humidity);
        }
        return gson.toJson(payload);
    }

    private void copyIfPresent(Map<String, Object> source, Map<String, String> target, String key) {
        if (source.containsKey(key)) {
            target.put(key, getString(source, key));
        }
    }
}
