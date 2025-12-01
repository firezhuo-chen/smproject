<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { request } from '@/utils/request'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const stats = ref<any>(null)

// 管理员角色权限
const adminRole = computed(() => userStore.adminRole)
const isEducationAdmin = computed(() => adminRole.value === '教务管理员')

// 获取角色对应的中文名称
const roleDisplayName = computed(() => {
  switch (adminRole.value) {
    case '宿管管理员': return '宿管管理'
    case '图书馆管理员': return '图书馆管理'
    case '财务处管理员': return '财务管理'
    default: return '系统管理'
  }
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/dashboard/admin', {
      params: {
        adminId: userStore.userId,
        role: adminRole.value
      }
    })
    stats.value = res.data
  } catch (error) {
    console.error('获取仪表盘数据失败', error)
  } finally {
    loading.value = false
  }
}

const totalPending = computed(() => {
  if (!stats.value?.counts) return 0
  const c = stats.value.counts
  return (c.pendingAwards || 0) + (c.pendingPunishments || 0) + (c.pendingAppeals || 0) + 
         (c.pendingStatusChanges || 0) + (c.pendingLeaves || 0)
})

const getWarningLevelType = (level: string) => {
  switch (level) {
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
      <h2>{{ roleDisplayName }}后台</h2>
      <p class="welcome-subtitle">欢迎回来，{{ userStore.userName }}（{{ adminRole }}） | {{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</p>
    </div>

    <!-- 快捷入口 -->
    <el-card class="quick-card">
      <div class="quick-links">
        <!-- 教务管理员快捷入口 -->
        <template v-if="isEducationAdmin">
          <div class="quick-link" @click="goTo('/admin/students')">
            <el-icon size="28" color="#409eff"><User /></el-icon>
            <span>学生管理</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/award-review')">
            <el-icon size="28" color="#e6a23c"><Trophy /></el-icon>
            <span>奖励终审</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/punishment')">
            <el-icon size="28" color="#f56c6c"><Warning /></el-icon>
            <span>处分审批</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/appeal-review')">
            <el-icon size="28" color="#909399"><ChatDotRound /></el-icon>
            <span>申诉终审</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/status-change-review')">
            <el-icon size="28" color="#67c23a"><Switch /></el-icon>
            <span>学籍变动终审</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/leave-review')">
            <el-icon size="28" color="#409eff"><Promotion /></el-icon>
            <span>离校审核</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/notice')">
            <el-icon size="28" color="#e6a23c"><Bell /></el-icon>
            <span>通知管理</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/log')">
            <el-icon size="28" color="#909399"><Notebook /></el-icon>
            <span>操作日志</span>
          </div>
        </template>
        <!-- 其他管理员快捷入口 -->
        <template v-else>
          <div class="quick-link" @click="goTo('/admin/punishment')">
            <el-icon size="28" color="#f56c6c"><Warning /></el-icon>
            <span>处分申请</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/leave-review')">
            <el-icon size="28" color="#409eff"><Promotion /></el-icon>
            <span>离校审核</span>
          </div>
          <div class="quick-link" @click="goTo('/admin/user-center')">
            <el-icon size="28" color="#909399"><Setting /></el-icon>
            <span>个人中心</span>
          </div>
        </template>
      </div>
    </el-card>

    <!-- 系统概览 - 仅教务管理员可见 -->
    <el-row v-if="isEducationAdmin" :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="goTo('/admin/students')">
          <el-statistic title="学生总数" :value="stats?.counts?.totalStudents || 0">
            <template #prefix>
              <el-icon color="#409eff"><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <el-statistic title="辅导员数" :value="stats?.counts?.totalAdvisors || 0">
            <template #prefix>
              <el-icon color="#67c23a"><UserFilled /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <el-statistic title="管理员数" :value="stats?.counts?.totalAdmins || 0">
            <template #prefix>
              <el-icon color="#909399"><Setting /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-danger" shadow="hover">
          <el-statistic title="预警学生" :value="stats?.counts?.warningStudents || 0">
            <template #prefix>
              <el-icon color="#f56c6c"><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待审批统计 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span>待处理事项</span>
          <el-tag v-if="totalPending > 0" type="danger">{{ totalPending }} 条待处理</el-tag>
        </div>
      </template>
      <el-row :gutter="20">
        <!-- 教务管理员显示全部待处理项 -->
        <template v-if="isEducationAdmin">
          <el-col :span="4">
            <div class="pending-item" @click="goTo('/admin/award-review')">
              <div class="pending-count" :class="{ 'has-pending': stats?.counts?.pendingAwards > 0 }">
                {{ stats?.counts?.pendingAwards || 0 }}
              </div>
              <div class="pending-label">待终审奖励</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="pending-item" @click="goTo('/admin/punishment')">
              <div class="pending-count" :class="{ 'has-pending': stats?.counts?.pendingPunishments > 0 }">
                {{ stats?.counts?.pendingPunishments || 0 }}
              </div>
              <div class="pending-label">待审批处分</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="pending-item" @click="goTo('/admin/appeal-review')">
              <div class="pending-count" :class="{ 'has-pending': stats?.counts?.pendingAppeals > 0 }">
                {{ stats?.counts?.pendingAppeals || 0 }}
              </div>
              <div class="pending-label">待终审申诉</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="pending-item" @click="goTo('/admin/status-change-review')">
              <div class="pending-count" :class="{ 'has-pending': stats?.counts?.pendingStatusChanges > 0 }">
                {{ stats?.counts?.pendingStatusChanges || 0 }}
              </div>
              <div class="pending-label">待审学籍变动</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="pending-item" @click="goTo('/admin/leave-review')">
              <div class="pending-count" :class="{ 'has-pending': stats?.counts?.pendingLeaves > 0 }">
                {{ stats?.counts?.pendingLeaves || 0 }}
              </div>
              <div class="pending-label">待审离校</div>
            </div>
          </el-col>
        </template>
        <!-- 其他管理员只显示离校审核和处分申请 -->
        <template v-else>
          <el-col :span="8">
            <div class="pending-item" @click="goTo('/admin/leave-review')">
              <div class="pending-count" :class="{ 'has-pending': stats?.counts?.pendingLeaves > 0 }">
                {{ stats?.counts?.pendingLeaves || 0 }}
              </div>
              <div class="pending-label">待审离校手续</div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="pending-item" @click="goTo('/admin/punishment')">
              <div class="pending-count">
                {{ stats?.counts?.myPunishmentApplications || 0 }}
              </div>
              <div class="pending-label">我的处分申请</div>
            </div>
          </el-col>
        </template>
      </el-row>
    </el-card>

    <!-- 教务管理员显示待办列表和预警学生 -->
    <el-row v-if="isEducationAdmin" :gutter="20" style="margin-top: 20px;">
      <!-- 待办事项列表 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>待处理列表</span>
            </div>
          </template>
          <el-empty v-if="!stats?.todoList?.length" description="暂无待处理事项" :image-size="80" />
          <div v-else class="todo-list">
            <div v-for="item in stats.todoList" :key="item.id" class="todo-item" @click="goTo(item.link)">
              <div class="todo-icon">
                <el-icon v-if="item.type === 'award'" color="#67c23a"><Trophy /></el-icon>
                <el-icon v-else-if="item.type === 'punishment'" color="#f56c6c"><Warning /></el-icon>
                <el-icon v-else color="#909399"><Document /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-desc">{{ item.description }}</div>
              </div>
              <div class="todo-time">{{ item.time }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 预警学生 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon><Warning /></el-icon>
              <span>预警学生</span>
              <el-button type="primary" link size="small" @click="goTo('/admin/students')">查看全部</el-button>
            </div>
          </template>
          <el-empty v-if="!stats?.warningStudents?.length" description="暂无预警学生" :image-size="80" />
          <el-table v-else :data="stats.warningStudents" size="small" max-height="250">
            <el-table-column prop="stuId" label="学号" width="110" />
            <el-table-column prop="name" label="姓名" width="80" />
            <el-table-column prop="department" label="院系" show-overflow-tooltip />
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

    <!-- 最近操作日志 - 仅教务管理员可见 -->
    <el-card v-if="isEducationAdmin" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>最近操作日志</span>
          <el-button type="primary" link size="small" @click="goTo('/admin/log')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="stats?.logs || []" size="small">
        <el-table-column prop="userName" label="操作人" width="100" />
        <el-table-column prop="operation" label="操作类型" width="80" />
        <el-table-column prop="operationDetail" label="操作详情" show-overflow-tooltip />
        <el-table-column prop="operationTime" label="操作时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.operationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result ? 'success' : 'danger'" size="small">
              {{ row.result ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
    
    .el-button {
      margin-left: auto;
    }
  }

  .stat-card {
    cursor: pointer;
    transition: transform 0.2s;
    &:hover {
      transform: translateY(-4px);
    }
    &.stat-danger {
      :deep(.el-statistic__number) {
        color: #f56c6c;
      }
    }
  }

  .pending-item {
    text-align: center;
    padding: 16px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.2s;
    &:hover {
      background: #f5f7fa;
    }
    .pending-count {
      font-size: 32px;
      font-weight: bold;
      color: #909399;
      &.has-pending {
        color: #e6a23c;
      }
    }
    .pending-label {
      font-size: 14px;
      color: #606266;
      margin-top: 8px;
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
