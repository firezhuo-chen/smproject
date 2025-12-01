<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getLogPage } from '@/api/business'
import type { Log } from '@/api/types'

const loading = ref(false)
const logList = ref<Log[]>([])
const searchKeyword = ref('')
const dateRange = ref<[string, string] | null>(null)
const filterOperation = ref('')
const detailVisible = ref(false)
const currentLog = ref<Log | null>(null)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 操作类型选项
const operationOptions = ['登录', '新增', '修改', '删除', '审批', '导出']

// 搜索过滤（前端过滤关键词和日期）
const filteredLogs = computed(() => {
  let result = logList.value
  
  // 关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(log => 
      log.userId?.toLowerCase().includes(keyword) ||
      log.userName?.toLowerCase().includes(keyword) ||
      log.operation?.toLowerCase().includes(keyword) ||
      log.operationDetail?.toLowerCase().includes(keyword)
    )
  }
  
  // 操作类型过滤
  if (filterOperation.value) {
    result = result.filter(log => log.operation === filterOperation.value)
  }
  
  // 日期过滤
  if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
    const startDate = new Date(dateRange.value[0])
    const endDate = new Date(dateRange.value[1])
    endDate.setHours(23, 59, 59, 999)
    
    result = result.filter(log => {
      const logDate = new Date(log.operationTime)
      return logDate >= startDate && logDate <= endDate
    })
  }
  
  return result
})

// 查看详情
const handleViewDetail = (row: Log) => {
  currentLog.value = row
  detailVisible.value = true
}

// 获取用户类型显示
const getUserTypeLabel = (type: string) => {
  switch (type) {
    case 'student': return '学生'
    case 'advisor': return '辅导员'
    case 'admin': return '管理员'
    default: return type || '-'
  }
}

// 解析设备信息
const parseUserAgent = (ua: string) => {
  if (!ua) return '-'
  // 简化显示
  if (ua.length > 100) {
    return ua.substring(0, 100) + '...'
  }
  return ua
}

const formatTime = (time: string) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLogPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    logList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取日志列表失败', error)
  } finally {
    loading.value = false
  }
}

// 分页变化处理
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="log-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
          <div class="header-actions">
            <el-select v-model="filterOperation" placeholder="操作类型" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="op in operationOptions" :key="op" :label="op" :value="op" />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px; margin-right: 12px;"
              clearable
            />
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户/操作/详情"
              style="width: 200px;"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table :data="filteredLogs" v-loading="loading" stripe>
        <el-table-column prop="operationTime" label="操作时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.operationTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="userName" label="用户名" width="100" />
        <el-table-column prop="userType" label="用户类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getUserTypeLabel(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.operation === '登录' ? 'success' : ''">{{ row.operation }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationDetail" label="操作详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result ? 'success' : 'danger'" size="small">
              {{ row.result ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <template v-if="currentLog">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志编号" :span="2">{{ currentLog.logId }}</el-descriptions-item>
          <el-descriptions-item label="操作时间" :span="2">{{ formatTime(currentLog.operationTime) }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentLog.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentLog.userName }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">
            <el-tag size="small" type="info">{{ getUserTypeLabel(currentLog.userType || '') }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag size="small">{{ currentLog.operation }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作结果">
            <el-tag :type="currentLog.result ? 'success' : 'danger'" size="small">
              {{ currentLog.result ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            <el-tag size="small" type="info">{{ currentLog.requestMethod || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求URL" :span="2">{{ currentLog.requestUrl || '-' }}</el-descriptions-item>
          <el-descriptions-item label="IP地址" :span="2">{{ currentLog.ipAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作详情" :span="2">{{ currentLog.operationDetail }}</el-descriptions-item>
          <el-descriptions-item label="操作对象类型">{{ currentLog.targetType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作对象ID">{{ currentLog.targetId || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentLog.errorMessage" label="错误信息" :span="2">
            <span style="color: #f56c6c;">{{ currentLog.errorMessage }}</span>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 变更内容 -->
        <template v-if="currentLog.oldValue || currentLog.newValue">
          <el-divider content-position="left">变更内容</el-divider>
          <el-row :gutter="20">
            <el-col :span="12" v-if="currentLog.oldValue">
              <div class="change-box">
                <div class="change-title">变更前</div>
                <pre class="change-content">{{ currentLog.oldValue }}</pre>
              </div>
            </el-col>
            <el-col :span="12" v-if="currentLog.newValue">
              <div class="change-box">
                <div class="change-title">变更后</div>
                <pre class="change-content">{{ currentLog.newValue }}</pre>
              </div>
            </el-col>
          </el-row>
        </template>
        
        <!-- 设备信息 -->
        <el-divider content-position="left">设备信息</el-divider>
        <div class="user-agent-box">
          {{ currentLog.userAgent || '-' }}
        </div>
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.log-container {
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
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}

.change-box {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  
  .change-title {
    background: #f5f7fa;
    padding: 8px 12px;
    font-weight: bold;
    font-size: 13px;
    color: #606266;
  }
  
  .change-content {
    padding: 12px;
    margin: 0;
    font-size: 12px;
    max-height: 200px;
    overflow: auto;
    background: #fafafa;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

.user-agent-box {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}
</style>
