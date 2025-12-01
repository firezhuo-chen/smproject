<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  userId: '',
  passwd: '',
  userType: 'student' as 'student' | 'advisor' | 'admin'
})

const rules = reactive<FormRules>({
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' }
  ],
  passwd: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ]
})

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    
    // 根据用户类型跳转到主页
    switch (loginForm.userType) {
      case 'student':
        router.push('/student/dashboard')
        break
      case 'advisor':
        router.push('/advisor/dashboard')
        break
      case 'admin':
        router.push('/admin/dashboard')
        break
    }
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <!-- 左上角校徽 -->
    <div class="logo-wrapper">
      <img src="@/assets/logo.png" alt="校徽" class="logo-img" />
    </div>
    
    <div class="login-box">
      <div class="login-header">
        <h1>学籍管理系统</h1>
        <p class="login-subtitle">信息系统开发实践</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        size="large"
      >
        <el-form-item prop="userType">
          <el-radio-group v-model="loginForm.userType" class="user-type-radio">
            <el-radio-button value="student">学生</el-radio-button>
            <el-radio-button value="advisor">辅导员</el-radio-button>
            <el-radio-button value="admin">管理员</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item prop="userId">
          <el-input
            v-model="loginForm.userId"
            placeholder="请输入用户ID"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="passwd">
          <el-input
            v-model="loginForm.passwd"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tips">
        <p>测试账号： （密码：123）</p>
        <p>学生：U202341001 | 辅导员：G202310001</p>
        <p>宿管管理员：G202320001 | 图书馆管理员：G202330001</p>
        <p>财务处管理员：G202340001 | 教务管理员：G202350001</p>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url('@/assets/login-bg.jpg') no-repeat center center;
  background-size: cover;
  position: relative;
  
  // 白色透明遮罩
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.75);
    z-index: 0;
  }
}

// 左上角校徽
.logo-wrapper {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 10;
  
  .logo-img {
    height: 60px;
    width: auto;
  }
}

.login-box {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h1 {
    margin: 0;
    font-size: 28px;
    color: #1a56db;
    font-weight: bold;
  }
  
  .login-subtitle {
    margin: 8px 0 0 0;
    font-size: 14px;
    color: #666;
  }
}

.login-form {
  .user-type-radio {
    width: 100%;
    display: flex;
    
    :deep(.el-radio-button) {
      flex: 1;
      
      .el-radio-button__inner {
        width: 100%;
      }
    }
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.login-tips {
  margin-top: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 12px;
  color: #909399;
  
  p {
    margin: 4px 0;
  }
}

// 响应式适配
@media screen and (max-width: 480px) {
  .logo-wrapper {
    top: 16px;
    left: 16px;
    
    .logo-img {
      height: 40px;
    }
  }
  
  .login-box {
    width: 90%;
    max-width: 360px;
    padding: 24px 20px;
    margin: 20px;
  }
  
  .login-header {
    margin-bottom: 20px;
    
    h1 {
      font-size: 22px;
    }
    
    .login-subtitle {
      font-size: 12px;
    }
  }
  
  .login-tips {
    font-size: 11px;
    padding: 10px;
  }
}
</style>
