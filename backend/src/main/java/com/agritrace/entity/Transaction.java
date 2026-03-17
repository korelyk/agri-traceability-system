package com.agritrace.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Data
public class Transaction {

    private String transactionId;
    private String transactionHash;
    private long timestamp;

    private String fromAddress;
    private String toAddress;
    private String fromPublicKey;

    private String productId;
    private String productName;
    private String productCategory;
    private String batchNumber;

    private String operationType;
    private String operationDetail;
    private String operatorId;
    private String operatorName;
    private String location;
    private String locationCode;

    private String qualityGrade;
    private String inspectionResult;
    private String certificateNo;

    private Double temperature;
    private Double humidity;
    private String environmentData;

    private String digitalSignature;

    private String metadata;
    private String documentHash;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Transaction() {
        this.transactionId = UUID.randomUUID().toString();
        this.timestamp = Instant.now().getEpochSecond();
    }

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

    public String calculateHash() {
        try {
            String data = transactionId + timestamp
                    + (fromAddress != null ? fromAddress : "")
                    + (productId != null ? productId : "")
                    + (operationType != null ? operationType : "")
                    + (operationDetail != null ? operationDetail : "");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算交易哈希失败", e);
        }
    }

    public boolean verifySignature(String publicKeyBase64) {
        if (digitalSignature == null || digitalSignature.isEmpty() || publicKeyBase64 == null || publicKeyBase64.isEmpty()) {
            return false;
        }

        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(buildSignaturePayload().getBytes(StandardCharsets.UTF_8));

            return signature.verify(Base64.getDecoder().decode(digitalSignature));
        } catch (Exception e) {
            return false;
        }
    }

    private String buildSignaturePayload() {
        return String.join("|",
                safe(transactionId),
                String.valueOf(timestamp),
                safe(productId),
                safe(productName),
                safe(operationType),
                safe(operatorId),
                safe(location),
                safe(operationDetail));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public String getSummary() {
        return String.format("Transaction[%s]: %s - %s - %s - %s",
                transactionId.substring(0, 8),
                productId,
                operationType,
                operatorName,
                location);
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static Transaction fromJson(String json) {
        return gson.fromJson(json, Transaction.class);
    }
}
