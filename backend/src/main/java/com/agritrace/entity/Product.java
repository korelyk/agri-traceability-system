package com.agritrace.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 农产品实体类
 * 存储产品的基本信息和溯源记录
 */
@Data
@TableName( "products")
public class Product {
    
        @TableId(value = "product_id", type = IdType.INPUT)
    private String productId;           // 产品唯一标识
    
    @TableField("product_name")
    private String productName;         // 产品名称
    
    @TableField("product_category")
    private String productCategory;     // 产品类别
    
    @TableField("batch_number")
    private String batchNumber;         // 批次号
    
    @TableField("origin")
    private String origin;              // 产地
    
    @TableField("producer_id")
    private String producerId;          // 生产者ID
    
    @TableField("producer_name")
    private String producerName;        // 生产者名称
    
    @TableField("production_date")
    private LocalDateTime productionDate;  // 生产日期
    
    @TableField("expiry_date")
    private LocalDateTime expiryDate;   // 保质期
    
    @TableField("quality_grade")
    private String qualityGrade;        // 质量等级
    
    @TableField("certification")
    private String certification;       // 认证信息（有机/绿色/无公害）
    
    @TableField("description")
    private String description;         // 产品描述
    
    @TableField("qr_code")
    private String qrCode;              // 二维码数据
    
    @TableField("current_status")
    private String currentStatus;       // 当前状态
    
    @TableField("current_holder")
    private String currentHolder;       // 当前持有者
    
    @TableField("current_location")
    private String currentLocation;     // 当前位置
    
    @TableField("block_hash")
    private String blockHash;           // 关联的区块哈希
    
    @TableField("transaction_id")
    private String transactionId;       // 关联的交易ID
    
    @TableField("created_at")
    private LocalDateTime createdAt;    // 创建时间
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;    // 更新时间
    
    @TableField(exist = false)
    private List<TraceRecord> traceHistory;  // 溯源历史记录
    
    public Product() {
        this.traceHistory = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 创建新产品
     */
    public static Product create(String productId, String productName, 
                                  String productCategory, String producerId,
                                  String producerName, String origin) {
        Product product = new Product();
        product.setProductId(productId);
        product.setProductName(productName);
        product.setProductCategory(productCategory);
        product.setProducerId(producerId);
        product.setProducerName(producerName);
        product.setOrigin(origin);
        product.setProductionDate(LocalDateTime.now());
        product.setCurrentStatus("CREATED");
        product.setCurrentHolder(producerId);
        product.setCurrentLocation(origin);
        return product;
    }
    
    /**
     * 更新产品状态
     */
    public void updateStatus(String status, String holder, String location) {
        this.currentStatus = status;
        this.currentHolder = holder;
        this.currentLocation = location;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 添加溯源记录
     */
    public void addTraceRecord(TraceRecord record) {
        if (this.traceHistory == null) {
            this.traceHistory = new ArrayList<>();
        }
        this.traceHistory.add(record);
    }
    
    /**
     * 获取产品信息摘要
     */
    public String getSummary() {
        return String.format("Product[%s]: %s (%s) - %s - %s",
                productId, productName, productCategory, 
                producerName, currentStatus);
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", category='" + productCategory + '\'' +
                ", producer='" + producerName + '\'' +
                ", origin='" + origin + '\'' +
                ", status='" + currentStatus + '\'' +
                '}';
    }
}
