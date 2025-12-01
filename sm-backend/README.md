# sm-backend 学籍管理系统后端

基于 Spring Boot 3 构建的学籍管理系统 RESTful API 服务。

## 技术栈

| 类别 | 技术 | 版本 |
| :--- | :--- | :--- |
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.0.2 |
| ORM | MyBatis-Plus | 3.5.6 |
| 数据库 | MySQL | 8.0+ |
| 连接池 | Druid | 1.2.23 |
| API文档 | Knife4j (OpenAPI 3) | 4.4.0 |
| 鉴权 | JWT (jjwt) | 0.11.5 |
| 工具 | Lombok, Apache POI, Validation | - |

## 目录结构

```
sm-backend/
├── sql/                          # 数据库脚本
│   ├── sm数据库设计.sql            # 建表语句
│   └── 示例数据.sql                # 测试数据
├── src/main/java/is/smbackend/
│   ├── SmBackendApplication.java # 启动类
│   ├── annotation/               # 自定义注解
│   ├── aspect/                   # AOP 切面 (日志记录)
│   ├── config/                   # 配置类 (Swagger, WebMvc, 跨域)
│   ├── controller/               # 控制层 (17个接口模块)
│   ├── service/                  # 业务逻辑层
│   ├── mapper/                   # MyBatis 数据访问层
│   ├── pojo/                     # 数据库实体类
│   ├── dto/                      # 数据传输对象
│   ├── response/                 # 统一响应封装 (Result)
│   ├── interceptor/              # 拦截器 (JWT鉴权)
│   ├── exception/                # 全局异常处理
│   └── util/                     # 工具类 (JwtUtil)
└── src/main/resources/
    └── application.yml           # 应用配置
```

## 快速启动

### 1. 数据库准备
```sql
CREATE DATABASE studentstatus_manage CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```
然后导入 `sql/sm数据库设计.sql` 和 `sql/示例数据.sql`。

### 2. 修改配置
编辑 `src/main/resources/application.yml`，配置数据库连接：
```yaml
spring:
  datasource:
    url: jdbc:mysql://your-database-address:3306/studentstatus_manage?characterEncoding=UTF-8&serverTimezone=GMT
    username: username
    password: password
```

### 3. 启动服务
```bash
# Maven 命令启动
mvn clean spring-boot:run

# 或打包后运行
mvn clean package -DskipTests
java -jar target/sm-backend-0.0.1-SNAPSHOT.jar
```

### 4. 访问 API 文档
启动成功后，访问 Knife4j 接口文档：
> **http://localhost:8080/smbackend/doc.html**

## API 模块

| 模块 | 路径前缀 | 说明 |
| :--- | :--- | :--- |
| 登录认证 | `/login` | 多角色登录、JWT Token 生成 |
| 学生基本信息 | `/student/basic` | 学生基础资料 CRUD |
| 学籍信息 | `/student/status` | 学籍状态管理 |
| 奖励管理 | `/award` | 奖励申请与审批 |
| 处分管理 | `/punishment` | 处分记录与审批 |
| 申诉管理 | `/appeal` | 处分申诉流程 |
| 学籍变动 | `/statusChange` | 转专业/休学/复学等 |
| 离校手续 | `/leaveSchool` | 多部门联审流程 |
| 通知管理 | `/notice` | 系统通知发布与查询 |
| 附件管理 | `/attachment` | 文件上传下载 |
| 数据导出 | `/export` | Excel 导出 |
| 操作日志 | `/log` | 系统日志查询 |
| 数据看板 | `/dashboard` | 统计数据接口 |
| 个人中心 | `/profile` | 用户信息修改 |

## 配置说明

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `server.port` | 8080 | 服务端口 |
| `server.servlet.context-path` | /smbackend | 上下文路径 |
| `file.upload-dir` | uploads | 文件上传目录 |
| `spring.servlet.multipart.max-file-size` | 10MB | 单文件大小限制 |

## 测试账号

| 角色 | 账号 | 密码 |
| :--- | :--- | :--- |
| 学生 | U202341001 | 123 |
| 辅导员 | G202310001 | 123 |
| 教务管理员 | G202350001 | 123 |
| 宿管管理员 | G202320001 | 123 |
| 图书馆管理员 | G202330001 | 123 |
| 财务管理员 | G202340001 | 123 |
