package com.agritrace.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 区块类 - 区块链的基本组成单元
 * 包含区块头信息和交易数据
 */
@Data
public class Block {
    
    // 区块头信息
    private String blockHash;           // 当前区块哈希值
    private String previousHash;        // 前一个区块哈希值
    private String merkleRoot;          // Merkle树根哈希
    private long timestamp;             // 时间戳
    private int nonce;                  // 随机数（用于工作量证明）
    private int difficulty;             // 难度值
    
    // 区块体信息
    private int index;                  // 区块索引
    private List<Transaction> transactions;  // 交易列表
    private String digitalSignature;    // 数字签名
    private String signerPublicKey;     // 签名者公钥
    
    // 溯源相关信息
    private String traceId;             // 溯源ID
    private String productId;           // 产品ID
    private String operationType;       // 操作类型（生产/加工/物流/销售）
    private String operatorId;          // 操作者ID
    private String dataHash;            // 数据哈希
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public Block() {
        this.transactions = new ArrayList<>();
        this.timestamp = Instant.now().getEpochSecond();
        this.nonce = 0;
        this.difficulty = 4;  // 默认难度值
    }
    
    public Block(int index, String previousHash, List<Transaction> transactions) {
        this();
        this.index = index;
        this.previousHash = previousHash;
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        this.merkleRoot = calculateMerkleRoot();
        this.blockHash = calculateHash();
    }
    
    /**
     * 计算区块哈希值（使用SHA-256）
     */
    public String calculateHash() {
        try {
            String data = index + previousHash + merkleRoot + timestamp + nonce + difficulty;
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
            throw new RuntimeException("计算哈希失败", e);
        }
    }
    
    /**
     * 计算Merkle树根哈希
     */
    public String calculateMerkleRoot() {
        if (transactions == null || transactions.isEmpty()) {
            return "";
        }
        
        List<String> treeLayer = new ArrayList<>();
        for (Transaction tx : transactions) {
            treeLayer.add(tx.getTransactionHash());
        }
        
        while (treeLayer.size() > 1) {
            List<String> newLayer = new ArrayList<>();
            for (int i = 0; i < treeLayer.size(); i += 2) {
                String left = treeLayer.get(i);
                String right = (i + 1 < treeLayer.size()) ? treeLayer.get(i + 1) : left;
                newLayer.add(applySha256(left + right));
            }
            treeLayer = newLayer;
        }
        
        return treeLayer.get(0);
    }
    
    /**
     * 应用SHA-256哈希
     */
    private String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256计算失败", e);
        }
    }
    
    /**
     * 挖矿（工作量证明）
     */
    public void mineBlock() {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!blockHash.substring(0, difficulty).equals(target)) {
            nonce++;
            blockHash = calculateHash();
        }
        System.out.println("区块挖矿成功: " + blockHash);
    }
    
    /**
     * 验证区块完整性
     */
    public boolean isValid() {
        return blockHash.equals(calculateHash());
    }
    
    /**
     * 添加交易到区块
     */
    public void addTransaction(Transaction transaction) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        transactions.add(transaction);
        this.merkleRoot = calculateMerkleRoot();
        this.blockHash = calculateHash();
    }
    
    /**
     * 获取区块数据的JSON表示
     */
    public String toJson() {
        return gson.toJson(this);
    }
    
    /**
     * 从JSON创建区块
     */
    public static Block fromJson(String json) {
        return gson.fromJson(json, Block.class);
    }
    
    @Override
    public String toString() {
        return "Block{" +
                "index=" + index +
                ", blockHash='" + blockHash + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", timestamp=" + timestamp +
                ", transactions=" + (transactions != null ? transactions.size() : 0) +
                ", traceId='" + traceId + '\'' +
                ", productId='" + productId + '\'' +
                ", operationType='" + operationType + '\'' +
                '}';
    }
}
