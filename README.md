# 农产品防伪溯源系统

基于轻量级区块链与 RSA 数字签名的农产品溯源毕业设计项目。系统提供产品注册、溯源记录上链、公开查询、区块链统计展示，以及 Web 端与移动端演示入口。

## 项目特点

- Web 前端、移动端、后端 API 一体化
- 用户密码采用 `BCrypt` 哈希存储
- 用户登录返回 JWT，非公开接口需要鉴权
- 溯源交易使用 RSA 数字签名校验
- 轻量级区块链支持本地持久化，服务重启后链状态可恢复
- Docker Compose 一键部署 MySQL、后端和前端

## 技术栈

- 前端：Vue 3、Vuex、Vue Router、Element Plus、Axios
- 移动端：uni-app、Vue 3、Axios
- 后端：Spring Boot 2.7、MyBatis-Plus、JWT、ZXing
- 数据库：MySQL 8
- 链上校验：Java 自实现轻量级区块链、SHA-256、Merkle Root、RSA 签名
- 部署：Docker Compose、Nginx

## 目录结构

```text
agri-traceability-system/
├─ backend/                 后端服务
├─ frontend/                Web 前端
├─ mobile/                  移动端
├─ documentation/           论文与项目文档
├─ presentation/            答辩材料
├─ docker-compose.yml       一键部署配置
└─ .env.example             部署环境变量示例
```

## 本地运行

### 1. 启动后端

先准备 MySQL 数据库，并设置环境变量：

```powershell
$env:DB_URL="jdbc:mysql://127.0.0.1:3306/agritrace?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="一段足够长的随机字符串"
$env:APP_KEY_SECRET="另一段足够长的随机字符串"
```

然后启动：

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

- API：`http://localhost:8080/api`
- 健康检查：`http://localhost:8080/api/health`

### 2. 启动 Web 前端

```bash
cd frontend
npm install
npm run serve
```

开发环境默认会访问 `http://localhost:8080/api`。

### 3. 启动移动端

```bash
cd mobile
npm install
npm run dev:h5
```

移动端默认请求地址为临时安全地址 `http://38.76.221.36:8088`，也可以在运行时通过本地存储覆盖 `apiBaseUrl`。

## Docker 部署

### 1. 准备环境变量

复制示例文件：

```bash
cp .env.example .env
```

至少配置以下值：

- `MYSQL_ROOT_PASSWORD`
- `JWT_SECRET`
- `APP_KEY_SECRET`

### 2. 启动服务

```bash
docker compose up -d --build
```

默认端口：

- 前端：`8088`
- 后端：`8080`

## 已部署演示地址

- Web：`http://38.76.221.36:8088`
- 后端健康检查：`http://38.76.221.36:8088/api/health`

演示账号：

- `demo_producer` / `Demo@123456`
- `demo_processor` / `Demo@123456`

## 当前实现说明

这是一个面向毕业设计答辩的完整原型系统，已经具备稳定演示能力，但仍不等同于生产级联盟链平台。当前版本重点解决的是：

- 溯源流程打通
- 签名与校验逻辑可运行
- 登录鉴权可用
- Docker 可部署
- 文档与代码基本一致

仍可继续增强的方向包括：

- 多节点共识
- 更严格的私钥托管方案
- 更细粒度的权限模型
- 更完整的自动化测试

## 相关文档

- [毕业论文](documentation/论文-基于区块链与数字签名的农产品防伪溯源系统设计与实现.md)
- [项目复现文档](documentation/项目复现文档.md)
- [项目完整思路](documentation/项目完整思路.md)
- [附录：需求分析与测试用例](documentation/附录-需求分析与测试用例.md)
- [附录：附件填写参考](documentation/附录-附件填写参考.md)
- [汇报 PPT 文稿](presentation/汇报PPT-基于区块链与数字签名的农产品防伪溯源系统.md)
