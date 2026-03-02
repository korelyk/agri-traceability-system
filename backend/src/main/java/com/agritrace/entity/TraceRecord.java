package com.agritrace.entity;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 溯源记录实体类
 * 记录农产品在供应链中的每个环节信息
 */
@Data
@TableName( "trace_records")
public class TraceRecord {
    
        @TableId(value = "record_id", type = IdType.INPUT)
    private String recordId;            // 记录ID
    
    @TableField("product_id")
    private String productId;           // 产品ID
    
    @TableField("operation_type")
    private String operationType;       // 操作类型
    
    @TableField("operation_detail")
    private String operationDetail;     // 操作详情
    
    @TableField("operator_id")
    private String operatorId;          // 操作者ID
    
    @TableField("operator_name")
    private String operatorName;        // 操作者名称
    
    @TableField("operator_type")
    private String operatorType;        // 操作者类型（生产者/加工商/物流/销售商）
    
    @TableField("location")
    private String location;            // 操作地点
    
    @TableField("location_code")
    private String locationCode;        // 地点编码
    
    @TableField("operation_time")
    private LocalDateTime operationTime;  // 操作时间
    
    @TableField("temperature")
    private Double temperature;         // 温度（物联网数据）
    
    @TableField("humidity")
    private Double humidity;            // 湿度（物联网数据）
    
    @TableField("environment_data")
    private String environmentData;     // 环境数据JSON
    
    @TableField("quality_check_result")
    private String qualityCheckResult;  // 质量检测结果
    
    @TableField("certificate_no")
    private String certificateNo;       // 证书编号
    
    @TableField("document_hash")
    private String documentHash;        // 附件文档哈希
    
    @TableField("block_hash")
    private String blockHash;           // 区块哈希
    
    @TableField("transaction_id")
    private String transactionId;       // 交易ID
    
    @TableField("digital_signature")
    private String digitalSignature;    // 数字签名
    
    @TableField("signer_public_key")
    private String signerPublicKey;     // 签名者公钥
    
    @TableField("previous_record_id")
    private String previousRecordId;    // 前一条记录ID（形成链式结构）
    
    @TableField("created_at")
    private LocalDateTime createdAt;    // 创建时间
    
    @TableField("verified")
    private boolean verified;           // 是否已验证
    
    public TraceRecord() {
        this.operationTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.verified = false;
    }
    
    /**
     * 创建溯源记录
     */
    public static TraceRecord create(
            String productId,
            String operationType,
            String operatorId,
            String operatorName,
            String operatorType,
            String location,
            String operationDetail) {
        
        TraceRecord record = new TraceRecord();
        record.setRecordId(java.util.UUID.randomUUID().toString());
        record.setProductId(productId);
        record.setOperationType(operationType);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setOperatorType(operatorType);
        record.setLocation(location);
        record.setOperationDetail(operationDetail);
        
        return record;
    }
    
    /**
     * 设置物联网环境数据
     */
    public void setEnvironmentData(Double temperature, Double humidity, String additionalData) {
        this.temperature = temperature;
        this.humidity = humidity;
        if (additionalData != null) {
            this.environmentData = additionalData;
        }
    }
    
    /**
     * 设置区块链关联信息
     */
    public void setBlockchainInfo(String blockHash, String transactionId) {
        this.blockHash = blockHash;
        this.transactionId = transactionId;
    }
    
    /**
     * 设置数字签名
     */
    public void setSignature(String signature, String publicKey) {
        this.digitalSignature = signature;
        this.signerPublicKey = publicKey;
    }
    
    /**
     * 获取操作类型中文名称
     */
    public String getOperationTypeName() {
        switch (operationType) {
            case "PRODUCE":
                return "生产";
            case "PROCESS":
                return "加工";
            case "TRANSPORT":
                return "运输";
            case "STORAGE":
                return "仓储";
            case "SALE":
                return "销售";
            case "INSPECT":
                return "检测";
            case "PACKAGE":
                return "包装";
            default:
                return operationType;
        }
    }
    
    /**
     * 获取记录摘要
     */
    public String getSummary() {
        return String.format("Trace[%s]: %s - %s - %s - %s",
                recordId.substring(0, 8),
                operationType,
                operatorName,
                location,
                operationTime);
    }
    
    @Override
    public String toString() {
        return "TraceRecord{" +
                "recordId='" + recordId + '\'' +
                ", productId='" + productId + '\'' +
                ", operationType='" + operationType + '\'' +
                ", operatorName='" + operatorName + '\'' +
                ", location='" + location + '\'' +
                ", operationTime=" + operationTime +
                ", blockHash='" + (blockHash != null ? blockHash.substring(0, 10) + "..." : null) + '\'' +
                '}';
    }
}
