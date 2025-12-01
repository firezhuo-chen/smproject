# 🚀 学籍管理系统部署指南

本文档详细说明如何将学籍管理系统部署到 Ubuntu 24.04 LTS 生产环境。

**部署架构**: 本地构建 + 远程数据库 + 最小化服务器环境

## 📋 目录

- [1. 部署架构](#1-部署架构)
- [2. 本地构建环境](#2-本地构建环境)
- [3. 服务器环境准备](#3-服务器环境准备)
- [4. 本地构建和部署](#4-本地构建和部署)
- [5. 服务器配置](#5-服务器配置)
- [6. 启动验证](#6-启动验证)
- [7. 运维管理](#7-运维管理)
- [8. 常见问题](#8-常见问题)

---

## 1. 部署架构

### 1.1 架构概览

```
┌─────────────────┐    构建产物    ┌─────────────────┐
│   本地开发机     │ ──────────────► │  Ubuntu服务器    │
│ Node.js + Maven │                │ JDK17 + Nginx   │
└─────────────────┘                └─────────────────┘
                                           │
                                           ▼
                                   ┌─────────────────┐
                                   │  阿里云RDS MySQL │
                                   └─────────────────┘
```

### 1.2 环境要求

| 环境 | 组件 | 版本要求 | 说明 |
| :--- | :--- | :--- | :--- |
| **本地开发机** | Node.js | 20+ | 前端构建 |
| | Maven | 3.6+ | 后端打包 |
| | JDK | 17+ | 后端编译 |
| **Ubuntu服务器** | 操作系统 | Ubuntu 24.04 LTS | 生产环境 |
| | JDK | 17+ | 后端运行时 |
| | Nginx | 1.18+ | Web服务器 |
| **远程数据库** | MySQL | 8.0+ | 阿里云RDS |

### 1.3 目录结构
```
/opt/sm-system/
├── backend/
│   ├── sm-backend-0.0.1-SNAPSHOT.jar
│   ├── uploads/              # 文件上传目录
│   └── logs/                 # 日志目录
└── frontend/                 # 前端静态文件
    ├── index.html
    ├── assets/
    └── ...
```

---

## 2. 本地构建环境

### 2.1 环境验证
```bash
# 验证本地开发环境
node --version    # >= 20.x.x
npm --version     # >= 10.x.x
mvn --version     # >= 3.6.x
java -version     # >= 17.x.x
```

### 2.2 远程数据库配置

项目已配置阿里云RDS MySQL数据库，配置信息如下：

```yaml
# application.yml 中的数据库配置
spring:
  datasource:
    url: jdbc:mysql://your-database-address:3306/studentstatus_manage?characterEncoding=UTF-8&serverTimezone=GMT
    username: username
    password: password
```

**注意**: 数据库表结构和测试数据已在远程数据库中配置完成，无需本地安装MySQL。

---

## 3. 服务器环境准备

### 3.1 系统更新
```bash
# 连接到Ubuntu服务器
ssh user@your-server-ip

# 更新系统包
sudo apt update && sudo apt upgrade -y
```

### 3.2 安装运行环境
```bash
# 安装JDK 17 (后端运行时)
sudo apt install openjdk-17-jdk -y

# 安装Nginx (Web服务器)
sudo apt install nginx -y

# 启动并设置开机自启
sudo systemctl start nginx
sudo systemctl enable nginx

# 验证安装
java -version
nginx -v
```

### 3.3 创建部署目录
```bash
# 创建应用目录结构
sudo mkdir -p /opt/sm-system/{backend,frontend}
sudo mkdir -p /opt/sm-system/backend/{logs,uploads}

# 设置目录权限
sudo chown -R $USER:$USER /opt/sm-system
```

### 3.4 配置防火墙
```bash
# 启用防火墙
sudo ufw enable

# 允许必要端口
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# 查看状态
sudo ufw status
```

---

## 4. 本地构建和部署

### 4.1 开发机环境要求
确保开发机已安装：
- **Node.js 20+** (前端构建)
- **Maven 3.6+** (后端打包)
- **JDK 17+** (后端编译)

### 4.2 构建前端应用
```bash
# 在开发机上执行
cd sm-frontend

# 安装依赖
npm install

# 构建生产版本
npm run build

# 验证构建产物
ls -la dist/
# 应该看到 index.html, assets/ 等文件
```

### 4.3 构建后端应用
```bash
# 在开发机上执行
cd sm-backend

# 清理并打包
mvn clean package -DskipTests

# 验证JAR包
ls -la target/sm-backend-0.0.1-SNAPSHOT.jar
# 应该看到约 50MB 的JAR文件
```

### 4.4 直接上传到服务器目标目录
```bash
# 上传后端JAR包到服务器
scp sm-backend/target/sm-backend-0.0.1-SNAPSHOT.jar user@your-server-ip:/opt/sm-system/backend/

# 上传前端构建产物到服务器
scp -r sm-frontend/dist/* user@your-server-ip:/opt/sm-system/frontend/

# 在服务器上设置权限
ssh user@your-server-ip << 'EOF'
sudo chown -R www-data:www-data /opt/sm-system/frontend
sudo chown -R $USER:$USER /opt/sm-system/backend
chmod +x /opt/sm-system/backend/sm-backend-0.0.1-SNAPSHOT.jar
EOF
```

---

## 5. 服务器配置

### 5.1 创建后端系统服务
创建 `/etc/systemd/system/sm-backend.service`：

```bash
sudo tee /etc/systemd/system/sm-backend.service > /dev/null << 'EOF'
[Unit]
Description=Student Status Management Backend
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/sm-system/backend
ExecStart=/usr/bin/java -jar sm-backend-0.0.1-SNAPSHOT.jar \
  --file.upload-dir=/opt/sm-system/backend/uploads
Restart=always
RestartSec=10
StandardOutput=append:/opt/sm-system/backend/logs/backend.log
StandardError=append:/opt/sm-system/backend/logs/backend-error.log

[Install]
WantedBy=multi-user.target
EOF
```

---

### 5.2 配置Nginx
创建 `/etc/nginx/sites-available/sm-system`：

```bash
sudo tee /etc/nginx/sites-available/sm-system > /dev/null << 'EOF'
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或IP

    # 前端静态资源
    location / {
        root /opt/sm-system/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/smbackend/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 文件上传大小限制
        client_max_body_size 10m;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 静态资源缓存优化
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        root /opt/sm-system/frontend;
        expires 30d;
        add_header Cache-Control "public, immutable";
        add_header Vary Accept-Encoding;
    }

    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;
}
EOF
```

### 5.3 启用Nginx配置
```bash
# 启用站点配置
sudo ln -s /etc/nginx/sites-available/sm-system /etc/nginx/sites-enabled/

# 删除默认配置
sudo rm -f /etc/nginx/sites-enabled/default

# 测试配置
sudo nginx -t

# 重启Nginx
sudo systemctl restart nginx
```

---

## 6. 启动验证

### 6.1 启动后端服务
```bash
# 重载systemd配置
sudo systemctl daemon-reload

# 启用并启动服务
sudo systemctl enable sm-backend
sudo systemctl start sm-backend

# 检查服务状态
sudo systemctl status sm-backend
```

### 6.2 验证服务运行
```bash
# 检查后端API
curl http://localhost:8080/smbackend/doc.html

# 检查进程
ps aux | grep java

# 查看日志
tail -f /opt/sm-system/backend/logs/backend.log
```

### 6.3 访问系统
1. 打开浏览器访问: `http://your-server-ip`
2. 使用测试账号登录:

| 角色 | 账号 | 密码 |
| :--- | :--- | :--- |
| 学生 | U202341001 | 123 |
| 辅导员 | G202310001 | 123 |
| 教务管理员 | G202350001 | 123 |
| 宿管管理员 | G202320001 | 123 |
| 图书馆管理员 | G202330001 | 123 |
| 财务管理员 | G202340001 | 123 |

---

## 7. 运维管理

### 7.1 常用管理命令
```bash
# 查看服务状态
sudo systemctl status sm-backend

# 重启服务
sudo systemctl restart sm-backend

# 查看实时日志
tail -f /opt/sm-system/backend/logs/backend.log

# 查看系统日志
sudo journalctl -u sm-backend -f

# 检查端口占用
sudo netstat -tlnp | grep :8080
```

### 7.2 日志轮转配置
创建 `/etc/logrotate.d/sm-backend`：
```bash
sudo tee /etc/logrotate.d/sm-backend > /dev/null << 'EOF'
/opt/sm-system/backend/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    copytruncate
    su ubuntu ubuntu
}
EOF
```


---

## 8. 常见问题

### Q1: 后端启动失败
**检查步骤**:
1. 查看日志: `tail -100 /opt/sm-system/backend/logs/backend.log`
2. 检查端口占用: `sudo netstat -tlnp | grep :8080`
3. 验证远程数据库连接: 
   ```bash
   # 安装MySQL客户端测试连接
   sudo apt install mysql-client -y
   mysql -h your-database-address -P 3306 -u Studentstatus_manage -p studentstatus_manage
   ```

### Q2: 前端页面404
**原因**: Vue Router History 模式需要服务器支持

**解决**: 确保 Nginx 配置了 `try_files`
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### Q3: API请求失败
**原因**: Nginx 代理配置错误

**解决**: 检查 `proxy_pass` 配置
```nginx
# 正确配置
location /api/ {
    proxy_pass http://127.0.0.1:8080/smbackend/;  # 注意结尾的 /
}
```

### Q4: 文件上传失败
**原因**: Nginx 默认限制 1MB

**解决**: 添加 `client_max_body_size`
```nginx
location /api/ {
    client_max_body_size 10m;
}
```

### Q5: 网络连接问题
**检查步骤**:
1. 测试服务器到RDS的网络连通性:
   ```bash
   telnet your-database-address 3306
   ```
2. 检查阿里云安全组规则
3. 确认RDS白名单包含服务器IP

---

## 📞 技术支持

### API模块说明

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

### 配置说明

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `server.port` | 8080 | 服务端口 |
| `server.servlet.context-path` | /smbackend | 上下文路径 |
| `file.upload-dir` | uploads | 文件上传目录 |
| `spring.servlet.multipart.max-file-size` | 10MB | 单文件大小限制 |

### 相关文档
- [README.md](./README.md) - 项目概览
- [sm-backend/README.md](./sm-backend/README.md) - 后端文档  
- [sm-frontend/README.md](./sm-frontend/README.md) - 前端文档
