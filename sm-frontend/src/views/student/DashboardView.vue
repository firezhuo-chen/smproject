<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const stats = ref<any>(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get(`/dashboard/student/${userStore.userId}`)
    stats.value = res.data
  } catch (error) {
    console.error('获取仪表盘数据失败', error)
  } finally {
    loading.value = false
  }
}

const getWarningLevelType = (level: string) => {
  switch (level) {
    case '无': return 'success'
    case '一级': return 'warning'
    case '二级': return 'danger'
    case '三级': return 'danger'
    default: return 'info'
  }
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const goTo = (path: string) => {
  router.push(path)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <h2>欢迎回来，{{ stats?.userInfo?.name || userStore.userName }}</h2>
      <p class="welcome-subtitle">{{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</p>
    </div>

    <!-- 快捷入口 -->
    <el-card class="quick-card">
      <div class="quick-links">
        <div class="quick-link" @click="goTo('/student/profile')">
          <el-icon size="28" color="#409eff"><User /></el-icon>
          <span>个人信息</span>
        </div>
        <div class="quick-link" @click="goTo('/student/status')">
          <el-icon size="28" color="#67c23a"><Document /></el-icon>
          <span>学籍信息</span>
        </div>
        <div class="quick-link" @click="goTo('/student/award')">
          <el-icon size="28" color="#e6a23c"><Trophy /></el-icon>
          <span>奖励申请</span>
        </div>
        <div class="quick-link" @click="goTo('/student/punishment')">
          <el-icon size="28" color="#f56c6c"><Warning /></el-icon>
          <span>处分查看</span>
        </div>
        <div class="quick-link" @click="goTo('/student/appeal')">
          <el-icon size="28" color="#909399"><ChatDotRound /></el-icon>
          <span>申诉管理</span>
        </div>
        <div class="quick-link" @click="goTo('/student/status-change')">
          <el-icon size="28" color="#409eff"><Switch /></el-icon>
          <span>学籍变动</span>
        </div>
        <div class="quick-link" @click="goTo('/student/leave')">
          <el-icon size="28" color="#e6a23c"><Promotion /></el-icon>
          <span>离校手续</span>
        </div>
        <div class="quick-link" @click="goTo('/student/notice')">
          <el-icon size="28" color="#f56c6c"><Bell /></el-icon>
          <span>系统通知</span>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 个人信息卡片 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </div>
          </template>
          <div class="info-content" v-if="stats?.userInfo">
            <el-descriptions :column="2" border size="default">
              <el-descriptions-item label="学号">{{ stats.userInfo.stuId }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ stats.userInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="院系" :span="2">{{ stats.userInfo.department }}</el-descriptions-item>
              <el-descriptions-item label="专业">{{ stats.userInfo.major }}</el-descriptions-item>
              <el-descriptions-item label="班级">{{ stats.userInfo.className }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- 学籍状态卡片 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>学籍状态</span>
            </div>
          </template>
          <div class="info-content" v-if="stats?.userInfo">
            <el-descriptions :column="1" border size="default">
              <el-descriptions-item label="学籍状态">
                <el-tag :type="stats.userInfo.academicStatus === '在读' ? 'success' : 'info'">
                  {{ stats.userInfo.academicStatus }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="警示等级">
                <el-tag :type="getWarningLevelType(stats.userInfo.warningLevel)">
                  {{ stats.userInfo.warningLevel || '无' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="注册状态">
                <el-tag :type="stats.userInfo.registerStatus === '已注册' ? 'success' : 'danger'">
                  {{ stats.userInfo.registerStatus }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计数据 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goTo('/student/award')">
          <el-statistic title="奖励记录" :value="stats?.counts?.awards || 0">
            <template #prefix>
              <el-icon color="#67c23a"><Trophy /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goTo('/student/punishment')">
          <el-statistic title="处分记录" :value="stats?.counts?.punishments || 0">
            <template #prefix>
              <el-icon color="#f56c6c"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goTo('/student/appeal')">
          <el-statistic title="待处理申诉" :value="stats?.counts?.pendingAppeals || 0">
            <template #prefix>
              <el-icon color="#e6a23c"><ChatDotRound /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goTo('/student/notice')">
          <el-statistic title="系统通知" :value="stats?.counts?.notices || 0">
            <template #prefix>
              <el-icon color="#409eff"><Bell /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 最新通知 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Bell /></el-icon>
              <span>最新通知</span>
              <el-button type="primary" link size="small" @click="goTo('/student/notice')">查看全部</el-button>
            </div>
          </template>
          <el-empty v-if="!stats?.notices?.length" description="暂无通知" :image-size="80" />
          <div v-else class="notice-list">
            <div v-for="notice in stats.notices" :key="notice.noticeId" class="notice-item" @click="goTo('/student/notice')">
              <div class="notice-title">
                <el-tag v-if="notice.priority === '重要'" type="danger" size="small">重要</el-tag>
                {{ notice.title }}
              </div>
              <div class="notice-time">{{ formatTime(notice.publishTime) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<style scoped lang="less">
.dashboard-container {
  .welcome-section {
    margin-bottom: 20px;
    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #303133;
    }
    .welcome-subtitle {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: bold;
    
    .el-button {
      margin-left: auto;
    }
  }

  .info-card {
    min-height: 200px;
    margin-bottom: 20px;
    
    .info-content {
      :deep(.el-descriptions) {
        .el-descriptions__label {
          width: 90px;
          min-width: 90px;
        }
      }
    }
  }

  .stat-card {
    cursor: pointer;
    transition: transform 0.2s;
    &:hover {
      transform: translateY(-4px);
    }
  }

  .notice-list {
    .notice-item {
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      &:last-child {
        border-bottom: none;
      }
      &:hover {
        .notice-title {
          color: #409eff;
        }
      }
      .notice-title {
        color: #303133;
        margin-bottom: 4px;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .notice-time {
        font-size: 12px;
        color: #c0c4cc;
      }
    }
  }

  .quick-card {
    margin-bottom: 20px;
  }

  .quick-links {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    .quick-link {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 16px 20px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      min-width: 80px;
      &:hover {
        background: #f5f7fa;
        transform: translateY(-2px);
      }
      span {
        font-size: 13px;
        color: #606266;
        white-space: nowrap;
      }
    }
  }
}
</style>
