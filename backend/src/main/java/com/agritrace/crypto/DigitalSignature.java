package com.agritrace.crypto;

import com.agritrace.entity.Transaction;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 数字签名服务
 * 基于RSA算法实现数字签名和验证
 * 用于确保农产品溯源数据的完整性和不可抵赖性
 */
@Component
public class DigitalSignature {

    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int KEY_SIZE = 2048;

    private final Gson gson = new Gson();

    /**
     * 密钥对生成器
     */
    public static class KeyPairGenerator {

        /**
         * 生成RSA密钥对
         */
        public static Map<String, String> generateKeyPair() {
            try {
                java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance(ALGORITHM);
                keyGen.initialize(KEY_SIZE);
                KeyPair keyPair = keyGen.generateKeyPair();

                // 获取公钥和私钥
                PublicKey publicKey = keyPair.getPublic();
                PrivateKey privateKey = keyPair.getPrivate();

                // 编码为Base64字符串
                String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

                Map<String, String> keys = new HashMap<>();
                keys.put("publicKey", publicKeyBase64);
                keys.put("privateKey", privateKeyBase64);

                return keys;
            } catch (Exception e) {
                throw new RuntimeException("生成密钥对失败", e);
            }
        }
    }

    /**
     * 对数据进行数字签名
     * 
     * @param data             待签名数据
     * @param privateKeyBase64 Base64编码的私钥
     * @return Base64编码的签名
     */
    public String sign(String data, String privateKeyBase64) {
        try {
            // 解码私钥
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // 创建签名对象
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            // 生成签名
            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("数字签名失败", e);
        }
    }

    /**
     * 对交易进行签名
     */
    public String signTransaction(Transaction transaction, String privateKeyBase64) {
        // 构建交易数据的字符串表示
        String data = buildTransactionData(transaction);
        return sign(data, privateKeyBase64);
    }

    /**
     * 验证数字签名
     * 
     * @param data            原始数据
     * @param signatureBase64 Base64编码的签名
     * @param publicKeyBase64 Base64编码的公钥
     * @return 验证结果
     */
    public boolean verify(String data, String signatureBase64, String publicKeyBase64) {
        try {
            // 解码公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // 解码签名
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            // 验证签名
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            return signature.verify(signatureBytes);
        } catch (Exception e) {
            System.err.println("签名验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 验证交易签名
     */
    public boolean verifyTransaction(Transaction transaction) {
        if (transaction.getDigitalSignature() == null ||
                transaction.getFromPublicKey() == null) {
            return false;
        }

        String data = buildTransactionData(transaction);
        return verify(data, transaction.getDigitalSignature(), transaction.getFromPublicKey());
    }

    public String getTransactionPayload(Transaction transaction) {
        return buildTransactionData(transaction);
    }

    /**
     * 构建交易数据字符串
     */
    private String buildTransactionData(Transaction transaction) {
        StringBuilder sb = new StringBuilder();
        sb.append(transaction.getTransactionId()).append("|");
        sb.append(transaction.getTimestamp()).append("|");
        sb.append(transaction.getProductId()).append("|");
        sb.append(transaction.getProductName()).append("|");
        sb.append(transaction.getOperationType()).append("|");
        sb.append(transaction.getOperatorId()).append("|");
        sb.append(transaction.getLocation()).append("|");
        sb.append(transaction.getOperationDetail());
        return sb.toString();
    }

    /**
     * 计算数据的SHA-256哈希
     */
    public String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算哈希失败", e);
        }
    }

    /**
     * 从公钥生成地址
     */
    public String generateAddress(String publicKeyBase64) {
        // 对公钥进行哈希，生成地址
        String hash = calculateHash(publicKeyBase64);
        // 取前40位作为地址
        return "AT" + hash.substring(0, 38); // AT = AgriTrace
    }

    /**
     * 生成证书指纹
     */
    public String generateCertificateFingerprint(String certificateData) {
        return calculateHash(certificateData);
    }

    /**
     * 验证证书有效性
     */
    public boolean verifyCertificate(String certificateData, String signature, String issuerPublicKey) {
        return verify(certificateData, signature, issuerPublicKey);
    }

    /**
     * 测试数字签名功能
     */
    public static void main(String[] args) {
        DigitalSignature ds = new DigitalSignature();

        // 生成密钥对
        Map<String, String> keys = KeyPairGenerator.generateKeyPair();
        String publicKey = keys.get("publicKey");
        String privateKey = keys.get("privateKey");

        System.out.println("=== 数字签名测试 ===");
        System.out.println("公钥: " + publicKey.substring(0, 50) + "...");
        System.out.println("私钥: " + privateKey.substring(0, 50) + "...");

        // 测试数据
        String data = "农产品溯源数据: 产品ID=P001, 操作=种植, 操作者=张三, 地点=山东";

        // 签名
        String signature = ds.sign(data, privateKey);
        System.out.println("\n原始数据: " + data);
        System.out.println("数字签名: " + signature.substring(0, 50) + "...");

        // 验证
        boolean isValid = ds.verify(data, signature, publicKey);
        System.out.println("\n签名验证结果: " + (isValid ? "有效" : "无效"));

        // 测试篡改数据
        String tamperedData = data + "(篡改)";
        boolean isTamperedValid = ds.verify(tamperedData, signature, publicKey);
        System.out.println("篡改后验证结果: " + (isTamperedValid ? "有效" : "无效"));

        // 生成地址
        String address = ds.generateAddress(publicKey);
        System.out.println("\n生成地址: " + address);
    }
}
