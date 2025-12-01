<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getStatusChangeList, updateStatusChange } from '@/api/business'
import { getStudentStatusInfoList } from '@/api/student'
import type { StatusChange, StudentStatusInfo } from '@/api/types'
import { ElMessage } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const userStore = useUserStore()
const loading = ref(false)
const statusChangeList = ref<StatusChange[]>([])
const statusList = ref<StudentStatusInfo[]>([])
const searchKeyword = ref('')
const filterType = ref('')
const activeTab = ref('pending')
const opinionDialogVisible = ref(false)
const detailVisible = ref(false)
const currentRow = ref<StatusChange | null>(null)
const currentChange = ref<StatusChange | null>(null)
const opinion = ref('')
const isApprove = ref(true)

const changeTypes = ['转专业', '交换', '退学', '休学', '复学', '转学']

// 获取辅导员负责的学生ID列表
const myStudentIds = computed(() => {
  return statusList.value
    .filter(s => s.advisorId === userStore.userId)
    .map(s => s.stuId)
})

// 搜索过滤
const filterList = (list: StatusChange[]) => {
  let result = list
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(c =>
      c.stuId?.toLowerCase().includes(keyword) ||
      c.changeReason?.toLowerCase().includes(keyword)
    )
  }
  if (filterType.value) {
    result = result.filter(c => c.changeType === filterType.value)
  }
  return result
}

// 待审核的学籍变动（辅导员待审核）
const pendingChanges = computed(() => {
  return filterList(statusChangeList.value.filter(c => 
    myStudentIds.value.includes(c.stuId) && 
    c.advisorStatus === '待审核'
  ))
})

// 已处理的学籍变动
const processedChanges = computed(() => {
  return filterList(statusChangeList.value.filter(c => 
    myStudentIds.value.includes(c.stuId) && 
    c.advisorStatus !== '待审核'
  ))
})

const getStatusType = (status: string) => {
  switch (status) {
    case '已通过': return 'success'
    case '未通过': return 'danger'
    case '待审核': return 'warning'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const [changeRes, statusRes] = await Promise.all([
      getStatusChangeList(),
      getStudentStatusInfoList()
    ])
    statusChangeList.value = changeRes.data || []
    statusList.value = statusRes.data || []
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

// 打开审核对话框
const openOpinionDialog = (row: StatusChange, approve: boolean) => {
  currentRow.value = row
  isApprove.value = approve
  opinion.value = ''
  opinionDialogVisible.value = true
}

// 提交审核
const submitReview = async () => {
  if (!currentRow.value) return
  
  // 拒绝时必须填写意见
  if (!isApprove.value && !opinion.value.trim()) {
    ElMessage.warning('请填写拒绝理由')
    return
  }
  
  try {
    const updated: StatusChange = {
      ...currentRow.value,
      advisorId: userStore.userId,
      advisorStatus: isApprove.value ? '已通过' : '未通过',
      advisorOpinion: opinion.value || (isApprove.value ? '同意' : '')
    }
    
    // 如果辅导员拒绝，最终状态也设为未通过
    if (!isApprove.value) {
      updated.applyStatus = '未通过'
    }
    
    await updateStatusChange(updated)
    ElMessage.success(isApprove.value ? '审核通过' : '已拒绝')
    opinionDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleViewDetail = (row: StatusChange) => {
  currentChange.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="status-change-review-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学籍变动审核</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="变动类型" clearable style="width: 120px; margin-right: 12px;">
              <el-option v-for="t in changeTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索学号/原因" clearable style="width: 160px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-tag type="danger" v-if="pendingChanges.length > 0">
              {{ pendingChanges.length }} 条待审核
            </el-tag>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审核" name="pending">
          <el-empty v-if="pendingChanges.length === 0" description="暂无待审核的学籍变动申请" />
          <el-table v-else :data="pendingChanges" v-loading="loading" stripe>
            <el-table-column prop="changeId" label="变动编号" width="140" />
            <el-table-column prop="stuId" label="学号" width="120" />
            <el-table-column prop="changeType" label="变动类型" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.changeType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="changeReason" label="变动原因" min-width="150" show-overflow-tooltip />
            <el-table-column prop="currentMajor" label="当前专业" min-width="140" show-overflow-tooltip />
            <el-table-column prop="targetMajor" label="目标专业" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.targetMajor || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="applyDate" label="申请日期" width="110" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="handleViewDetail(row)">
                  详情
                </el-button>
                <el-button type="success" size="small" link @click="openOpinionDialog(row, true)">
                  通过
                </el-button>
                <el-button type="danger" size="small" link @click="openOpinionDialog(row, false)">
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已处理" name="processed">
          <el-empty v-if="processedChanges.length === 0" description="暂无已处理的学籍变动申请" />
          <el-table v-else :data="processedChanges" v-loading="loading" stripe>
            <el-table-column prop="changeId" label="变动编号" width="140" />
            <el-table-column prop="stuId" label="学号" width="120" />
            <el-table-column prop="changeType" label="变动类型" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.changeType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="changeReason" label="变动原因" min-width="150" show-overflow-tooltip />
            <el-table-column prop="currentMajor" label="当前专业" min-width="140" show-overflow-tooltip />
            <el-table-column prop="targetMajor" label="目标专业" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.targetMajor || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="applyDate" label="申请日期" width="110" />
            <el-table-column prop="advisorStatus" label="我的审核" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.advisorStatus)" size="small">
                  {{ row.advisorStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="advisorOpinion" label="审核意见" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.advisorOpinion || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="applyStatus" label="最终状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.applyStatus)">
                  {{ row.applyStatus }}
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
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 审核意见对话框 -->
    <el-dialog v-model="opinionDialogVisible" :title="isApprove ? '通过审核' : '拒绝申请'" width="450px">
      <el-form label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="opinion"
            type="textarea"
            :rows="4"
            :placeholder="isApprove ? '请输入审核意见（可选）' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="opinionDialogVisible = false">取消</el-button>
        <el-button :type="isApprove ? 'success' : 'danger'" @click="submitReview">
          {{ isApprove ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="学籍变动详情" width="650px">
      <template v-if="currentChange">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="变动编号">{{ currentChange.changeId }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentChange.stuId }}</el-descriptions-item>
          <el-descriptions-item label="变动类型">
            <el-tag>{{ currentChange.changeType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ currentChange.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="当前学校">{{ currentChange.currentSchool }}</el-descriptions-item>
          <el-descriptions-item label="当前学院">{{ currentChange.currentCollege }}</el-descriptions-item>
          <el-descriptions-item label="当前专业">{{ currentChange.currentMajor }}</el-descriptions-item>
          <el-descriptions-item label="目标学校">{{ currentChange.targetSchool || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目标学院">{{ currentChange.targetCollege || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目标专业">{{ currentChange.targetMajor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="我的审核">
            <el-tag :type="getStatusType(currentChange.advisorStatus)" size="small">
              {{ currentChange.advisorStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="教务审核">
            <el-tag :type="getStatusType(currentChange.adminStatus)" size="small">
              {{ currentChange.adminStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最终状态" :span="2">
            <el-tag :type="getStatusType(currentChange.applyStatus)">
              {{ currentChange.applyStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="变动原因" :span="2">{{ currentChange.changeReason }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">证明材料</el-divider>
        <AttachmentUpload
          :related-id="currentChange.changeId"
          related-type="statusChange"
          :readonly="true"
        />
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.status-change-review-container {
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
</style>
