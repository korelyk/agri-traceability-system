package com.agritrace.service;

import com.agritrace.blockchain.Blockchain;
import com.agritrace.crypto.DigitalSignature;
import com.agritrace.entity.*;
import com.agritrace.mapper.ProductMapper;
import com.agritrace.mapper.TraceRecordMapper;
import com.agritrace.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 溯源服务类
 * 核心业务逻辑：产品注册、溯源记录添加、二维码生成、数据验证
 */
@Service
public class TraceabilityService {

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private DigitalSignature digitalSignature;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private TraceRecordMapper traceRecordMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 注册新产品
     */
    @Transactional
    public Product registerProduct(String productName, String productCategory,
            String producerId, String origin, String description) {
        // 生成产品ID
        String productId = generateProductId(productCategory);

        // 获取生产者信息
        User producer = Optional.ofNullable(userMapper.selectById(producerId))
                .orElseThrow(() -> new RuntimeException("生产者不存在"));

        // 创建产品
        Product product = Product.create(
                productId, productName, productCategory,
                producerId, producer.getRealName(), origin);
        product.setDescription(description);
        product.setBatchNumber(generateBatchNumber());

        // 创建生产溯源记录
        TraceRecord record = TraceRecord.create(
                productId, "PRODUCE", producerId,
                producer.getRealName(), "PRODUCER", origin,
                "产品生产注册: " + productName);

        // 创建区块链交易
        Transaction transaction = createTransaction(product, record, producer);

        // 数字签名
        String signature = digitalSignature.signTransaction(transaction, producer.getPrivateKey());
        transaction.setDigitalSignature(signature);

        record.setSignature(signature, producer.getPublicKey());

        // 添加到区块链
        blockchain.addTransaction(transaction);
        Block newBlock = blockchain.minePendingTransactions(producerId);

        // 更新区块链关联信息
        if (newBlock != null) {
            product.setBlockHash(newBlock.getBlockHash());
            product.setTransactionId(transaction.getTransactionId());
            record.setBlockchainInfo(newBlock.getBlockHash(), transaction.getTransactionId());
        }

        // 生成二维码
        String qrCode = generateQRCode(productId);
        product.setQrCode(qrCode);

        // 保存到数据库
        productMapper.insert(product);
        traceRecordMapper.insert(record);

        return product;
    }

    /**
     * 添加溯源记录
     */
    @Transactional
    public TraceRecord addTraceRecord(String productId, String operationType,
            String operatorId, String location,
            String operationDetail, String environmentData) {
        // 验证产品存在
        Product product = Optional.ofNullable(productMapper.selectById(productId))
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        // 获取操作者信息
        User operator = Optional.ofNullable(userMapper.selectById(operatorId))
                .orElseThrow(() -> new RuntimeException("操作者不存在"));

        // 创建溯源记录
        TraceRecord record = TraceRecord.create(
                productId, operationType, operatorId,
                operator.getRealName(), operator.getUserType(),
                location, operationDetail);

        // 解析环境数据
        if (environmentData != null && !environmentData.isEmpty()) {
            parseEnvironmentData(record, environmentData);
        }

        // 创建区块链交易
        Transaction transaction = createTransaction(product, record, operator);

        // 数字签名
        String signature = digitalSignature.signTransaction(transaction, operator.getPrivateKey());
        transaction.setDigitalSignature(signature);
        record.setSignature(signature, operator.getPublicKey());

        // 添加到区块链
        blockchain.addTransaction(transaction);
        Block newBlock = blockchain.minePendingTransactions(operatorId);

        // 更新区块链关联信息
        if (newBlock != null) {
            record.setBlockchainInfo(newBlock.getBlockHash(), transaction.getTransactionId());
        }

        // 更新产品状态
        product.updateStatus(operationType, operatorId, location);

        // 保存
        traceRecordMapper.insert(record);
        productMapper.updateById(product);

        return record;
    }

    /**
     * 查询产品完整溯源信息
     */
    public Map<String, Object> traceProduct(String productId) {
        Map<String, Object> result = new HashMap<>();

        // 获取产品信息
        Product product = Optional.ofNullable(productMapper.selectById(productId))
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        // 获取数据库中的溯源记录
        List<TraceRecord> dbRecords = traceRecordMapper.selectList(
                new LambdaQueryWrapper<TraceRecord>()
                        .eq(TraceRecord::getProductId, productId)
                        .orderByAsc(TraceRecord::getOperationTime));

        // 获取区块链中的溯源记录
        List<Transaction> blockchainRecords = blockchain.getProductTraceHistory(productId);

        // 验证数据一致性
        boolean isConsistent = verifyDataConsistency(dbRecords, blockchainRecords);

        // 构建结果
        result.put("product", product);
        result.put("traceHistory", dbRecords);
        result.put("blockchainRecords", blockchainRecords);
        result.put("dataConsistent", isConsistent);
        result.put("blockchainValid", blockchain.isChainValid());

        // 统计信息
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRecords", dbRecords.size());
        statistics.put("verifiedRecords", dbRecords.stream().filter(TraceRecord::isVerified).count());
        statistics.put("operationTypes", dbRecords.stream()
                .map(TraceRecord::getOperationType)
                .distinct()
                .toList());
        result.put("statistics", statistics);

        return result;
    }

