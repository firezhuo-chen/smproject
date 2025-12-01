<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getNoticeList, markNoticeAsRead, markAllNoticesAsRead } from '@/api/business'
import type { Notice } from '@/api/types'

const userStore = useUserStore()
const loading = ref(false)
const noticeList = ref<Notice[]>([])
const selectedNotice = ref<Notice | null>(null)
const detailVisible = ref(false)
const searchKeyword = ref('')
const filterType = ref('')
const filterRead = ref('')

const noticeTypes = ['系统通知', '学业通知', '奖励通知', '处分通知', '其他']
const readOptions = [{ label: '未读', value: 'unread' }, { label: '已读', value: 'read' }]

// 只显示发给当前学生的通知
const myNotices = computed(() => {
  let result = noticeList.value.filter(item => item.targetUser === userStore.userId)
  if (searchKeyword.value) {
    result = result.filter(n =>
      n.title?.includes(searchKeyword.value) ||
      n.content?.includes(searchKeyword.value)
    )
  }
  if (filterType.value) {
    result = result.filter(n => n.noticeType === filterType.value)
  }
  if (filterRead.value === 'unread') {
    result = result.filter(n => !n.isRead)
  } else if (filterRead.value === 'read') {
    result = result.filter(n => n.isRead)
  }
  return result
})

// 未读通知数量
const unreadCount = computed(() => {
  return noticeList.value.filter(n => n.targetUser === userStore.userId && !n.isRead).length
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getNoticeList()
    noticeList.value = res.data || []
  } catch (error) {
    console.error('获取通知列表失败', error)
  } finally {
    loading.value = false
  }
}

const showDetail = async (notice: Notice) => {
  selectedNotice.value = notice
  detailVisible.value = true
  
  // 如果未读，标记为已读
  if (!notice.isRead) {
    try {
      await markNoticeAsRead(notice.noticeId)
      notice.isRead = true
      notice.readTime = new Date().toISOString()
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
}

// 全部标记为已读
const handleMarkAllRead = async () => {
  try {
    await markAllNoticesAsRead(userStore.userId)
    noticeList.value.forEach(n => {
      if (n.targetUser === userStore.userId && !n.isRead) {
        n.isRead = true
        n.readTime = new Date().toISOString()
      }
    })
  } catch (error) {
    console.error('批量标记已读失败', error)
  }
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="notice-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统通知</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="通知类型" clearable style="width: 120px; margin-right: 12px;">
              <el-option v-for="t in noticeTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="filterRead" placeholder="读取状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="r in readOptions" :key="r.value" :label="r.label" :value="r.value" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索标题/内容" clearable style="width: 150px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button v-if="unreadCount > 0" type="primary" size="small" link @click="handleMarkAllRead">
              全部标记已读
            </el-button>
            <el-tag :type="unreadCount > 0 ? 'danger' : 'info'" style="margin-left: 8px;">
              {{ unreadCount > 0 ? `${unreadCount} 条未读` : `共 ${myNotices.length} 条` }}
            </el-tag>
          </div>
        </div>
      </template>

      <el-empty v-if="myNotices.length === 0 && !loading" description="暂无通知" />

      <div v-else class="notice-list" v-loading="loading">
        <div
          v-for="notice in myNotices"
          :key="notice.noticeId"
          class="notice-item"
          :class="{ 'unread': !notice.isRead }"
          @click="showDetail(notice)"
        >
          <div class="notice-header">
            <div class="notice-title">
              <el-badge v-if="!notice.isRead" is-dot style="margin-right: 8px;" />
              <el-tag
                v-if="notice.priority === '重要'"
                type="danger"
                size="small"
                style="margin-right: 8px;"
              >
                重要
              </el-tag>
              <span :style="{ fontWeight: notice.isRead ? 'normal' : 'bold' }">{{ notice.title }}</span>
            </div>
            <span class="notice-time">{{ formatTime(notice.publishTime) }}</span>
          </div>
          <div class="notice-content">
            {{ notice.content.substring(0, 100) }}{{ notice.content.length > 100 ? '...' : '' }}
          </div>
          <div class="notice-footer">
            <span class="notice-type">
              <el-tag size="small" type="info">{{ notice.noticeType }}</el-tag>
            </span>
            <span class="notice-publisher">发布人：{{ notice.publishUser }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog v-model="detailVisible" title="通知详情" width="600px">
      <template v-if="selectedNotice">
        <div class="detail-header">
          <h3>
            <el-tag
              v-if="selectedNotice.priority === '重要'"
              type="danger"
              size="small"
              style="margin-right: 8px;"
            >
              重要
            </el-tag>
            {{ selectedNotice.title }}
          </h3>
          <div class="detail-meta">
            <span>
              <el-icon><Clock /></el-icon>
              {{ formatTime(selectedNotice.publishTime) }}
            </span>
            <span>
              <el-icon><User /></el-icon>
              {{ selectedNotice.publishUser }}
            </span>
            <el-tag size="small" type="info">{{ selectedNotice.noticeType }}</el-tag>
          </div>
        </div>
        <el-divider />
        <div class="detail-content">
          {{ selectedNotice.content }}
        </div>
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.notice-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: bold;
  }
  .header-actions {
    display: flex;
    align-items: center;
  }
}

.notice-list {
  .notice-item {
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
    }
    
    &:last-child {
      margin-bottom: 0;
    }
    
    &.unread {
      background-color: #f0f9ff;
      border-color: #b3d8ff;
    }
  }
  
  .notice-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }
  
  .notice-title {
    font-size: 15px;
    font-weight: 500;
    color: #303133;
  }
  
  .notice-time {
    font-size: 12px;
    color: #909399;
  }
  
  .notice-content {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 8px;
  }
  
  .notice-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #909399;
  }
}

.detail-header {
  h3 {
    margin: 0 0 12px 0;
    font-size: 18px;
    color: #303133;
  }
  
  .detail-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 13px;
    color: #909399;
    
    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

.detail-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
