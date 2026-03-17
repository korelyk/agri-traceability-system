CREATE DATABASE IF NOT EXISTS agritrace DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE agritrace;

CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(64),
    email VARCHAR(128),
    phone VARCHAR(32),
    user_type VARCHAR(32),
    company_name VARCHAR(128),
    company_code VARCHAR(64),
    business_license VARCHAR(64),
    address VARCHAR(255),
    region_code VARCHAR(32),
    public_key TEXT,
    private_key TEXT,
    blockchain_address VARCHAR(128),
    certificate_hash VARCHAR(128),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(32),
    created_at DATETIME,
    last_login DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(64) PRIMARY KEY,
    product_name VARCHAR(128) NOT NULL,
    product_category VARCHAR(64),
    batch_number VARCHAR(64),
    origin VARCHAR(128),
    producer_id VARCHAR(64),
    producer_name VARCHAR(128),
    production_date DATETIME,
    expiry_date DATETIME,
    quality_grade VARCHAR(32),
    certification VARCHAR(64),
    description TEXT,
    qr_code TEXT,
    current_status VARCHAR(64),
    current_holder VARCHAR(64),
    current_location VARCHAR(128),
    block_hash VARCHAR(128),
    transaction_id VARCHAR(128),
    created_at DATETIME,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trace_records (
    record_id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL,
    operation_type VARCHAR(64) NOT NULL,
    operation_detail TEXT,
    operator_id VARCHAR(64) NOT NULL,
    operator_name VARCHAR(128),
    operator_type VARCHAR(64),
    location VARCHAR(128),
    location_code VARCHAR(64),
    operation_time DATETIME,
    temperature DOUBLE,
    humidity DOUBLE,
    environment_data VARCHAR(255),
    quality_check_result VARCHAR(128),
    certificate_no VARCHAR(128),
    document_hash VARCHAR(128),
    digital_signature TEXT,
    signer_public_key TEXT,
    previous_record_id VARCHAR(64),
    transaction_id VARCHAR(128),
    block_hash VARCHAR(128),
    created_at DATETIME,
    verified BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_products_producer_id ON products(producer_id);
CREATE INDEX idx_trace_records_product_id ON trace_records(product_id);
CREATE INDEX idx_trace_records_operation_time ON trace_records(operation_time);
