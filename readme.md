# URL Shortener（短链服务）

一个基于 Spring Boot 的短链服务，接收长链接并返回短链接，数据存储在 MySQL 中。

# 项目概览

## 技术栈

- 后端：Java 8 + Spring Boot 2.2.x
- 数据访问：Spring Data JPA + Hibernate
- 数据库：MySQL 8
- 安全：Spring Security + JWT 过滤器
- 监控：Spring Boot Actuator
- 其他：Apache Commons Validator（URL 校验）
- 构建：Gradle
- 部署：Docker / Docker Compose

## 架构与流程

- API 服务负责短链生成、跳转、统计记录
- MySQL 负责持久化存储
- Docker Compose 将 API 与 MySQL 通过自定义网络连接
- API 启动依赖 DB 健康检查通过后再启动

## 已实现功能

- 生成短链（POST /shorten）
- 短链跳转（GET /{shortCode}）
- URL 合法性校验
- Google Safe Browsing 安全检测（可开关）
- 健康检查（GET /actuator/health）
- MySQL 数据持久化（容器卷）

# 快速开始（Docker）

## 前置条件

- 已安装并启动 Docker Desktop

## 构建与启动

1. 构建 jar（API 镜像会拷贝 jar）

```shell
./gradlew clean build
```

2. 启动容器

```shell
docker-compose up -d
```

3. 健康检查

```shell
curl http://localhost:18080/actuator/health
```

返回 `{"status":"UP"}` 表示 API 正常运行。

## 端口说明

- API：宿主机 `18080` -> 容器 `8080`
- MySQL：宿主机 `13306` -> 容器 `3306`

# API 使用说明

Base URL（Docker）：`http://localhost:18080`

## POST /shorten

请求体：

```json
{
  "fullUrl": "https://example.com/example/1"
}
```

PowerShell 示例：

```powershell
Invoke-RestMethod -Method Post http://localhost:18080/shorten `
  -ContentType "application/json" `
  -Body '{"fullUrl":"https://example.com/example/1"}'
```

响应示例：

```json
{
  "shortUrl": "http://localhost:18080/og"
}
```

## GET /{shortCode}

直接访问返回的短链地址，会重定向到原始 URL。

## GET /actuator/health

```shell
curl http://localhost:18080/actuator/health
```

# 配置说明

## Safe Browsing

通过环境变量控制：

- `SAFEBROWSING_APIKEY`：Google Safe Browsing API Key
- `SAFEBROWSING_ENABLED=true|false`

如需关闭检测，设置：

```
SAFEBROWSING_ENABLED=false
```

# Docker 文件说明

- `api.Dockerfile`：API 镜像，拷贝 `build/libs/url-shortener-0.0.1-SNAPSHOT.jar`
- `db.Dockerfile`：MySQL 镜像，加载 `schema.sql`
- `docker-compose.yml`：编排 API + MySQL，包含网络、端口与健康检查

# 本地启动（非 Docker）

> 适合本地开发。

## 前置条件

- JDK 8
- MySQL 8
- Gradle（或使用项目内置的 `gradlew`）

## 创建数据库并导入 schema

1. 在本地 MySQL 创建数据库与用户：

```sql
CREATE DATABASE urldb DEFAULT CHARACTER SET utf8mb4;
CREATE USER 'urlshortener'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON urldb.* TO 'urlshortener'@'%';
FLUSH PRIVILEGES;
```

2. 导入表结构：

```shell
mysql -u urlshortener -p urldb < schema.sql
```

## 配置本地 application.properties

编辑 [src/main/resources/application.properties](src/main/resources/application.properties) 并设置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/urldb?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
spring.datasource.username=urlshortener
spring.datasource.password=123456
safebrowsing.enabled=true
safebrowsing.apiKey=${SAFEBROWSING_APIKEY}
```

> 如果不想启用 Safe Browsing，可将 `safebrowsing.enabled=false`。

## 设置环境变量

Windows PowerShell：

```powershell
$env:SAFEBROWSING_APIKEY="你的真实 API Key"
```

## 启动服务

```shell
./gradlew bootRun
```

启动后访问：

- `http://localhost:8080/actuator/health`
- `POST http://localhost:8080/shorten`

## 本地测试示例

```powershell
Invoke-RestMethod -Method Post http://localhost:8080/shorten `
  -ContentType "application/json" `
  -Body '{"fullUrl":"https://example.com/example/1"}'
```

# 常见问题

## 403（/actuator/health）

需要在安全配置白名单中允许 `/actuator/health`，当前已放行。

## 404（浏览器直接打开 /shorten）

`/shorten` 是 POST 接口，不能直接在浏览器打开。

## 端口占用

- `8080` 被占用时使用 `18080:8080`
- `3306` 被占用时使用 `13306:3306`

# 停止服务

```shell
docker-compose down
```

# 短链算法说明

采用 Base62 编码数据库自增 ID，生成短字符串，较哈希方式更短且更易记。

# Contributors

email: ahmad.zeeshaan@gmail.com
