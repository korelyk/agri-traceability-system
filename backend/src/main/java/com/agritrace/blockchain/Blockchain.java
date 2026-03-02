package com.agritrace.blockchain;

import com.agritrace.entity.Block;
import com.agritrace.entity.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 区块链核心类
 * 管理整个区块链网络，包括区块的创建、验证和同步
 */
@Component
@Data
public class Blockchain {

    private List<Block> chain; // 主链
    private List<Transaction> pendingTransactions; // 待处理交易池
    private Map<String, Block> blockIndex; // 区块索引
    private Map<String, List<Transaction>> productTrace; // 产品溯源记录

    private int difficulty; // 挖矿难度
    private int miningReward; // 挖矿奖励
    private static final int BLOCK_SIZE = 10; // 每个区块的最大交易数

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.blockIndex = new ConcurrentHashMap<>();
        this.productTrace = new ConcurrentHashMap<>();
        this.difficulty = 4;
        this.miningReward = 100;

        // 创建创世区块
        createGenesisBlock();
    }

    /**
     * 创建创世区块
     */
    private void createGenesisBlock() {
        Block genesisBlock = new Block(0, "0", new ArrayList<>());
        genesisBlock.setTraceId("GENESIS");
        genesisBlock.setProductId("GENESIS");
        genesisBlock.setOperationType("创世区块");
        genesisBlock.setTimestamp(System.currentTimeMillis() / 1000);
        genesisBlock.mineBlock();

        chain.add(genesisBlock);
        blockIndex.put(genesisBlock.getBlockHash(), genesisBlock);

        System.out.println("创世区块创建成功: " + genesisBlock.getBlockHash());
    }

    /**
     * 获取最新区块
     */
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    /**
     * 添加交易到待处理池
     */
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("交易不能为空");
        }

        // 验证交易
        if (!transaction.verifySignature(transaction.getFromPublicKey())) {
            throw new IllegalArgumentException("交易签名验证失败");
        }

        pendingTransactions.add(transaction);

        // 更新产品溯源记录
        String productId = transaction.getProductId();
        if (productId != null) {
            productTrace.computeIfAbsent(productId, k -> new ArrayList<>()).add(transaction);
        }

        // 如果待处理交易达到区块大小，自动挖矿
        if (pendingTransactions.size() >= BLOCK_SIZE) {
            minePendingTransactions("system");
        }
    }

    /**
     * 挖矿 - 将待处理交易打包成区块
     */
    public Block minePendingTransactions(String minerAddress) {
        if (pendingTransactions.isEmpty()) {
            return null;
        }

        // 获取最新区块的哈希作为前一个哈希
        String previousHash = getLatestBlock().getBlockHash();

        // 创建新区块
        Block newBlock = new Block(
                chain.size(),
                previousHash,
                new ArrayList<>(pendingTransactions));

        // 设置区块信息
        newBlock.setTimestamp(System.currentTimeMillis() / 1000);
        newBlock.setDifficulty(difficulty);

        // 挖矿
        newBlock.mineBlock();

        // 添加到链上
        chain.add(newBlock);
        blockIndex.put(newBlock.getBlockHash(), newBlock);

        // 清空待处理交易
        pendingTransactions.clear();

        System.out.println("新区块已挖矿并添加到链上: " + newBlock.getBlockHash());

        return newBlock;
    }

    /**
     * 手动创建区块（用于溯源场景）
     */
    public Block createTraceBlock(String productId, String operationType,
            String operatorId, List<Transaction> transactions) {
        String previousHash = getLatestBlock().getBlockHash();

        Block newBlock = new Block(
                chain.size(),
                previousHash,
                transactions);

        newBlock.setTraceId(UUID.randomUUID().toString());
        newBlock.setProductId(productId);
        newBlock.setOperationType(operationType);
        newBlock.setOperatorId(operatorId);
        newBlock.setTimestamp(System.currentTimeMillis() / 1000);
        newBlock.setDifficulty(difficulty);

        // 挖矿
        newBlock.mineBlock();

        // 添加到链上
        chain.add(newBlock);
        blockIndex.put(newBlock.getBlockHash(), newBlock);

        return newBlock;
    }

    /**
     * 验证区块链完整性
     */
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // 验证当前区块哈希
            if (!currentBlock.isValid()) {
                System.out.println("区块 " + i + " 哈希无效");
                return false;
            }

            // 验证前一个哈希
            if (!currentBlock.getPreviousHash().equals(previousBlock.getBlockHash())) {
                System.out.println("区块 " + i + " 前一个哈希不匹配");
                return false;
            }

            // 验证Merkle根
            String calculatedMerkleRoot = currentBlock.calculateMerkleRoot();
            if (!calculatedMerkleRoot.equals(currentBlock.getMerkleRoot())) {
                System.out.println("区块 " + i + " Merkle根无效");
                return false;
            }
        }

        return true;
    }

    /**
     * 获取产品的完整溯源记录
     */
    public List<Transaction> getProductTraceHistory(String productId) {
        List<Transaction> history = new ArrayList<>();

        // 遍历所有区块查找相关交易
        for (Block block : chain) {
            if (block.getTransactions() != null) {
                for (Transaction tx : block.getTransactions()) {
                    if (productId.equals(tx.getProductId())) {
                        history.add(tx);
                    }
                }
            }
        }

        // 按时间戳排序
        history.sort(Comparator.comparingLong(Transaction::getTimestamp));

        return history;
    }

    /**
     * 根据区块哈希获取区块
     */
    public Block getBlockByHash(String hash) {
        return blockIndex.get(hash);
    }

    /**
     * 根据交易ID查找交易
     */
    public Transaction findTransaction(String transactionId) {
        for (Block block : chain) {
            if (block.getTransactions() != null) {
                for (Transaction tx : block.getTransactions()) {
                    if (transactionId.equals(tx.getTransactionId())) {
                        return tx;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取区块链统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalBlocks", chain.size());
        stats.put("totalTransactions", chain.stream()
                .mapToInt(b -> b.getTransactions() != null ? b.getTransactions().size() : 0)
                .sum());
        stats.put("pendingTransactions", pendingTransactions.size());
        stats.put("difficulty", difficulty);
        stats.put("chainValid", isChainValid());
        stats.put("productsTraced", productTrace.size());

        return stats;
    }

    /**
     * 获取所有产品ID列表
     */
    public Set<String> getAllProductIds() {
        Set<String> productIds = new HashSet<>();

        for (Block block : chain) {
            if (block.getTransactions() != null) {
                for (Transaction tx : block.getTransactions()) {
                    if (tx.getProductId() != null) {
                        productIds.add(tx.getProductId());
                    }
                }
            }
        }

        return productIds;
    }

    /**
     * 导出区块链为JSON
     */
    public String exportToJson() {
        return gson.toJson(this);
    }

    /**
     * 从JSON导入区块链
     */
    public static Blockchain importFromJson(String json) {
        return gson.fromJson(json, Blockchain.class);
    }

    /**
     * 打印区块链信息
     */
    public void printChain() {
        System.out.println("========== 区块链信息 ==========");
        System.out.println("区块数量: " + chain.size());
        System.out.println("待处理交易: " + pendingTransactions.size());
        System.out.println("链有效性: " + isChainValid());
        System.out.println("\n区块详情:");

        for (Block block : chain) {
            System.out.println("\n区块 #" + block.getIndex());
            System.out.println(
                    "  哈希: " + (block.getBlockHash().length() > 20 ? block.getBlockHash().substring(0, 20) + "..."
                            : block.getBlockHash()));
            System.out.println("  前一哈希: "
                    + (block.getPreviousHash().length() > 20 ? block.getPreviousHash().substring(0, 20) + "..."
                            : block.getPreviousHash()));
            System.out.println("  时间戳: " + block.getTimestamp());
            System.out.println("  交易数: " + (block.getTransactions() != null ? block.getTransactions().size() : 0));
            System.out.println("  产品ID: " + block.getProductId());
            System.out.println("  操作类型: " + block.getOperationType());
        }

        System.out.println("\n==============================");
    }
}
