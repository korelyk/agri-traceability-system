package com.agritrace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("trace_records")
public class TraceRecord {

    @TableId(value = "record_id", type = IdType.INPUT)
    private String recordId;

    @TableField("product_id")
    private String productId;

    @TableField("operation_type")
    private String operationType;

    @TableField("operation_detail")
    private String operationDetail;

    @TableField("operator_id")
    private String operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("operator_type")
    private String operatorType;

    @TableField("location")
    private String location;

    @TableField("location_code")
    private String locationCode;

    @TableField("operation_time")
    private LocalDateTime operationTime;

    @TableField("temperature")
    private Double temperature;

    @TableField("humidity")
    private Double humidity;

    @TableField("environment_data")
    private String environmentData;

    @TableField("quality_check_result")
    private String qualityCheckResult;

    @TableField("certificate_no")
    private String certificateNo;

    @TableField("document_hash")
    private String documentHash;

    @TableField("block_hash")
    private String blockHash;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("digital_signature")
    private String digitalSignature;

    @TableField("signer_public_key")
    private String signerPublicKey;

    @TableField("previous_record_id")
    private String previousRecordId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("verified")
    private boolean verified;

    public TraceRecord() {
        this.operationTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.verified = false;
    }

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

    public void setEnvironmentData(Double temperature, Double humidity, String additionalData) {
        this.temperature = temperature;
        this.humidity = humidity;
        if (additionalData != null) {
            this.environmentData = additionalData;
        }
    }

    public void setBlockchainInfo(String blockHash, String transactionId) {
        this.blockHash = blockHash;
        this.transactionId = transactionId;
    }

    public void setSignature(String signature, String publicKey) {
        this.digitalSignature = signature;
        this.signerPublicKey = publicKey;
    }

    public String getOperationTypeName() {
        if (operationType == null) {
            return "-";
        }
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
