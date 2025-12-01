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
    const res = await request.get(`/dashboard/advisor/${userStore.userId}`)
    stats.value = res.data
  } catch (error) {
    console.error('获取仪表盘数据失败', error)
  } finally {
    loading.value = false
  }
}

const getWarningLevelType = (level: string) => {
  switch (level) {
    case '一级': return 'warning'
    case '二级': return 'danger'
    case '三级': return 'danger'
    default: return 'info'
  }
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
      <h2>欢迎回来，{{ stats?.userInfo?.userName || userStore.userName }} 老师</h2>
      <p class="welcome-subtitle">{{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</p>
    </div>

    <!-- 快捷入口 -->
    <el-card class="quick-card">
      <div class="quick-links">
        <div class="quick-link" @click="goTo('/advisor/students')">
          <el-icon size="28" color="#409eff"><User /></el-icon>
          <span>学生列表</span>
        </div>
        <div class="quick-link" @click="goTo('/advisor/award-review')">
          <el-icon size="28" color="#e6a23c"><Trophy /></el-icon>
          <span>奖励审批</span>
        </div>
        <div class="quick-link" @click="goTo('/advisor/appeal-review')">
          <el-icon size="28" color="#909399"><ChatDotRound /></el-icon>
          <span>申诉审理</span>
        </div>
        <div class="quick-link" @click="goTo('/advisor/status-change-review')">
          <el-icon size="28" color="#67c23a"><Switch /></el-icon>
          <span>学籍变动审核</span>
        </div>
        <div class="quick-link" @click="goTo('/advisor/notice')">
          <el-icon size="28" color="#f56c6c"><Bell /></el-icon>
          <span>系统通知</span>
        </div>
      </div>
    </el-card>

    <!-- 统计数据 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goTo('/advisor/students')">
          <el-statistic title="负责学生" :value="stats?.counts?.totalStudents || 0">
            <template #prefix>
              <el-icon color="#409eff"><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-warning" shadow="hover" @click="goTo('/advisor/award-review')">
          <el-statistic title="待审批奖励" :value="stats?.counts?.pendingAwards || 0">
            <template #prefix>
              <el-icon color="#67c23a"><Trophy /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-warning" shadow="hover" @click="goTo('/advisor/appeal-review')">
          <el-statistic title="待审理申诉" :value="stats?.counts?.pendingAppeals || 0">
            <template #prefix>
              <el-icon color="#e6a23c"><ChatDotRound /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goTo('/advisor/students')">
          <el-statistic title="预警学生" :value="stats?.counts?.warningStudents || 0">
            <template #prefix>
              <el-icon color="#f56c6c"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 待办事项 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>待处理事项</span>
              <el-tag v-if="stats?.todoList?.length" type="danger" size="small">
                {{ stats.todoList.length }} 条待处理
              </el-tag>
            </div>
          </template>
          <el-empty v-if="!stats?.todoList?.length" description="暂无待处理事项" :image-size="80" />
          <div v-else class="todo-list">
            <div v-for="item in stats.todoList" :key="item.id" class="todo-item" @click="goTo(item.link)">
              <div class="todo-icon">
                <el-icon v-if="item.type === 'award'" color="#67c23a"><Trophy /></el-icon>
                <el-icon v-else-if="item.type === 'appeal'" color="#e6a23c"><ChatDotRound /></el-icon>
                <el-icon v-else color="#909399"><Document /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-desc">{{ item.description }}</div>
              </div>
              <div class="todo-time">{{ item.time }}</div>
              <el-button type="primary" size="small">去处理</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 预警学生 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Warning /></el-icon>
              <span>预警学生</span>
            </div>
          </template>
          <el-empty v-if="!stats?.warningStudents?.length" description="暂无预警学生" :image-size="80" />
          <el-table v-else :data="stats.warningStudents" size="small">
            <el-table-column prop="stuId" label="学号" width="110" />
            <el-table-column prop="name" label="姓名" width="80" />
            <el-table-column prop="className" label="班级" />
            <el-table-column prop="warningLevel" label="警示" width="70">
              <template #default="{ row }">
                <el-tag :type="getWarningLevelType(row.warningLevel)" size="small">
                  {{ row.warningLevel }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<style scoped lang="less">
.dashboard-container {
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
  }

  .stat-card {
    cursor: pointer;
    transition: transform 0.2s;
    &:hover {
      transform: translateY(-4px);
    }
    &.stat-warning {
      :deep(.el-statistic__number) {
        color: #e6a23c;
      }
    }
  }

  .todo-list {
    .todo-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.2s;
      &:hover {
        background: #f5f7fa;
      }
      .todo-icon {
        margin-right: 12px;
      }
      .todo-content {
        flex: 1;
        .todo-title {
          font-weight: 500;
          color: #303133;
        }
        .todo-desc {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }
      .todo-time {
        font-size: 12px;
        color: #c0c4cc;
        margin-right: 12px;
      }
    }
  }

  .quick-links {
    display: flex;
    gap: 32px;
    .quick-link {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      padding: 20px 32px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      &:hover {
        background: #f5f7fa;
        transform: translateY(-2px);
      }
      span {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}
</style>
