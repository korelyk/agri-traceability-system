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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TraceabilityService {

    private static final String DEFAULT_PUBLIC_BASE_URL = "https://bishe.yyy999.my";

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

    @Value("${app.public-base-url:" + DEFAULT_PUBLIC_BASE_URL + "}")
    private String publicBaseUrl;

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
        String traceUrl = buildPublicTraceUrl(productId);

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

    private String buildPublicTraceUrl(String productId) {
        String baseUrl = publicBaseUrl;
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_PUBLIC_BASE_URL;
        }
        baseUrl = baseUrl.trim().replaceAll("/+$", "");
        return baseUrl + "/public-trace/" + productId;
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

    public Map<String, Object> getTransactionVerificationDetail(String transactionId) {
/*
        Transaction transaction = Optional.ofNullable(blockchain.findTransaction(transactionId))
                .orElseThrow(() -> new RuntimeException("浜ゆ槗涓嶅瓨鍦?));
*/
        Transaction transaction = Optional.ofNullable(blockchain.findTransaction(transactionId))
                .orElseThrow(() -> new RuntimeException("交易不存在"));
        Block block = blockchain.findBlockByTransactionId(transactionId);
        TraceRecord traceRecord = traceRecordMapper.selectOne(
                new LambdaQueryWrapper<TraceRecord>()
                        .eq(TraceRecord::getTransactionId, transactionId)
                        .last("LIMIT 1"));
        Product product = transaction.getProductId() == null
                ? null
                : productMapper.selectById(transaction.getProductId());
        User operator = transaction.getOperatorId() == null
                ? null
                : userMapper.selectById(transaction.getOperatorId());

        boolean signatureValid = digitalSignature.verifyTransaction(transaction);
        boolean transactionHashValid = Objects.equals(transaction.getTransactionHash(), transaction.calculateHash());
        boolean blockValid = block != null && block.isValid();
        boolean chainValid = blockchain.isChainValid();
        boolean recordFound = traceRecord != null;
        boolean dbConsistent = traceRecord != null && isTransactionConsistentWithRecord(transaction, traceRecord);

        Map<String, Object> payloadFields = new LinkedHashMap<>();
        payloadFields.put("transactionId", transaction.getTransactionId());
        payloadFields.put("timestamp", transaction.getTimestamp());
        payloadFields.put("productId", transaction.getProductId());
        payloadFields.put("productName", transaction.getProductName());
        payloadFields.put("operationType", transaction.getOperationType());
        payloadFields.put("operatorId", transaction.getOperatorId());
        payloadFields.put("location", transaction.getLocation());
        payloadFields.put("operationDetail", transaction.getOperationDetail());

        Map<String, Object> verification = new LinkedHashMap<>();
        verification.put("signatureValid", signatureValid);
        verification.put("transactionHashValid", transactionHashValid);
        verification.put("blockValid", blockValid);
        verification.put("chainValid", chainValid);
        verification.put("recordFound", recordFound);
        verification.put("dbConsistent", dbConsistent);
        verification.put("overallPassed", signatureValid && transactionHashValid && blockValid && chainValid);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transaction", transaction);
        result.put("payloadFields", payloadFields);
        result.put("payloadText", digitalSignature.getTransactionPayload(transaction));
        result.put("verification", verification);
        result.put("traceRecord", traceRecord);
        result.put("product", product);
        result.put("operator", operator);
        result.put("block", buildBlockSummary(block));
        result.put("signature", buildSignatureSummary(transaction));
        return result;
    }

    public List<Product> getAllProducts() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreatedAt));
    }

    public Product getProductById(String productId) {
        return Optional.ofNullable(productMapper.selectById(productId))
                .orElseThrow(() -> new RuntimeException("产品不存在"));
    }

    @Transactional
    public Map<String, Object> rebuildBlockchainFromDatabase() {
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .orderByAsc(Product::getCreatedAt)
                .orderByAsc(Product::getProductId));
        List<TraceRecord> traceRecords = traceRecordMapper.selectList(new LambdaQueryWrapper<TraceRecord>()
                .orderByAsc(TraceRecord::getOperationTime)
                .orderByAsc(TraceRecord::getCreatedAt)
                .orderByAsc(TraceRecord::getRecordId));

        Map<String, Product> productsById = products.stream()
                .filter(product -> product.getProductId() != null)
                .collect(Collectors.toMap(
                        Product::getProductId,
                        product -> product,
                        (left, right) -> left));

        Set<String> operatorIds = traceRecords.stream()
                .map(TraceRecord::getOperatorId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        Map<String, User> usersById = operatorIds.isEmpty()
                ? new HashMap<>()
                : userMapper.selectBatchIds(operatorIds).stream()
                        .filter(user -> user.getUserId() != null)
                        .collect(Collectors.toMap(
                                User::getUserId,
                                user -> user,
                                (left, right) -> left));

        int difficulty = blockchain.getDifficulty();
        List<Block> rebuiltChain = new ArrayList<>();
        rebuiltChain.add(createGenesisBlock(difficulty));

        Map<String, String> firstTransactionIdByProduct = new HashMap<>();
        Map<String, String> firstBlockHashByProduct = new HashMap<>();
        Map<String, String> lastHolderByProduct = new HashMap<>();
        Map<String, String> lastLocationByProduct = new HashMap<>();
        Map<String, String> lastStatusByProduct = new HashMap<>();

        int repairedRecords = 0;
        int generatedTransactionIds = 0;

        for (TraceRecord record : traceRecords) {
            Product product = Optional.ofNullable(productsById.get(record.getProductId()))
                    .orElseThrow(() -> new RuntimeException("鍖哄潡閲嶅缓澶辫触: 浜у搧涓嶅瓨鍦? -> " + record.getProductId()));
            User operator = Optional.ofNullable(usersById.get(record.getOperatorId()))
                    .orElseThrow(() -> new RuntimeException("鍖哄潡閲嶅缓澶辫触: 鎿嶄綔鑰呬笉瀛樺湪 -> " + record.getOperatorId()));

            if (record.getTransactionId() == null || record.getTransactionId().isBlank()) {
                record.setTransactionId(UUID.randomUUID().toString());
                generatedTransactionIds++;
            }

            Transaction transaction = buildRebuiltTransaction(product, record, operator);
            Block block = createTraceBlock(rebuiltChain.size(), rebuiltChain.get(rebuiltChain.size() - 1), transaction, record, difficulty);
            rebuiltChain.add(block);

            record.setBlockHash(block.getBlockHash());
            record.setSignature(transaction.getDigitalSignature(), transaction.getFromPublicKey());
            record.setVerified(digitalSignature.verifyTransaction(transaction));
            traceRecordMapper.updateById(record);

            firstTransactionIdByProduct.putIfAbsent(product.getProductId(), transaction.getTransactionId());
            firstBlockHashByProduct.putIfAbsent(product.getProductId(), block.getBlockHash());
            lastHolderByProduct.put(product.getProductId(), record.getOperatorId());
            lastLocationByProduct.put(product.getProductId(), record.getLocation());
            lastStatusByProduct.put(product.getProductId(), record.getOperationType());
            repairedRecords++;
        }

        for (Product product : products) {
            String firstTransactionId = firstTransactionIdByProduct.get(product.getProductId());
            String firstBlockHash = firstBlockHashByProduct.get(product.getProductId());
            if (firstTransactionId != null) {
                product.setTransactionId(firstTransactionId);
            }
            if (firstBlockHash != null) {
                product.setBlockHash(firstBlockHash);
            }

            String currentHolder = lastHolderByProduct.get(product.getProductId());
            String currentLocation = lastLocationByProduct.get(product.getProductId());
            String currentStatus = lastStatusByProduct.get(product.getProductId());
            if (currentHolder != null) {
                product.setCurrentHolder(currentHolder);
            }
            if (currentLocation != null) {
                product.setCurrentLocation(currentLocation);
            }
            if (currentStatus != null) {
                product.setCurrentStatus(currentStatus);
            }
            productMapper.updateById(product);
        }

        blockchain.replaceChainState(rebuiltChain);

        Map<String, Object> result = new HashMap<>();
        result.put("totalProducts", products.size());
        result.put("totalTraceRecords", traceRecords.size());
        result.put("repairedRecords", repairedRecords);
        result.put("generatedTransactionIds", generatedTransactionIds);
        result.put("totalBlocks", rebuiltChain.size());
        result.put("totalTransactions", Math.max(0, rebuiltChain.size() - 1));
        result.put("chainValid", blockchain.isChainValid());
        return result;
    }

    public List<Block> getAllBlocks() {
        return enrichBlocksForDisplay(blockchain.getBlocks());
    }

    private Transaction buildRebuiltTransaction(Product product, TraceRecord record, User operator) {
        Transaction transaction = new Transaction();
        String publicKey = firstNonBlank(operator.getPublicKey(), record.getSignerPublicKey());
        String blockchainAddress = operator.getBlockchainAddress();
        if ((blockchainAddress == null || blockchainAddress.isBlank()) && publicKey != null && !publicKey.isBlank()) {
            blockchainAddress = digitalSignature.generateAddress(publicKey);
        }

        transaction.setTransactionId(record.getTransactionId());
        transaction.setTimestamp(toEpochSecond(record.getOperationTime(), record.getCreatedAt(), product.getCreatedAt()));
        transaction.setFromAddress(blockchainAddress);
        transaction.setFromPublicKey(publicKey);
        transaction.setProductId(product.getProductId());
        transaction.setProductName(product.getProductName());
        transaction.setProductCategory(product.getProductCategory());
        transaction.setBatchNumber(product.getBatchNumber());
        transaction.setOperationType(record.getOperationType());
        transaction.setOperationDetail(record.getOperationDetail());
        transaction.setOperatorId(record.getOperatorId());
        transaction.setOperatorName(firstNonBlank(record.getOperatorName(), operator.getRealName(), operator.getUsername()));
        transaction.setLocation(record.getLocation());
        transaction.setLocationCode(record.getLocationCode());
        transaction.setEnvironmentData(record.getEnvironmentData());
        transaction.setTemperature(record.getTemperature());
        transaction.setHumidity(record.getHumidity());
        transaction.setQualityGrade(product.getQualityGrade());
        transaction.setInspectionResult(record.getQualityCheckResult());
        transaction.setCertificateNo(record.getCertificateNo());
        transaction.setDocumentHash(record.getDocumentHash());
        transaction.setTransactionHash(transaction.calculateHash());

        if (publicKey == null || publicKey.isBlank()) {
            throw new RuntimeException("鍖哄潡閲嶅缓澶辫触: 鎿嶄綔鑰呯己灏戝叕閽? -> " + operator.getUserId());
        }

        String privateKey = keySecurityService.decrypt(operator.getPrivateKey());
        if (privateKey == null || privateKey.isBlank()) {
            throw new RuntimeException("鍖哄潡閲嶅缓澶辫触: 鎿嶄綔鑰呯己灏戠閽? -> " + operator.getUserId());
        }
        transaction.setDigitalSignature(digitalSignature.signTransaction(transaction, privateKey));
        return transaction;
    }

    private Block createGenesisBlock(int difficulty) {
        Block genesisBlock = new Block(0, "0", new ArrayList<>());
        genesisBlock.setTraceId("GENESIS");
        genesisBlock.setProductId("GENESIS");
        genesisBlock.setOperationType("GENESIS");
        genesisBlock.setTimestamp(System.currentTimeMillis() / 1000);
        genesisBlock.setDifficulty(difficulty);
        genesisBlock.setMerkleRoot(genesisBlock.calculateMerkleRoot());
        genesisBlock.setBlockHash(genesisBlock.calculateHash());
        genesisBlock.mineBlock();
        return genesisBlock;
    }

    private Block createTraceBlock(int index, Block previousBlock, Transaction transaction, TraceRecord record, int difficulty) {
        Block block = new Block(index, previousBlock.getBlockHash(), List.of(transaction));
        block.setTraceId(record.getRecordId());
        block.setProductId(record.getProductId());
        block.setOperationType(record.getOperationType());
        block.setOperatorId(record.getOperatorId());
        block.setTimestamp(transaction.getTimestamp());
        block.setDifficulty(difficulty);
        block.setMerkleRoot(block.calculateMerkleRoot());
        block.setBlockHash(block.calculateHash());
        block.mineBlock();
        return block;
    }

    private long toEpochSecond(LocalDateTime... candidates) {
        for (LocalDateTime candidate : candidates) {
            if (candidate != null) {
                return candidate.atZone(ZoneId.systemDefault()).toEpochSecond();
            }
        }
        return System.currentTimeMillis() / 1000;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Map<String, Object> buildBlockSummary(Block block) {
        if (block == null) {
            return null;
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("index", block.getIndex());
        summary.put("blockHash", block.getBlockHash());
        summary.put("previousHash", block.getPreviousHash());
        summary.put("merkleRoot", block.getMerkleRoot());
        summary.put("timestamp", block.getTimestamp());
        summary.put("difficulty", block.getDifficulty());
        summary.put("nonce", block.getNonce());
        summary.put("valid", block.isValid());
        return summary;
    }

    private Map<String, Object> buildSignatureSummary(Transaction transaction) {
        Map<String, Object> signature = new LinkedHashMap<>();
        String publicKey = transaction.getFromPublicKey();
        signature.put("algorithm", "RSA / SHA256withRSA");
        signature.put("digitalSignature", transaction.getDigitalSignature());
        signature.put("publicKey", publicKey);
        signature.put("publicKeyFingerprint", publicKey == null || publicKey.isBlank()
                ? null
                : digitalSignature.calculateHash(publicKey).substring(0, 24));
        signature.put("blockchainAddress", transaction.getFromAddress());
        return signature;
    }

    private boolean isTransactionConsistentWithRecord(Transaction transaction, TraceRecord record) {
        return sameText(transaction.getTransactionId(), record.getTransactionId())
                && sameText(transaction.getProductId(), record.getProductId())
                && sameText(transaction.getOperationType(), record.getOperationType())
                && sameText(transaction.getOperatorId(), record.getOperatorId())
                && sameText(transaction.getLocation(), record.getLocation())
                && sameText(transaction.getOperationDetail(), record.getOperationDetail())
                && sameText(transaction.getDigitalSignature(), record.getDigitalSignature());
    }

    private boolean sameText(String left, String right) {
        return Objects.equals(left, right);
    }

    private List<Block> enrichBlocksForDisplay(List<Block> sourceBlocks) {
        List<Block> blocks = sourceBlocks.stream()
                .map(block -> Block.fromJson(block.toJson()))
                .collect(Collectors.toList());

        Set<String> transactionIds = blocks.stream()
                .flatMap(block -> block.getTransactions() == null ? java.util.stream.Stream.<Transaction>empty() : block.getTransactions().stream())
                .map(Transaction::getTransactionId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Set<String> operatorIds = blocks.stream()
                .flatMap(block -> block.getTransactions() == null ? java.util.stream.Stream.<Transaction>empty() : block.getTransactions().stream())
                .map(Transaction::getOperatorId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Set<String> productIds = blocks.stream()
                .flatMap(block -> block.getTransactions() == null ? java.util.stream.Stream.<Transaction>empty() : block.getTransactions().stream())
                .map(Transaction::getProductId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Map<String, TraceRecord> traceRecordsByTransactionId = new HashMap<>();
        if (!transactionIds.isEmpty()) {
            traceRecordsByTransactionId = traceRecordMapper.selectList(
                    new LambdaQueryWrapper<TraceRecord>()
                            .in(TraceRecord::getTransactionId, transactionIds))
                    .stream()
                    .filter(record -> record.getTransactionId() != null)
                    .collect(Collectors.toMap(
                            TraceRecord::getTransactionId,
                            record -> record,
                            (left, right) -> left));
        }

        Map<String, String> operatorNamesByUserId = new HashMap<>();
        if (!operatorIds.isEmpty()) {
            operatorNamesByUserId = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .in(User::getUserId, operatorIds))
                    .stream()
                    .filter(user -> user.getUserId() != null)
                    .collect(Collectors.toMap(
                            User::getUserId,
                            user -> hasDisplayText(user.getRealName()) ? user.getRealName() : user.getUsername(),
                            (left, right) -> left));
        }

        Map<String, Product> productsById = new HashMap<>();
        if (!productIds.isEmpty()) {
            productsById = productMapper.selectBatchIds(productIds).stream()
                    .filter(product -> product.getProductId() != null)
                    .collect(Collectors.toMap(
                            Product::getProductId,
                            product -> product,
                            (left, right) -> left));
        }

        for (Block block : blocks) {
            if (block.getTransactions() == null) {
                continue;
            }
            for (Transaction transaction : block.getTransactions()) {
                TraceRecord traceRecord = traceRecordsByTransactionId.get(transaction.getTransactionId());
                if (traceRecord != null) {
                    if (hasDisplayText(traceRecord.getOperatorName())) {
                        transaction.setOperatorName(traceRecord.getOperatorName());
                    }
                    if (hasDisplayText(traceRecord.getLocation())) {
                        transaction.setLocation(traceRecord.getLocation());
                    }
                    if (hasDisplayText(traceRecord.getOperationDetail())) {
                        transaction.setOperationDetail(traceRecord.getOperationDetail());
                    }
                }

                Product product = productsById.get(transaction.getProductId());
                if (product != null) {
                    if (hasDisplayText(product.getProductName())) {
                        transaction.setProductName(product.getProductName());
                    }
                    if (hasDisplayText(product.getProductCategory())) {
                        transaction.setProductCategory(product.getProductCategory());
                    }
                }

                if (!hasDisplayText(transaction.getOperatorName())) {
                    String displayName = operatorNamesByUserId.get(transaction.getOperatorId());
                    if (hasDisplayText(displayName)) {
                        transaction.setOperatorName(displayName);
                    }
                }
            }
        }

        return blocks;
    }

    private boolean needsDisplayOperatorName(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != '?') {
                return false;
            }
        }
        return true;
    }

    private boolean hasDisplayText(String value) {
        return value != null && !value.isBlank() && !needsDisplayOperatorName(value);
    }
}
