# sm-frontend 学籍管理系统前端

基于 Vue 3 + TypeScript + Element Plus 构建的学籍管理系统前端应用。

## 技术栈

| 类别 | 技术 | 版本 |
| :--- | :--- | :--- |
| 运行环境 | Node.js | >= 20 |
| 框架 | Vue | 3.5 |
| 构建工具 | Vite | 7.2 |
| 语言 | TypeScript | 5.9 |
| UI 组件库 | Element Plus | 2.11 |
| 状态管理 | Pinia | 3.0 |
| 路由 | Vue Router | 4.6 |
| HTTP 客户端 | Axios | 1.13 |
| 样式预处理 | Less | 4.4 |

## 目录结构

```
sm-frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/                # 后端 API 接口封装
│   │   ├── user.ts         # 用户相关接口
│   │   ├── student.ts      # 学生信息接口
│   │   ├── business.ts     # 业务流程接口
│   │   └── types.ts        # 类型定义
│   ├── components/         # 公共组件
│   ├── composables/        # 组合式函数
│   ├── layout/             # 布局组件
│   │   └── MainLayout.vue  # 主布局 (侧边栏+顶栏)
│   ├── router/             # 路由配置
│   │   └── index.ts        # 路由定义与守卫
│   ├── stores/             # Pinia 状态管理
│   ├── styles/             # 全局样式
│   ├── utils/              # 工具函数
│   │   └── request.ts      # Axios 封装 (拦截器、Token)
│   ├── views/              # 页面视图
│   │   ├── LoginView.vue   # 登录页
│   │   ├── student/        # 学生端页面 (9个)
│   │   ├── advisor/        # 辅导员端页面 (6个)
│   │   ├── admin/          # 管理员端页面 (9个)
│   │   └── common/         # 公共页面
│   ├── App.vue             # 根组件
│   └── main.ts             # 入口文件
├── index.html              # HTML 模板
├── vite.config.ts          # Vite 配置
├── tsconfig.json           # TypeScript 配置
└── package.json            # 依赖配置
```

## 快速启动

### 1. 安装依赖
```bash
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```
访问 http://localhost:5173

### 3. 生产构建
```bash
npm run build
```
构建产物输出到 `dist/` 目录。

## 功能模块

### 学生端 (`/student`)
| 路由 | 页面 | 功能 |
| :--- | :--- | :--- |
| `/student/dashboard` | 主页 | 数据概览、待办事项 |
| `/student/profile` | 个人信息 | 查看基本信息 |
| `/student/status` | 学籍信息 | 查看学籍状态 |
| `/student/award` | 奖励申请 | 提交奖励申请 |
| `/student/punishment` | 处分查看 | 查看处分记录 |
| `/student/appeal` | 申诉管理 | 提交处分申诉 |
| `/student/status-change` | 学籍变动 | 申请转专业/休学等 |
| `/student/leave` | 离校手续 | 发起离校申请 |
| `/student/notice` | 系统通知 | 查看通知消息 |

### 辅导员端 (`/advisor`)
| 路由 | 页面 | 功能 |
| :--- | :--- | :--- |
| `/advisor/dashboard` | 主页 | 待办统计 |
| `/advisor/students` | 学生列表 | 管理所带学生 |
| `/advisor/award-review` | 奖励审批 | 初审奖励申请 |
| `/advisor/appeal-review` | 申诉审理 | 初审处分申诉 |
| `/advisor/status-change-review` | 学籍变动审核 | 初审学籍变动 |
| `/advisor/notice` | 系统通知 | 查看/发布通知 |

### 管理员端 (`/admin`)
| 路由 | 页面 | 功能 |
| :--- | :--- | :--- |
| `/admin/dashboard` | 主页 | 全局数据统计 |
| `/admin/students` | 学生管理 | 全校学生信息 |
| `/admin/award-review` | 奖励终审 | 终审奖励申请 |
| `/admin/punishment` | 处分管理 | 录入/管理处分 |
| `/admin/appeal-review` | 申诉终审 | 终审处分申诉 |
| `/admin/status-change-review` | 学籍变动终审 | 终审学籍变动 |
| `/admin/leave-review` | 离校审核 | 多部门联审 |
| `/admin/notice` | 通知管理 | 发布系统通知 |
| `/admin/log` | 操作日志 | 查看系统日志 |

## 配置说明

### 开发环境代理 (`vite.config.ts`)
```typescript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080/smbackend',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```
前端所有 `/api/*` 请求会被代理到后端 `http://localhost:8080/smbackend/*`。

### 生产环境
生产环境需配置 Nginx 反向代理，将 `/api/` 请求转发到后端服务。

## 测试账号

| 角色 | 账号 | 密码 |
| :--- | :--- | :--- |
| 学生 | U202341001 | 123 |
| 辅导员 | G202310001 | 123 |
| 教务管理员 | G202350001 | 123 |

## 脚本命令

| 命令 | 说明 |
| :--- | :--- |
| `npm run dev` | 启动开发服务器 |
| `npm run build` | 生产环境构建 |
| `npm run preview` | 预览构建产物 |
| `npm run type-check` | TypeScript 类型检查 |
