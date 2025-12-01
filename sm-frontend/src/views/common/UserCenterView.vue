<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { request } from '@/utils/request'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const profile = ref<any>(null)
const editDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const editForm = ref({
  phone: '',
  email: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = ref<FormRules>({
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

const passwordRules = ref<FormRules>({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 3, message: '密码长度不能少于3位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await request.get(`/profile/${userStore.userType}/${userStore.userId}`)
    profile.value = res.data
  } catch (error) {
    console.error('获取个人信息失败', error)
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  editForm.value = {
    phone: profile.value?.phone || '',
    email: profile.value?.email || ''
  }
  editDialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await request.put(`/profile/${userStore.userType}/${userStore.userId}`, editForm.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    
    // 更新本地存储的用户信息
    const userInfo = { ...userStore.userInfo, phone: editForm.value.phone, email: editForm.value.email }
    userStore.updateUserInfo(userInfo as any)
    
    fetchProfile()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleChangePassword = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

const handleSavePassword = async () => {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await request.put(`/profile/${userStore.userType}/${userStore.userId}/password`, {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    saving.value = false
  }
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const getStatusType = (status: string) => {
  return status === '正常' ? 'success' : 'danger'
}

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <div class="user-center-container" v-loading="loading">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </div>
      </template>

      <div class="profile-content" v-if="profile">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-avatar :size="100" class="avatar">
            <el-icon :size="50"><User /></el-icon>
          </el-avatar>
          <div class="user-info">
            <h2>{{ profile.userName }}</h2>
            <div class="user-tags">
              <el-tag :type="profile.userType === 'student' ? 'primary' : profile.userType === 'advisor' ? 'success' : 'warning'">
                {{ profile.userTypeLabel }}
              </el-tag>
              <el-tag v-if="profile.role" type="info" style="margin-left: 8px;">
                {{ profile.role }}
              </el-tag>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 基本信息 -->
        <div class="info-section">
          <h3>
            <el-icon><InfoFilled /></el-icon>
            基本信息
          </h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">
              {{ profile.userId }}
            </el-descriptions-item>
            <el-descriptions-item label="用户名">
              {{ profile.userName }}
            </el-descriptions-item>
            <el-descriptions-item label="用户类型">
              <el-tag :type="profile.userType === 'student' ? 'primary' : profile.userType === 'advisor' ? 'success' : 'warning'" size="small">
                {{ profile.userTypeLabel }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="profile.role" label="管理员角色">
              <el-tag type="info" size="small">
                {{ profile.role }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag :type="getStatusType(profile.status)" size="small">
                {{ profile.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatTime(profile.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="最后登录">
              {{ formatTime(profile.lastLoginTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 联系方式 -->
        <div class="info-section">
          <h3>
            <el-icon><Phone /></el-icon>
            联系方式
            <el-button type="primary" size="small" @click="handleEdit" style="margin-left: auto;">
              <el-icon><Edit /></el-icon>
              修改
            </el-button>
          </h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="手机号码">
              <span v-if="profile.phone">{{ profile.phone }}</span>
              <span v-else class="text-gray">未设置</span>
            </el-descriptions-item>
            <el-descriptions-item label="电子邮箱">
              <span v-if="profile.email">{{ profile.email }}</span>
              <span v-else class="text-gray">未设置</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 账号安全 -->
        <div class="info-section">
          <h3>
            <el-icon><Lock /></el-icon>
            账号安全
            <el-button type="warning" size="small" @click="handleChangePassword" style="margin-left: auto;">
              <el-icon><Key /></el-icon>
              修改密码
            </el-button>
          </h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="登录密码">
              <span>******</span>
            </el-descriptions-item>
            <el-descriptions-item label="安全等级">
              <el-tag type="success" size="small">安全</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 安全提示 -->
        <el-alert
          title="安全提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 20px;"
        >
          <template #default>
            请确保您的联系方式准确无误，以便接收重要通知。定期修改密码可以提高账号安全性。
          </template>
        </el-alert>
      </div>
    </el-card>

    <!-- 编辑联系方式对话框 -->
    <el-dialog v-model="editDialogVisible" title="修改联系方式" width="450px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号码" maxlength="11">
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入电子邮箱">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="450px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSavePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.user-center-container {
  max-width: 900px;
  margin: 0 auto;

  .profile-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: bold;
    }
  }

  .profile-content {
    .avatar-section {
      display: flex;
      align-items: center;
      gap: 24px;
      padding: 20px 0;

      .avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      .user-info {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          color: #303133;
        }
      }
    }

    .info-section {
      margin-top: 24px;

      h3 {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 0 0 16px 0;
        font-size: 16px;
        color: #303133;
      }
    }

    .text-gray {
      color: #c0c4cc;
    }
  }
}
</style>
