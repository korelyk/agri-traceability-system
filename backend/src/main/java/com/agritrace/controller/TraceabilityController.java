package com.agritrace.controller;

import com.agritrace.entity.Product;
import com.agritrace.entity.TraceRecord;
import com.agritrace.service.TraceabilityService;
import com.agritrace.service.UserService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("产品注册失败: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        List<Product> products = traceabilityService.getAllProducts();
        return ResponseEntity.ok(success(products));
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
                    buildEnvironmentData(request));

            return ResponseEntity.ok(success("溯源记录添加成功", record));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("添加溯源记录失败: " + e.getMessage()));
        }
    }

    @GetMapping("/trace/{productId}")
    public ResponseEntity<?> traceProduct(@PathVariable String productId) {
        try {
            return ResponseEntity.ok(success(traceabilityService.traceProduct(productId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("查询溯源信息失败: " + e.getMessage()));
        }
    }

    @GetMapping("/trace/qr/{productId}")
    public ResponseEntity<?> traceByQRCode(@PathVariable String productId) {
        return traceProduct(productId);
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> request) {
        try {
            var user = userService.registerUser(
                    getString(request, "username"),
                    getString(request, "password"),
                    getString(request, "realName"),
                    getString(request, "userType"),
                    getString(request, "companyName"),
                    getString(request, "email"),
                    getString(request, "phone"));

            return ResponseEntity.ok(success("用户注册成功", userService.toUserView(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("用户注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = userService.login(
                    getString(request, "username"),
                    getString(request, "password"));

            return ResponseEntity.ok(success("登录成功", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("登录失败: " + e.getMessage()));
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

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        return ResponseEntity.ok(success(traceabilityService.getSystemStatistics()));
    }

    @GetMapping("/blockchain/statistics")
    public ResponseEntity<?> getBlockchainStatistics() {
        return ResponseEntity.ok(success(traceabilityService.getBlockchainStatistics()));
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
}
