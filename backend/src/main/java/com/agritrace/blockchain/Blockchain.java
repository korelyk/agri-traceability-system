package com.agritrace.blockchain;

import com.agritrace.entity.Block;
import com.agritrace.entity.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class Blockchain {

    private static final int BLOCK_SIZE = 10;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path stateFile;

    private List<Block> chain;
    private List<Transaction> pendingTransactions;
    private Map<String, Block> blockIndex;
    private Map<String, List<Transaction>> productTrace;

    private int difficulty;
    private int miningReward;

    public Blockchain(@Value("${blockchain.state-file}") String stateFilePath) {
        this.stateFile = Paths.get(stateFilePath);
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.blockIndex = new ConcurrentHashMap<>();
        this.productTrace = new ConcurrentHashMap<>();
        this.difficulty = 4;
        this.miningReward = 100;
    }

    @PostConstruct
    public synchronized void initialize() {
        if (Files.exists(stateFile)) {
            loadState();
        }

        if (chain.isEmpty()) {
            createGenesisBlock();
            persistState();
        } else {
            rebuildIndexes();
        }
    }

    public synchronized Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public synchronized void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if (!transaction.verifySignature(transaction.getFromPublicKey())) {
            throw new IllegalArgumentException("Transaction signature verification failed");
        }

        pendingTransactions.add(transaction);
        appendProductTrace(transaction);
        persistState();

        if (pendingTransactions.size() >= BLOCK_SIZE) {
            minePendingTransactions("system");
        }
    }

    public synchronized Block minePendingTransactions(String minerAddress) {
        if (pendingTransactions.isEmpty()) {
            return null;
        }

        Block newBlock = new Block(
                chain.size(),
                getLatestBlock().getBlockHash(),
                new ArrayList<>(pendingTransactions));
        newBlock.setTimestamp(System.currentTimeMillis() / 1000);
        newBlock.setDifficulty(difficulty);
        newBlock.mineBlock();

        chain.add(newBlock);
        blockIndex.put(newBlock.getBlockHash(), newBlock);
        pendingTransactions.clear();
        persistState();
        return newBlock;
    }

    public synchronized Block createTraceBlock(String productId, String operationType,
            String operatorId, List<Transaction> transactions) {
        Block newBlock = new Block(chain.size(), getLatestBlock().getBlockHash(), transactions);
        newBlock.setTraceId(UUID.randomUUID().toString());
        newBlock.setProductId(productId);
        newBlock.setOperationType(operationType);
        newBlock.setOperatorId(operatorId);
        newBlock.setTimestamp(System.currentTimeMillis() / 1000);
        newBlock.setDifficulty(difficulty);
        newBlock.mineBlock();

        chain.add(newBlock);
        blockIndex.put(newBlock.getBlockHash(), newBlock);
        persistState();
        return newBlock;
    }

    public synchronized boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            if (!currentBlock.isValid()) {
                return false;
            }
            if (!currentBlock.getPreviousHash().equals(previousBlock.getBlockHash())) {
                return false;
            }
            String calculatedMerkleRoot = currentBlock.calculateMerkleRoot();
            if (!calculatedMerkleRoot.equals(currentBlock.getMerkleRoot())) {
                return false;
            }
        }
        return true;
    }

    public synchronized List<Transaction> getProductTraceHistory(String productId) {
        List<Transaction> history = new ArrayList<>();
        for (Block block : chain) {
            if (block.getTransactions() == null) {
                continue;
            }
            for (Transaction tx : block.getTransactions()) {
                if (productId.equals(tx.getProductId())) {
                    history.add(tx);
                }
            }
        }
        history.sort(Comparator.comparingLong(Transaction::getTimestamp));
        return history;
    }

    public synchronized Block getBlockByHash(String hash) {
        return blockIndex.get(hash);
    }

    public synchronized Transaction findTransaction(String transactionId) {
        for (Block block : chain) {
            if (block.getTransactions() == null) {
                continue;
            }
            for (Transaction tx : block.getTransactions()) {
                if (transactionId.equals(tx.getTransactionId())) {
                    return tx;
                }
            }
        }
        return null;
    }

    public synchronized List<Block> getBlocks() {
        return chain.stream()
                .sorted(Comparator.comparingInt(Block::getIndex).reversed())
                .toList();
    }

    public synchronized Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBlocks", chain.size());
        stats.put("totalTransactions", chain.stream()
                .mapToInt(block -> block.getTransactions() != null ? block.getTransactions().size() : 0)
                .sum());
        stats.put("pendingTransactions", pendingTransactions.size());
        stats.put("difficulty", difficulty);
        stats.put("chainValid", isChainValid());
        stats.put("productsTraced", productTrace.size());
        return stats;
    }

    public synchronized Set<String> getAllProductIds() {
        return new HashSet<>(productTrace.keySet());
    }

    public synchronized String exportToJson() {
        return GSON.toJson(buildState());
    }

    public synchronized void printChain() {
        System.out.println("========== Blockchain ==========");
        System.out.println("Blocks: " + chain.size());
        System.out.println("Pending transactions: " + pendingTransactions.size());
        System.out.println("Chain valid: " + isChainValid());
    }

    private void createGenesisBlock() {
        Block genesisBlock = new Block(0, "0", new ArrayList<>());
        genesisBlock.setTraceId("GENESIS");
        genesisBlock.setProductId("GENESIS");
        genesisBlock.setOperationType("GENESIS");
        genesisBlock.setTimestamp(System.currentTimeMillis() / 1000);
        genesisBlock.setDifficulty(difficulty);
        genesisBlock.mineBlock();

        chain.add(genesisBlock);
        blockIndex.put(genesisBlock.getBlockHash(), genesisBlock);
    }

    private void appendProductTrace(Transaction transaction) {
        if (transaction.getProductId() == null) {
            return;
        }
        productTrace.computeIfAbsent(transaction.getProductId(), ignored -> new ArrayList<>()).add(transaction);
    }

    private void rebuildIndexes() {
        blockIndex = new ConcurrentHashMap<>();
        productTrace = new ConcurrentHashMap<>();

        for (Block block : chain) {
            blockIndex.put(block.getBlockHash(), block);
            if (block.getTransactions() == null) {
                continue;
            }
            for (Transaction tx : block.getTransactions()) {
                appendProductTrace(tx);
            }
        }

        for (Transaction tx : pendingTransactions) {
            appendProductTrace(tx);
        }
    }

    private void loadState() {
        try {
            BlockchainState state = GSON.fromJson(Files.readString(stateFile, StandardCharsets.UTF_8), BlockchainState.class);
            if (state == null) {
                return;
            }
            chain = state.chain != null ? new ArrayList<>(state.chain) : new ArrayList<>();
            pendingTransactions = state.pendingTransactions != null ? new ArrayList<>(state.pendingTransactions)
                    : new ArrayList<>();
            if (state.difficulty != null) {
                difficulty = state.difficulty;
            }
            if (state.miningReward != null) {
                miningReward = state.miningReward;
            }
            rebuildIndexes();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load blockchain state", ex);
        }
    }

    private void persistState() {
        try {
            Path parent = stateFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(stateFile, GSON.toJson(buildState()), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to persist blockchain state", ex);
        }
    }

    private BlockchainState buildState() {
        BlockchainState state = new BlockchainState();
        state.chain = chain;
        state.pendingTransactions = pendingTransactions;
        state.difficulty = difficulty;
        state.miningReward = miningReward;
        return state;
    }

    private static class BlockchainState {
        private List<Block> chain;
        private List<Transaction> pendingTransactions;
        private Integer difficulty;
        private Integer miningReward;
    }
}