    /**
     * 验证数据一致性
     */
    private boolean verifyDataConsistency(List<TraceRecord> dbRecords, List<Transaction> blockchainRecords) {
        if (dbRecords.size() != blockchainRecords.size()) {
            return false;
        }

        for (int i = 0; i < dbRecords.size(); i++) {
            TraceRecord record = dbRecords.get(i);
            Transaction tx = blockchainRecords.get(i);

            // 验证交易ID匹配
            if (!record.getTransactionId().equals(tx.getTransactionId())) {
                return false;
            }

            // 验证数字签名
            if (!digitalSignature.verifyTransaction(tx)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 创建区块链交易
     */
    private Transaction createTransaction(Product product, TraceRecord record, User operator) {
        Transaction transaction = new Transaction();
        transaction.setFromAddress(operator.getBlockchainAddress());
        transaction.setProductId(product.getProductId());
        transaction.setProductName(product.getProductName());
        transaction.setOperationType(record.getOperationType());
        transaction.setOperationDetail(record.getOperationDetail());
        transaction.setOperatorId(operator.getUserId());
        transaction.setOperatorName(operator.getRealName());
        transaction.setLocation(record.getLocation());
        transaction.setFromPublicKey(operator.getPublicKey());
        transaction.setTransactionHash(transaction.calculateHash());

        return transaction;
    }

    /**
     * 生成产品ID
     */
    private String generateProductId(String category) {
        String prefix = category != null ? category.substring(0, Math.min(2, category.length())).toUpperCase() : "PR";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + timestamp + random;
    }

    /**
     * 生成批次号
     */
    private String generateBatchNumber() {
        return "B" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                String.format("%06d", new Random().nextInt(1000000));
    }

    /**
     * 生成二维码
     */
    private String generateQRCode(String productId) {
        // 生成溯源URL
        String traceUrl = "https://agritrace.com/trace/" + productId;

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    traceUrl, BarcodeFormat.QR_CODE, 300, 300, hints);

            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // 转换为Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败", e);
        }
    }

    /**
     * 解析环境数据
     */
    private void parseEnvironmentData(TraceRecord record, String environmentData) {
        try {
            // 简单解析JSON格式环境数据
            if (environmentData.contains("temperature")) {
                String tempStr = environmentData.replaceAll(".*\"temperature\":\\s*([0-9.]+).*", "$1");
                record.setTemperature(Double.parseDouble(tempStr));
            }
            if (environmentData.contains("humidity")) {
                String humStr = environmentData.replaceAll(".*\"humidity\":\\s*([0-9.]+).*", "$1");
                record.setHumidity(Double.parseDouble(humStr));
            }
            record.setEnvironmentData(environmentData);
        } catch (Exception e) {
            // 解析失败不影响主流程
            record.setEnvironmentData(environmentData);
        }
    }

    /**
     * 获取区块链统计信息
     */
    public Map<String, Object> getBlockchainStatistics() {
        return blockchain.getStatistics();
    }

    /**
     * 获取系统统计信息
     */
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 产品统计
        stats.put("totalProducts", productMapper.selectCount(null));

        List<Map<String, Object>> catCounts = productMapper.countByCategory();
        Map<String, Long> categoryMap = new HashMap<>();
        for (Map<String, Object> map : catCounts) {
            categoryMap.put((String) map.get("category"), ((Number) map.get("count")).longValue());
        }
        stats.put("productsByCategory", categoryMap);

        // 状态统计
        List<Map<String, Object>> statCounts = productMapper.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Map<String, Object> map : statCounts) {
            statusMap.put((String) map.get("status"), ((Number) map.get("count")).longValue());
        }
        stats.put("productsByStatus", statusMap);

        // 溯源记录统计
        stats.put("totalTraceRecords", traceRecordMapper.selectCount(null));

        List<Map<String, Object>> opCounts = traceRecordMapper.countByOperationType();
        Map<String, Long> opMap = new HashMap<>();
        for (Map<String, Object> map : opCounts) {
            opMap.put((String) map.get("operationType"), ((Number) map.get("count")).longValue());
        }
        stats.put("recordsByOperationType", opMap);

        // 用户统计
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("usersByType", userMapper.selectList(null).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        User::getUserType, java.util.stream.Collectors.counting())));

        // 区块链统计
        stats.putAll(blockchain.getStatistics());

        return stats;
    }

    /**
     * 验证区块
     */
    public boolean verifyBlock(String blockHash) {
        Block block = blockchain.getBlockByHash(blockHash);
        if (block == null) {
            return false;
        }
        return block.isValid();
    }

    /**
     * 验证交易
     */
    public boolean verifyTransaction(String transactionId) {
        Transaction tx = blockchain.findTransaction(transactionId);
        if (tx == null) {
            return false;
        }
        return digitalSignature.verifyTransaction(tx);
    }

    public List<Product> getAllProducts() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreatedAt));
    }
}
