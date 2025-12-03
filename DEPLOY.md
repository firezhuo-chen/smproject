# 🚀 学籍管理系统部署指南

本文档详细说明如何将学籍管理系统部署到 **Linux**（多发行版）、**Windows** 和 **macOS** 环境。

**部署架构**: 本地构建 + 数据库 + Web 服务器

## 📋 目录

- [1. 部署架构](#1-部署架构)
- [2. 环境要求](#2-环境要求)
- [3. 开发环境安装](#3-开发环境安装)
  - [3.1 Linux](#31-linux)
  - [3.2 Windows](#32-windows)
  - [3.3 macOS](#33-macos)
- [4. 数据库配置](#4-数据库配置)
- [5. 应用构建](#5-应用构建)
- [6. 开发环境运行](#6-开发环境运行)
- [7. 生产环境部署](#7-生产环境部署)
  - [7.1 Linux 生产部署](#71-linux-生产部署)
  - [7.2 Windows 生产部署](#72-windows-生产部署)
  - [7.3 macOS 生产部署](#73-macos-生产部署)
- [8. 验证与测试](#8-验证与测试)
- [9. 运维管理](#9-运维管理)
- [10. 常见问题](#10-常见问题)

---

## 1. 部署架构

### 1.1 架构概览

```
┌─────────────────┐     HTTP/JSON     ┌─────────────────┐
│     Browser     │ ◄───────────────► │   Web Server    │
│    (Vue SPA)    │                   │ (Nginx/IIS等)   │
└─────────────────┘                   └────────┬────────┘
                                               │
                                               ▼
                                      ┌─────────────────┐
                                      │   Spring Boot   │
                                      │    Backend      │
                                      └────────┬────────┘
                                               │
                                               ▼
                                      ┌─────────────────┐
                                      │     MySQL       │
                                      └─────────────────┘
```

### 1.2 目录结构（推荐）

**Linux/macOS:**
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

**Windows:**
```
C:\sm-system\
├── backend\
│   ├── sm-backend-0.0.1-SNAPSHOT.jar
│   ├── uploads\
│   └── logs\
└── frontend\
    ├── index.html
    ├── assets\
    └── ...
```

---

## 2. 环境要求

| 组件 | 版本要求 | 说明 |
| :--- | :--- | :--- |
| **JDK** | 17+ | 后端运行时 |
| **Node.js** | 20+ | 前端构建 |
| **npm** | 10+ | 包管理器（随 Node.js 安装） |
| **Maven** | 3.6+ | 后端构建 |
| **MySQL** | 8.0+ | 数据库 |
| **Web 服务器** | Nginx 1.18+ / IIS 10+ / Apache 2.4+ | 生产环境前端代理（可选） |

### 验证环境

```bash
# 所有平台通用命令
java -version     # >= 17.x.x
node --version    # >= 20.x.x
npm --version     # >= 10.x.x
mvn --version     # >= 3.6.x
mysql --version   # >= 8.0.x
```

---

## 3. 开发环境安装

### 3.1 Linux

#### Ubuntu / Debian
```bash
# 更新系统包
sudo apt update && sudo apt upgrade -y

# 安装 JDK 17
sudo apt install openjdk-17-jdk -y

# 安装 Node.js 20 (使用 NodeSource)
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install nodejs -y

# 安装 Maven
sudo apt install maven -y

# 安装 MySQL 8.0
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql

# 安装 Nginx（可选，生产环境）
sudo apt install nginx -y
```

#### CentOS / RHEL / Rocky Linux / AlmaLinux
```bash
# 更新系统包
sudo dnf update -y

# 安装 JDK 17
sudo dnf install java-17-openjdk-devel -y

# 安装 Node.js 20 (使用 NodeSource)
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
sudo dnf install nodejs -y

# 安装 Maven
sudo dnf install maven -y

# 安装 MySQL 8.0
sudo dnf install mysql-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 安装 Nginx（可选，生产环境）
sudo dnf install nginx -y
```

#### Fedora
```bash
# 更新系统包
sudo dnf update -y

# 安装 JDK 17
sudo dnf install java-17-openjdk-devel -y

# 安装 Node.js 20
sudo dnf install nodejs -y

# 安装 Maven
sudo dnf install maven -y

# 安装 MySQL 8.0
sudo dnf install community-mysql-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 安装 Nginx（可选，生产环境）
sudo dnf install nginx -y
```

#### Arch Linux / Manjaro
```bash
# 更新系统包
sudo pacman -Syu

# 安装 JDK 17
sudo pacman -S jdk17-openjdk

# 安装 Node.js (最新 LTS)
sudo pacman -S nodejs npm

# 安装 Maven
sudo pacman -S maven

# 安装 MySQL (MariaDB 兼容)
sudo pacman -S mariadb
sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql
sudo systemctl start mariadb
sudo systemctl enable mariadb

# 安装 Nginx（可选，生产环境）
sudo pacman -S nginx
```

#### openSUSE
```bash
# 更新系统包
sudo zypper refresh && sudo zypper update -y

# 安装 JDK 17
sudo zypper install java-17-openjdk-devel -y

# 安装 Node.js 20
sudo zypper install nodejs20 npm20 -y

# 安装 Maven
sudo zypper install maven -y

# 安装 MySQL 8.0
sudo zypper install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql

# 安装 Nginx（可选，生产环境）
sudo zypper install nginx -y
```

---

### 3.2 Windows

#### 方式一：手动安装

1. **安装 JDK 17**
   - 下载: [Adoptium Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17)
   - 运行安装程序，勾选 "Set JAVA_HOME variable"
   - 验证: 打开命令提示符，运行 `java -version`

2. **安装 Node.js 20**
   - 下载: [Node.js 20 LTS](https://nodejs.org/)
   - 运行安装程序，默认选项即可
   - 验证: 打开命令提示符，运行 `node --version`

3. **安装 Maven**
   - 下载: [Apache Maven](https://maven.apache.org/download.cgi) (Binary zip archive)
   - 解压到 `C:\Program Files\Apache\maven`
   - 添加环境变量:
     - `MAVEN_HOME`: `C:\Program Files\Apache\maven`
     - 将 `%MAVEN_HOME%\bin` 添加到 `PATH`
   - 验证: 打开新命令提示符，运行 `mvn --version`

4. **安装 MySQL 8.0**
   - 下载: [MySQL Installer](https://dev.mysql.com/downloads/installer/)
   - 运行安装程序，选择 "Developer Default" 或 "Server only"
   - 设置 root 密码
   - 验证: 打开命令提示符，运行 `mysql -u root -p`

#### 方式二：使用包管理器 (推荐)

使用 [Chocolatey](https://chocolatey.org/) 或 [Scoop](https://scoop.sh/):

```powershell
# 使用 Chocolatey (以管理员身份运行 PowerShell)
choco install temurin17 -y
choco install nodejs-lts -y
choco install maven -y
choco install mysql -y

# 或使用 Scoop (普通用户 PowerShell)
scoop bucket add java
scoop install temurin17-jdk
scoop install nodejs-lts
scoop install maven
scoop install mysql
```

#### 方式三：使用 winget

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
winget install OpenJS.NodeJS.LTS
winget install Apache.Maven
winget install Oracle.MySQL
```

---

### 3.3 macOS

#### 使用 Homebrew (推荐)

```bash
# 安装 Homebrew (如果未安装)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 JDK 17
brew install openjdk@17
# 链接 JDK
sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 安装 Node.js 20
brew install node@20
echo 'export PATH="/opt/homebrew/opt/node@20/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# 安装 Maven
brew install maven

# 安装 MySQL 8.0
brew install mysql
brew services start mysql

# 安装 Nginx（可选，生产环境）
brew install nginx
```

#### 手动安装

1. **JDK 17**: 从 [Adoptium](https://adoptium.net/) 下载 macOS 安装包
2. **Node.js 20**: 从 [Node.js 官网](https://nodejs.org/) 下载 macOS 安装包
3. **Maven**: 从 [Maven 官网](https://maven.apache.org/) 下载并解压
4. **MySQL**: 从 [MySQL 官网](https://dev.mysql.com/downloads/mysql/) 下载 DMG 安装包

---

## 4. 数据库配置

### 4.1 创建数据库

```sql
-- 登录 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE studentstatus_manage CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 创建应用用户（可选，生产环境推荐）
CREATE USER 'sm_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON studentstatus_manage.* TO 'sm_user'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

### 4.2 导入表结构和测试数据

```bash
# Linux/macOS
mysql -u root -p studentstatus_manage < sm-backend/sql/sm数据库设计.sql
mysql -u root -p studentstatus_manage < sm-backend/sql/示例数据.sql

# Windows (命令提示符)
mysql -u root -p studentstatus_manage < sm-backend\sql\sm数据库设计.sql
mysql -u root -p studentstatus_manage < sm-backend\sql\示例数据.sql
```

### 4.3 配置应用连接

编辑 `sm-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/studentstatus_manage?characterEncoding=UTF-8&serverTimezone=GMT
    username: root  # 或 sm_user
    password: your_password
```

---

## 5. 应用构建

### 5.1 构建前端

```bash
# 进入前端目录
cd sm-frontend

# 安装依赖
npm install

# 开发模式运行（可选，用于测试）
npm run dev

# 生产构建
npm run build

# 验证构建产物
# Linux/macOS
ls -la dist/

# Windows
dir dist
```

### 5.2 构建后端

```bash
# 进入后端目录
cd sm-backend

# 清理并打包 (跳过测试加速构建)
mvn clean package -DskipTests

# 验证 JAR 包
# Linux/macOS
ls -la target/sm-backend-0.0.1-SNAPSHOT.jar

# Windows
dir target\sm-backend-0.0.1-SNAPSHOT.jar
```

---

## 6. 开发环境运行

### 6.1 启动后端

```bash
cd sm-backend

# 方式一：使用 Maven
mvn clean spring-boot:run

# 方式二：运行 JAR 包
java -jar target/sm-backend-0.0.1-SNAPSHOT.jar
```

后端服务地址: `http://localhost:8080/smbackend`  
API 文档: `http://localhost:8080/smbackend/doc.html`

### 6.2 启动前端

```bash
cd sm-frontend
npm run dev
```

前端服务地址: `http://localhost:5173`

### 6.3 测试账号

| 角色 | 账号 | 密码 |
| :--- | :--- | :--- |
| 学生 | U202341001 | 123 |
| 辅导员 | G202310001 | 123 |
| 教务管理员 | G202350001 | 123 |
| 宿管管理员 | G202320001 | 123 |
| 图书馆管理员 | G202330001 | 123 |
| 财务管理员 | G202340001 | 123 |

---

## 7. 生产环境部署

### 7.1 Linux 生产部署

#### 7.1.1 创建部署目录

```bash
# 创建应用目录
sudo mkdir -p /opt/sm-system/{backend,frontend}
sudo mkdir -p /opt/sm-system/backend/{logs,uploads}

# 设置权限
sudo chown -R $USER:$USER /opt/sm-system
```

#### 7.1.2 部署应用

```bash
# 复制后端 JAR 包
cp sm-backend/target/sm-backend-0.0.1-SNAPSHOT.jar /opt/sm-system/backend/

# 复制前端构建产物
cp -r sm-frontend/dist/* /opt/sm-system/frontend/
```

#### 7.1.3 创建系统服务 (systemd)

创建 `/etc/systemd/system/sm-backend.service`:

```bash
sudo tee /etc/systemd/system/sm-backend.service > /dev/null << 'EOF'
[Unit]
Description=Student Status Management Backend
After=network.target mysql.service

[Service]
Type=simple
User=www-data
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

启动服务:

```bash
sudo systemctl daemon-reload
sudo systemctl enable sm-backend
sudo systemctl start sm-backend
sudo systemctl status sm-backend
```

#### 7.1.4 配置 Nginx

创建 `/etc/nginx/sites-available/sm-system` (Debian/Ubuntu) 或 `/etc/nginx/conf.d/sm-system.conf` (RHEL/CentOS):

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或 IP

    # 前端静态资源
    location / {
        root /opt/sm-system/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/smbackend/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        client_max_body_size 10m;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        root /opt/sm-system/frontend;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/json;
}
```

启用配置:

```bash
# Debian/Ubuntu
sudo ln -s /etc/nginx/sites-available/sm-system /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# 测试并重启
sudo nginx -t
sudo systemctl restart nginx
```

#### 7.1.5 配置防火墙

```bash
# Ubuntu/Debian (ufw)
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

---

### 7.2 Windows 生产部署

#### 7.2.1 创建部署目录

```powershell
# PowerShell
New-Item -ItemType Directory -Force -Path C:\sm-system\backend\logs
New-Item -ItemType Directory -Force -Path C:\sm-system\backend\uploads
New-Item -ItemType Directory -Force -Path C:\sm-system\frontend
```

#### 7.2.2 部署应用

```powershell
# 复制后端 JAR 包
Copy-Item sm-backend\target\sm-backend-0.0.1-SNAPSHOT.jar C:\sm-system\backend\

# 复制前端构建产物
Copy-Item -Recurse sm-frontend\dist\* C:\sm-system\frontend\
```

#### 7.2.3 创建 Windows 服务

使用 [WinSW](https://github.com/winsw/winsw) 将 JAR 包注册为 Windows 服务:

1. 下载 [WinSW.exe](https://github.com/winsw/winsw/releases) 并重命名为 `sm-backend.exe`
2. 放置到 `C:\sm-system\backend\`
3. 创建 `C:\sm-system\backend\sm-backend.xml`:

```xml
<service>
  <id>sm-backend</id>
  <name>Student Status Management Backend</name>
  <description>Student Status Management Backend Service</description>
  <executable>java</executable>
  <arguments>-jar sm-backend-0.0.1-SNAPSHOT.jar --file.upload-dir=C:\sm-system\backend\uploads</arguments>
  <workingdirectory>C:\sm-system\backend</workingdirectory>
  <logpath>C:\sm-system\backend\logs</logpath>
  <log mode="roll-by-size">
    <sizeThreshold>10240</sizeThreshold>
    <keepFiles>8</keepFiles>
  </log>
</service>
```

4. 安装并启动服务:

```powershell
# 以管理员身份运行 PowerShell
cd C:\sm-system\backend
.\sm-backend.exe install
.\sm-backend.exe start

# 查看状态
.\sm-backend.exe status
```

#### 7.2.4 配置 IIS (可选)

1. 安装 IIS:
   - 打开 "控制面板" → "程序和功能" → "启用或关闭 Windows 功能"
   - 勾选 "Internet Information Services"

2. 安装 URL Rewrite 模块:
   - 下载 [URL Rewrite](https://www.iis.net/downloads/microsoft/url-rewrite)

3. 安装 ARR (Application Request Routing):
   - 下载 [ARR](https://www.iis.net/downloads/microsoft/application-request-routing)

4. 配置站点:
   - 打开 IIS 管理器
   - 创建新站点，物理路径指向 `C:\sm-system\frontend`
   - 配置 URL Rewrite 规则进行 API 代理

#### 7.2.5 配置 Nginx for Windows (替代方案)

1. 下载 [Nginx for Windows](http://nginx.org/en/download.html)
2. 解压到 `C:\nginx`
3. 编辑 `C:\nginx\conf\nginx.conf`:

```nginx
http {
    server {
        listen 80;
        server_name localhost;

        location / {
            root C:/sm-system/frontend;
            index index.html;
            try_files $uri $uri/ /index.html;
        }

        location /api/ {
            proxy_pass http://127.0.0.1:8080/smbackend/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
```

4. 启动 Nginx:

```powershell
cd C:\nginx
.\nginx.exe
```

---

### 7.3 macOS 生产部署

#### 7.3.1 创建部署目录

```bash
sudo mkdir -p /opt/sm-system/{backend,frontend}
sudo mkdir -p /opt/sm-system/backend/{logs,uploads}
sudo chown -R $(whoami):staff /opt/sm-system
```

#### 7.3.2 部署应用

```bash
cp sm-backend/target/sm-backend-0.0.1-SNAPSHOT.jar /opt/sm-system/backend/
cp -r sm-frontend/dist/* /opt/sm-system/frontend/
```

#### 7.3.3 创建 LaunchDaemon 服务

创建 `/Library/LaunchDaemons/com.sm.backend.plist`:

```bash
sudo tee /Library/LaunchDaemons/com.sm.backend.plist > /dev/null << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.sm.backend</string>
    <key>ProgramArguments</key>
    <array>
        <string>/usr/bin/java</string>
        <string>-jar</string>
        <string>/opt/sm-system/backend/sm-backend-0.0.1-SNAPSHOT.jar</string>
        <string>--file.upload-dir=/opt/sm-system/backend/uploads</string>
    </array>
    <key>WorkingDirectory</key>
    <string>/opt/sm-system/backend</string>
    <key>StandardOutPath</key>
    <string>/opt/sm-system/backend/logs/backend.log</string>
    <key>StandardErrorPath</key>
    <string>/opt/sm-system/backend/logs/backend-error.log</string>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <true/>
</dict>
</plist>
EOF
```

启动服务:

```bash
sudo launchctl load /Library/LaunchDaemons/com.sm.backend.plist
sudo launchctl start com.sm.backend

# 查看状态
sudo launchctl list | grep sm.backend
```

#### 7.3.4 配置 Nginx

```bash
# 使用 Homebrew 安装的 Nginx
# 编辑 /opt/homebrew/etc/nginx/nginx.conf 或 /usr/local/etc/nginx/nginx.conf

# 添加 server 块配置（同 Linux 配置）

# 启动 Nginx
brew services start nginx
```

---

## 8. 验证与测试

### 8.1 检查服务状态

```bash
# Linux (systemd)
sudo systemctl status sm-backend

# Windows (PowerShell)
Get-Service sm-backend

# macOS (launchd)
sudo launchctl list | grep sm.backend
```

### 8.2 验证 API

```bash
# 检查后端 API
curl http://localhost:8080/smbackend/doc.html

# 检查进程
# Linux/macOS
ps aux | grep java

# Windows (PowerShell)
Get-Process java
```

### 8.3 访问系统

1. 打开浏览器访问: `http://your-server-ip` (生产) 或 `http://localhost:5173` (开发)
2. 使用测试账号登录验证各角色功能

---

## 9. 运维管理

### 9.1 服务管理命令

| 操作 | Linux (systemd) | Windows | macOS (launchd) |
| :--- | :--- | :--- | :--- |
| 启动 | `sudo systemctl start sm-backend` | `.\sm-backend.exe start` | `sudo launchctl start com.sm.backend` |
| 停止 | `sudo systemctl stop sm-backend` | `.\sm-backend.exe stop` | `sudo launchctl stop com.sm.backend` |
| 重启 | `sudo systemctl restart sm-backend` | `.\sm-backend.exe restart` | `sudo launchctl stop com.sm.backend && sudo launchctl start com.sm.backend` |
| 状态 | `sudo systemctl status sm-backend` | `.\sm-backend.exe status` | `sudo launchctl list` |
| 日志 | `tail -f /opt/sm-system/backend/logs/backend.log` | `Get-Content C:\sm-system\backend\logs\backend.log -Tail 100` | `tail -f /opt/sm-system/backend/logs/backend.log` |

### 9.2 日志轮转

**Linux:**
创建 `/etc/logrotate.d/sm-backend`:
```bash
/opt/sm-system/backend/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    copytruncate
}
```

**Windows:**
WinSW 配置已包含日志轮转，见 `sm-backend.xml` 中的 `<log>` 配置。

**macOS:**
使用 `newsyslog` 或手动配置脚本。

### 9.3 数据库备份

```bash
# 所有平台通用
mysqldump -u root -p studentstatus_manage > backup_$(date +%Y%m%d).sql

# 恢复
mysql -u root -p studentstatus_manage < backup_20231201.sql
```

---

## 10. 常见问题

### Q1: 后端启动失败

**检查步骤:**
1. 查看日志:
   - Linux/macOS: `tail -100 /opt/sm-system/backend/logs/backend.log`
   - Windows: `Get-Content C:\sm-system\backend\logs\backend.log -Tail 100`
2. 检查端口占用:
   - Linux/macOS: `lsof -i :8080` 或 `netstat -tlnp | grep :8080`
   - Windows: `netstat -ano | findstr :8080`
3. 验证数据库连接:
   ```bash
   mysql -h localhost -P 3306 -u root -p studentstatus_manage
   ```

### Q2: 前端页面 404

**原因:** Vue Router History 模式需要服务器支持

**解决:** 确保 Web 服务器配置了回退到 `index.html`
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

### Q3: API 请求失败

**原因:** 代理配置错误

**解决:** 检查 `proxy_pass` 配置，注意结尾的 `/`
```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080/smbackend/;  # 注意结尾的 /
}
```

### Q4: 文件上传失败

**原因:** 服务器限制上传大小

**解决:**
- Nginx: 添加 `client_max_body_size 10m;`
- IIS: 配置 `maxAllowedContentLength`

### Q5: Java 版本不兼容

**原因:** 系统默认 Java 版本不是 17

**解决:**
```bash
# Linux (update-alternatives)
sudo update-alternatives --config java

# macOS
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Windows (设置 JAVA_HOME 环境变量)
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot"
```

### Q6: Node.js 版本不兼容

**原因:** 项目要求 Node.js 20+

**解决:** 使用版本管理器
```bash
# 使用 nvm (Linux/macOS)
nvm install 20
nvm use 20

# 使用 nvm-windows (Windows)
nvm install 20
nvm use 20
```

---

## 📚 相关文档

- [README.md](./README.md) - 项目概览
- [sm-backend/README.md](./sm-backend/README.md) - 后端文档
- [sm-frontend/README.md](./sm-frontend/README.md) - 前端文档

## 📞 API 模块说明

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

## ⚙️ 配置说明

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `server.port` | 8080 | 服务端口 |
| `server.servlet.context-path` | /smbackend | 上下文路径 |
| `file.upload-dir` | uploads | 文件上传目录 |
| `spring.servlet.multipart.max-file-size` | 10MB | 单文件大小限制 |
