# 农产品防伪溯源系统

## 基于区块链与数字签名的农产品防伪溯源系统设计与实现

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-11-orange.svg)](https://openjdk.org/)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-green.svg)](https://vuejs.org/)
[![SpringBoot](https://img.shields.io/badge/SpringBoot-2.7-brightgreen.svg)](https://spring.io/projects/spring-boot)

---

## 项目简介

本项目是一套基于**区块链技术**和**数字签名技术**的农产品防伪溯源系统，旨在解决传统农产品溯源系统中存在的数据易篡改、信息不透明、防伪能力不足等问题。

### 核心特性

- 🔗 **区块链存储**：采用轻量级区块链确保数据不可篡改
- 🔐 **数字签名**：使用RSA算法确保数据完整性和不可抵赖性
- 📱 **二维码溯源**：每个产品都有唯一二维码，扫码即可查询
- 🔍 **全程追溯**：记录农产品从生产到销售的全生命周期
- 🛡️ **防伪验证**：区块链验证确保溯源信息真实可信

---

## 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端层 (Vue.js 3)                     │
│   登录/注册 │ 产品管理 │ 溯源查询 │ 区块链浏览器              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     后端层 (SpringBoot 2.7)                  │
│   用户服务 │ 产品服务 │ 溯源服务 │ 区块链服务                  │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
┌─────────────────────────┐       ┌─────────────────────────────┐
│      数据层 (H2)         │       │      区块链层 (Java)         │
│  用户 │ 产品 │ 溯源记录   │       │  区块 │ 交易 │ Merkle树    │
└─────────────────────────┘       └─────────────────────────────┘
```

### 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue.js 3, Element Plus, ECharts |
| 后端 | SpringBoot 2.7, JPA, Maven |
| 数据库 | H2 Database |
| 区块链 | Java轻量级实现 (SHA-256, PoW) |
| 密码学 | RSA-2048, SHA-256 |

---

## 项目结构

```
agri-traceability-system/
├── backend/                    # 后端项目
│   ├── src/main/java/com/agritrace/
│   │   ├── blockchain/         # 区块链核心
│   │   ├── controller/         # API控制器
│   │   ├── crypto/             # 数字签名
│   │   ├── entity/             # 实体类
│   │   ├── repository/         # 数据访问
│   │   └── service/            # 业务逻辑
│   └── pom.xml
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── views/              # 页面视图
│   │   ├── router/             # 路由配置
│   │   └── store/              # 状态管理
│   └── package.json
├── documentation/              # 文档
│   ├── 论文-基于区块链与数字签名的农产品防伪溯源系统设计与实现.md
│   ├── 项目复现文档.md
│   └── 项目完整思路.md
└── presentation/               # 汇报PPT
    └── 汇报PPT-基于区块链与数字签名的农产品防伪溯源系统.md
```

---

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.8+
- Node.js 16+
- IntelliJ IDEA / VS Code

### 后端启动

```bash
cd backend

# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 访问
# API: http://localhost:8080/api
# H2控制台: http://localhost:8080/h2-console
```

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run serve

# 访问
# http://localhost:3000
```

---

## 核心功能

### 1. 用户管理

- 用户注册/登录
- RSA密钥对自动生成
- 角色权限管理

### 2. 产品管理

- 产品注册
- 二维码生成
- 产品列表查询

### 3. 溯源记录

- 添加溯源记录
- 数字签名
- 区块链上链

### 4. 溯源查询

- 产品ID查询
- 二维码扫描查询
- 完整溯源时间线

### 5. 区块链浏览器

- 区块列表
- 交易详情
- 链验证

---

## 核心算法

### 区块哈希计算

```java
public String calculateHash() {
    String data = index + previousHash + merkleRoot + 
                  timestamp + nonce + difficulty;
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(data.getBytes("UTF-8"));
    return bytesToHex(hash);
}
```

### 工作量证明挖矿

```java
public void mineBlock() {
    String target = new String(new char[difficulty]).replace('\0', '0');
    while (!blockHash.substring(0, difficulty).equals(target)) {
        nonce++;
        blockHash = calculateHash();
    }
}
```

### RSA数字签名

```java
// 签名
Signature signature = Signature.getInstance("SHA256withRSA");
signature.initSign(privateKey);
signature.update(data.getBytes());
byte[] signed = signature.sign();

// 验证
signature.initVerify(publicKey);
signature.update(data.getBytes());
boolean valid = signature.verify(signed);
```

---

## 系统截图

### 登录界面
简洁美观的登录/注册界面，支持多种用户类型注册。

### 系统主页
展示系统统计信息，包括产品总数、溯源记录数、区块数量等。

### 产品管理
产品列表展示，支持产品注册和二维码生成。

### 溯源查询
输入产品ID或扫描二维码，查看完整溯源历史。

### 区块链浏览器
查看区块链的区块列表和交易详情。

---

## 文档

- [毕业论文](documentation/论文-基于区块链与数字签名的农产品防伪溯源系统设计与实现.md)
- [项目复现文档](documentation/项目复现文档.md)
- [项目完整思路](documentation/项目完整思路.md)
- [汇报PPT](presentation/汇报PPT-基于区块链与数字签名的农产品防伪溯源系统.md)

---

## 测试

### 功能测试

- ✅ 用户注册/登录
- ✅ 产品注册
- ✅ 添加溯源记录
- ✅ 溯源查询
- ✅ 区块链验证
- ✅ 数字签名验证

### 性能测试

| 测试项 | 结果 |
|--------|------|
| 登录响应时间 | 0.8s |
| 查询响应时间 | 1.2s |
| 并发登录(50用户) | 通过 |
| 挖矿时间 | 3-5s |

---

## 未来规划

- [ ] 联盟链升级 (Hyperledger Fabric / FISCO BCOS)
- [ ] 共识算法优化 (PBFT / PoS)
- [ ] 移动端APP开发
- [ ] 物联网传感器集成
- [ ] 智能合约功能
- [ ] 跨链互操作

---

## 贡献者

- **袁敬东** - 项目设计与实现

---

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 致谢

- 感谢导师的悉心指导
- 感谢信息与管理科学学院的各位老师
- 感谢同学们的支持和帮助

---

**项目地址**：https://github.com/yourusername/agri-traceability-system

**作者**：袁敬东 (2210122238)

**学校**：河南农业大学

**完成日期**：2026年
