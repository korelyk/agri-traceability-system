package com.agritrace.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

/**
 * 交易类 - 区块链中的基本数据单元
 * 用于记录农产品溯源信息
 */
@Data
public class Transaction {
    
    // 交易基本信息
    private String transactionId;       // 交易ID
    private String transactionHash;     // 交易哈希
    private long timestamp;             // 时间戳
    
    // 交易参与方
    private String fromAddress;         // 发送方地址（公钥哈希）
    private String toAddress;           // 接收方地址
    private String fromPublicKey;       // 发送方公钥
    
    // 交易内容（农产品溯源信息）
    private String productId;           // 产品ID
    private String productName;         // 产品名称
    private String productCategory;     // 产品类别
    private String batchNumber;         // 批次号
    
    // 溯源操作信息
    private String operationType;       // 操作类型
    private String operationDetail;     // 操作详情
    private String operatorId;          // 操作者ID
    private String operatorName;        // 操作者名称
    private String location;            // 操作地点
    private String locationCode;        // 地点编码
    
    // 质量信息
    private String qualityGrade;        // 质量等级
    private String inspectionResult;    // 检测结果
    private String certificateNo;       // 证书编号
    
    // 环境信息（物联网数据）
    private Double temperature;         // 温度
    private Double humidity;            // 湿度
    private String environmentData;     // 环境数据JSON
    
    // 数字签名
    private String digitalSignature;    // 数字签名
    
    // 附加数据
    private String metadata;            // 附加元数据（JSON格式）
    private String documentHash;        // 文档哈希（用于验证附件）
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public Transaction() {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = Instant.now().getEpochSecond();
    }
    
    /**
     * 创建溯源交易
     */
    public static Transaction createTraceTransaction(
            String productId,
            String productName,
            String operationType,
            String operatorId,
            String operatorName,
            String location,
            String operationDetail) {
        
        Transaction tx = new Transaction();
        tx.setProductId(productId);
        tx.setProductName(productName);
        tx.setOperationType(operationType);
        tx.setOperatorId(operatorId);
        tx.setOperatorName(operatorName);
        tx.setLocation(location);
        tx.setOperationDetail(operationDetail);
        tx.setTransactionHash(tx.calculateHash());
        
        return tx;
    }
    
    /**
     * 计算交易哈希
     */
    public String calculateHash() {
        try {
            String data = transactionId + timestamp + 
                         (fromAddress != null ? fromAddress : "") +
                         (productId != null ? productId : "") +
                         (operationType != null ? operationType : "") +
                         (operationDetail != null ? operationDetail : "");
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算交易哈希失败", e);
        }
    }
    
    /**
     * 验证交易签名
     */
    public boolean verifySignature(String publicKey) {
        // 使用公钥验证数字签名
        // 实际实现需要调用加密服务
        return digitalSignature != null && !digitalSignature.isEmpty();
    }
    
    /**
     * 生成交易摘要
     */
    public String getSummary() {
        return String.format("Transaction[%s]: %s - %s - %s - %s",
                transactionId.substring(0, 8),
                productId,
                operationType,
                operatorName,
                location);
    }
    
    /**
     * 转换为JSON
     */
    public String toJson() {
        return gson.toJson(this);
    }
    
    /**
     * 从JSON解析
     */
    public static Transaction fromJson(String json) {
        return gson.fromJson(json, Transaction.class);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "txId='" + transactionId.substring(0, 8) + "...'" +
                ", productId='" + productId + '\'' +
                ", operationType='" + operationType + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", location='" + location + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
