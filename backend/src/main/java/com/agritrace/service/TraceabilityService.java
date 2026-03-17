package com.agritrace.service;

import com.agritrace.blockchain.Blockchain;
import com.agritrace.crypto.DigitalSignature;
import com.agritrace.entity.Block;
import com.agritrace.entity.Product;
import com.agritrace.entity.TraceRecord;
import com.agritrace.entity.Transaction;
import com.agritrace.entity.User;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private KeySecurityService keySecurityService;

    @Transactional
    public Product registerProduct(String productName, String productCategory,
            String producerId, String origin, String description) {
        User producer = Optional.ofNullable(userMapper.selectById(producerId))
                .orElseThrow(() -> new RuntimeException("生产者不存在"));

        String productId = generateProductId(productCategory);
        Product product = Product.create(
                productId,
                productName,
                productCategory,
                producerId,
                producer.getRealName(),
                origin);
        product.setDescription(description);
        product.setBatchNumber(generateBatchNumber());

        TraceRecord record = TraceRecord.create(
                productId,
                "PRODUCE",
                producerId,
                producer.getRealName(),
                "PRODUCER",
                origin,
                "产品生产注册: " + productName);

        Transaction transaction = createTransaction(product, record, producer);
        String signature = digitalSignature.signTransaction(
                transaction,
                keySecurityService.decrypt(producer.getPrivateKey()));
        transaction.setDigitalSignature(signature);
        record.setSignature(signature, producer.getPublicKey());

        blockchain.addTransaction(transaction);
        Block newBlock = blockchain.minePendingTransactions(producerId);
        if (newBlock != null) {
            product.setBlockHash(newBlock.getBlockHash());
            product.setTransactionId(transaction.getTransactionId());
            record.setBlockchainInfo(newBlock.getBlockHash(), transaction.getTransactionId());
        }

        product.setQrCode(generateQRCode(productId));
        productMapper.insert(product);
        traceRecordMapper.insert(record);
        return product;
    }

    @Transactional
    public TraceRecord addTraceRecord(String productId, String operationType,
            String operatorId, String location,
            String operationDetail, String environmentData) {
        Product product = Optional.ofNullable(productMapper.selectById(productId))
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        User operator = Optional.ofNullable(userMapper.selectById(operatorId))
                .orElseThrow(() -> new RuntimeException("操作者不存在"));

        TraceRecord record = TraceRecord.create(
                productId,
                operationType,
                operatorId,
                operator.getRealName(),
                operator.getUserType(),
                location,
                operationDetail);

        if (environmentData != null && !environmentData.isBlank()) {
            parseEnvironmentData(record, environmentData);
        }

        Transaction transaction = createTransaction(product, record, operator);
        String signature = digitalSignature.signTransaction(
                transaction,
                keySecurityService.decrypt(operator.getPrivateKey()));
        transaction.setDigitalSignature(signature);
        record.setSignature(signature, operator.getPublicKey());

        blockchain.addTransaction(transaction);
        Block newBlock = blockchain.minePendingTransactions(operatorId);
        if (newBlock != null) {
            record.setBlockchainInfo(newBlock.getBlockHash(), transaction.getTransactionId());
        }

        product.updateStatus(operationType, operatorId, location);
        traceRecordMapper.insert(record);
        productMapper.updateById(product);
        return record;
    }

    public Map<String, Object> traceProduct(String productId) {
        Product product = Optional.ofNullable(productMapper.selectById(productId))
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        List<TraceRecord> dbRecords = traceRecordMapper.selectList(
                new LambdaQueryWrapper<TraceRecord>()
                        .eq(TraceRecord::getProductId, productId)
                        .orderByAsc(TraceRecord::getOperationTime));

        List<Transaction> blockchainRecords = blockchain.getProductTraceHistory(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("traceHistory", dbRecords);
        result.put("blockchainRecords", blockchainRecords);
        result.put("dataConsistent", verifyDataConsistency(dbRecords, blockchainRecords));
        result.put("blockchainValid", blockchain.isChainValid());

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRecords", dbRecords.size());
        statistics.put("verifiedRecords", dbRecords.stream().filter(TraceRecord::isVerified).count());
        statistics.put("operationTypes", dbRecords.stream()
                .map(TraceRecord::getOperationType)
                .distinct()
                .collect(Collectors.toList()));
        result.put("statistics", statistics);
        return result;
    }

    private boolean verifyDataConsistency(List<TraceRecord> dbRecords, List<Transaction> blockchainRecords) {
        if (dbRecords.size() != blockchainRecords.size()) {
            return false;
        }

        Set<String> dbTransactionIds = dbRecords.stream()
                .map(TraceRecord::getTransactionId)
                .collect(Collectors.toSet());
        Set<String> blockchainTransactionIds = blockchainRecords.stream()
                .map(Transaction::getTransactionId)
                .collect(Collectors.toSet());

        if (!dbTransactionIds.equals(blockchainTransactionIds)) {
            return false;
        }

        for (Transaction transaction : blockchainRecords) {
            if (!digitalSignature.verifyTransaction(transaction)) {
                return false;
            }
        }
        return true;
    }

    private Transaction createTransaction(Product product, TraceRecord record, User operator) {
        Transaction transaction = new Transaction();
        transaction.setFromAddress(operator.getBlockchainAddress());
        transaction.setFromPublicKey(operator.getPublicKey());
        transaction.setProductId(product.getProductId());
        transaction.setProductName(product.getProductName());
        transaction.setProductCategory(product.getProductCategory());
        transaction.setBatchNumber(product.getBatchNumber());
        transaction.setOperationType(record.getOperationType());
        transaction.setOperationDetail(record.getOperationDetail());
        transaction.setOperatorId(operator.getUserId());
        transaction.setOperatorName(operator.getRealName());
        transaction.setLocation(record.getLocation());
        transaction.setEnvironmentData(record.getEnvironmentData());
        transaction.setTemperature(record.getTemperature());
        transaction.setHumidity(record.getHumidity());
        transaction.setTransactionHash(transaction.calculateHash());
        return transaction;
    }

    private String generateProductId(String category) {
        String prefix = category == null || category.isBlank()
                ? "PR"
                : category.substring(0, Math.min(2, category.length())).toUpperCase();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + timestamp + random;
    }

    private String generateBatchNumber() {
        return "B" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%06d", new Random().nextInt(1_000_000));
    }

    private String generateQRCode(String productId) {
        String traceUrl = "https://bishe.yyy999.my/#/public-trace?productId=" + productId;

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);

            BitMatrix bitMatrix = qrCodeWriter.encode(traceUrl, BarcodeFormat.QR_CODE, 300, 300, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException("生成二维码失败", ex);
        }
    }

    private void parseEnvironmentData(TraceRecord record, String environmentData) {
        try {
            if (environmentData.contains("temperature")) {
                String temp = environmentData.replaceAll(".*\"temperature\":\\s*([0-9.]+).*", "$1");
                record.setTemperature(Double.parseDouble(temp));
            }
            if (environmentData.contains("humidity")) {
                String humidity = environmentData.replaceAll(".*\"humidity\":\\s*([0-9.]+).*", "$1");
                record.setHumidity(Double.parseDouble(humidity));
            }
        } catch (Exception ignored) {
            // Keep the raw payload even if parsing fails.
        }
        record.setEnvironmentData(environmentData);
    }

    public Map<String, Object> getBlockchainStatistics() {
        return blockchain.getStatistics();
    }

    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productMapper.selectCount(null));
        stats.put("productsByCategory", toLongMap(productMapper.countByCategory(), "category"));
        stats.put("productsByStatus", toLongMap(productMapper.countByStatus(), "status"));
        stats.put("totalTraceRecords", traceRecordMapper.selectCount(null));
        stats.put("recordsByOperationType", toLongMap(traceRecordMapper.countByOperationType(), "operationType"));
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("usersByType", userMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(User::getUserType, Collectors.counting())));
        stats.putAll(blockchain.getStatistics());
        return stats;
    }

    private Map<String, Long> toLongMap(List<Map<String, Object>> source, String keyName) {
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : source) {
            Object key = row.get(keyName);
            Object count = row.get("count");
            if (key != null && count instanceof Number) {
                result.put(String.valueOf(key), ((Number) count).longValue());
            }
        }
        return result;
    }

    public boolean verifyBlock(String blockHash) {
        Block block = blockchain.getBlockByHash(blockHash);
        return block != null && block.isValid();
    }

    public boolean verifyTransaction(String transactionId) {
        Transaction transaction = blockchain.findTransaction(transactionId);
        return transaction != null && digitalSignature.verifyTransaction(transaction);
    }

    public List<Product> getAllProducts() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreatedAt));
    }

    public Product getProductById(String productId) {
        return Optional.ofNullable(productMapper.selectById(productId))
                .orElseThrow(() -> new RuntimeException("产品不存在"));
    }

    public List<Block> getAllBlocks() {
        return blockchain.getBlocks();
    }
}
