<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getLeaveSchoolList, updateLeaveSchool } from '@/api/business'
import type { LeaveSchool } from '@/api/types'
import { ElMessage } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'
import { confirmApprove, confirmReject } from '@/utils/confirm'

const userStore = useUserStore()
const loading = ref(false)
const leaveList = ref<LeaveSchool[]>([])
const activeTab = ref('pending')
const detailVisible = ref(false)
const currentLeave = ref<LeaveSchool | null>(null)
const searchKeyword = ref('')
const filterType = ref('')
const filterStatus = ref('')

const leaveTypes = ['毕业', '退学', '休学', '转学', '交换']
const statusOptions = ['审核中', '已通过', '未通过']

// 审核字段名称映射
const fieldNames = {
  dormitoryStatus: '宿管审核',
  libraryStatus: '图书馆审核',
  financeStatus: '财务处审核',
  adminStatus: '教务处审核'
} as const

const reviewerFields = {
  dormitoryStatus: 'dormitoryReviewerId',
  libraryStatus: 'libraryReviewerId',
  financeStatus: 'financeReviewerId',
  adminStatus: 'adminId'
} as const

type ReviewField = keyof typeof fieldNames

// 管理员角色权限
const adminRole = computed(() => userStore.adminRole)
const isEducationAdmin = computed(() => adminRole.value === '教务管理员')

// 根据角色判断是否可以审批某个环节（每个管理员只能审批自己职能范围内的环节）
const canReview = (field: string) => {
  switch (adminRole.value) {
    case '宿管管理员': return field === 'dormitoryStatus'
    case '图书馆管理员': return field === 'libraryStatus'
    case '财务处管理员': return field === 'financeStatus'
    case '教务管理员': return field === 'adminStatus'
    default: return false
  }
}

// 搜索过滤
const filterList = (list: LeaveSchool[]) => {
  let result = list
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(l =>
      l.stuId?.toLowerCase().includes(keyword) ||
      l.leaveReason?.toLowerCase().includes(keyword)
    )
  }
  if (filterType.value) {
    result = result.filter(l => l.leaveType === filterType.value)
  }
  if (filterStatus.value) {
    result = result.filter(l => l.overallStatus === filterStatus.value)
  }
  return result
}

// 待审核的离校申请
const pendingLeaves = computed(() => {
  return filterList(leaveList.value.filter(l => l.overallStatus === '审核中'))
})

// 已处理的离校申请
const processedLeaves = computed(() => {
  return filterList(leaveList.value.filter(l => l.overallStatus !== '审核中'))
})

const getStatusType = (status: string) => {
  switch (status) {
    case '已通过': return 'success'
    case '未通过': return 'danger'
    case '待审核':
    case '审核中': return 'warning'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLeaveSchoolList()
    leaveList.value = res.data || []
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

// 审核某个环节
const handleReview = async (row: LeaveSchool, field: ReviewField, approve: boolean) => {
  const confirmed = approve 
    ? await confirmApprove(fieldNames[field])
    : await confirmReject(fieldNames[field])
  if (!confirmed) return
  
  try {
    const updated: any = { ...row }
    updated[field] = approve ? '已通过' : '未通过'
    updated[reviewerFields[field]] = userStore.userId
    
    // 检查是否所有环节都通过
    const allPassed = 
      (field === 'dormitoryStatus' ? (approve ? '已通过' : updated.dormitoryStatus) : updated.dormitoryStatus) === '已通过' &&
      (field === 'libraryStatus' ? (approve ? '已通过' : updated.libraryStatus) : updated.libraryStatus) === '已通过' &&
      (field === 'financeStatus' ? (approve ? '已通过' : updated.financeStatus) : updated.financeStatus) === '已通过' &&
      (field === 'adminStatus' ? (approve ? '已通过' : updated.adminStatus) : updated.adminStatus) === '已通过'
    
    // 检查是否有任何环节被拒绝
    const anyRejected = 
      updated.dormitoryStatus === '未通过' ||
      updated.libraryStatus === '未通过' ||
      updated.financeStatus === '未通过' ||
      updated.adminStatus === '未通过'
    
    if (allPassed) {
      updated.overallStatus = '已通过'
    } else if (anyRejected) {
      updated.overallStatus = '未通过'
    }
    
    await updateLeaveSchool(updated)
    ElMessage.success(approve ? '审核通过' : '已拒绝')
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleViewDetail = (row: LeaveSchool) => {
  currentLeave.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="leave-review-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>离校审核</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="离校类型" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="t in leaveTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索学号/原因" clearable style="width: 150px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-tag type="danger" v-if="pendingLeaves.length > 0">
              {{ pendingLeaves.length }} 条待审核
            </el-tag>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审核" name="pending">
          <el-empty v-if="pendingLeaves.length === 0" description="暂无待审核的离校申请" />
          <el-table v-else :data="pendingLeaves" v-loading="loading" stripe>
            <el-table-column prop="leaveId" label="离校编号" width="140" />
            <el-table-column prop="stuId" label="学号" width="120" />
            <el-table-column prop="leaveType" label="离校类型" width="90">
              <template #default="{ row }">
                <el-tag>{{ row.leaveType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="leaveReason" label="离校原因" min-width="120" show-overflow-tooltip />
            <el-table-column prop="leaveDate" label="离校日期" width="120" />
            <el-table-column label="宿管" width="140">
              <template #default="{ row }">
                <template v-if="row.dormitoryStatus === '待审核' && canReview('dormitoryStatus')">
                  <el-button type="success" size="small" link @click="handleReview(row, 'dormitoryStatus', true)">通过</el-button>
                  <el-button type="danger" size="small" link @click="handleReview(row, 'dormitoryStatus', false)">拒绝</el-button>
                </template>
                <el-tag v-else :type="getStatusType(row.dormitoryStatus)" size="small">
                  {{ row.dormitoryStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="图书馆" width="140">
              <template #default="{ row }">
                <template v-if="row.libraryStatus === '待审核' && canReview('libraryStatus')">
                  <el-button type="success" size="small" link @click="handleReview(row, 'libraryStatus', true)">通过</el-button>
                  <el-button type="danger" size="small" link @click="handleReview(row, 'libraryStatus', false)">拒绝</el-button>
                </template>
                <el-tag v-else :type="getStatusType(row.libraryStatus)" size="small">
                  {{ row.libraryStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="财务处" width="140">
              <template #default="{ row }">
                <template v-if="row.financeStatus === '待审核' && canReview('financeStatus')">
                  <el-button type="success" size="small" link @click="handleReview(row, 'financeStatus', true)">通过</el-button>
                  <el-button type="danger" size="small" link @click="handleReview(row, 'financeStatus', false)">拒绝</el-button>
                </template>
                <el-tag v-else :type="getStatusType(row.financeStatus)" size="small">
                  {{ row.financeStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="教务处" width="140">
              <template #default="{ row }">
                <template v-if="row.adminStatus === '待审核' && canReview('adminStatus')">
                  <el-button type="success" size="small" link @click="handleReview(row, 'adminStatus', true)">通过</el-button>
                  <el-button type="danger" size="small" link @click="handleReview(row, 'adminStatus', false)">拒绝</el-button>
                </template>
                <el-tag v-else :type="getStatusType(row.adminStatus)" size="small">
                  {{ row.adminStatus }}
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

        <el-tab-pane label="已处理" name="processed">
          <el-empty v-if="processedLeaves.length === 0" description="暂无已处理的离校申请" />
          <el-table v-else :data="processedLeaves" v-loading="loading" stripe>
            <el-table-column prop="leaveId" label="离校编号" width="140" />
            <el-table-column prop="stuId" label="学号" width="120" />
            <el-table-column prop="leaveType" label="离校类型" width="90">
              <template #default="{ row }">
                <el-tag>{{ row.leaveType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="leaveReason" label="离校原因" min-width="150" show-overflow-tooltip />
            <el-table-column prop="leaveDate" label="离校日期" width="120" />
            <el-table-column prop="dormitoryStatus" label="宿管" width="90">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.dormitoryStatus)" size="small">
                  {{ row.dormitoryStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="libraryStatus" label="图书馆" width="90">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.libraryStatus)" size="small">
                  {{ row.libraryStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="financeStatus" label="财务处" width="90">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.financeStatus)" size="small">
                  {{ row.financeStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="adminStatus" label="教务处" width="90">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.adminStatus)" size="small">
                  {{ row.adminStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="overallStatus" label="整体状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.overallStatus)">
                  {{ row.overallStatus }}
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="离校详情" width="600px">
      <template v-if="currentLeave">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="离校编号">{{ currentLeave.leaveId }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentLeave.stuId }}</el-descriptions-item>
          <el-descriptions-item label="离校类型">
            <el-tag>{{ currentLeave.leaveType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="离校日期">{{ currentLeave.leaveDate }}</el-descriptions-item>
          <el-descriptions-item label="宿管审核">
            <el-tag :type="getStatusType(currentLeave.dormitoryStatus)" size="small">
              {{ currentLeave.dormitoryStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="图书馆审核">
            <el-tag :type="getStatusType(currentLeave.libraryStatus)" size="small">
              {{ currentLeave.libraryStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="财务处审核">
            <el-tag :type="getStatusType(currentLeave.financeStatus)" size="small">
              {{ currentLeave.financeStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="教务处审核">
            <el-tag :type="getStatusType(currentLeave.adminStatus)" size="small">
              {{ currentLeave.adminStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="整体状态" :span="2">
            <el-tag :type="getStatusType(currentLeave.overallStatus)">
              {{ currentLeave.overallStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="离校原因" :span="2">{{ currentLeave.leaveReason }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">证明材料</el-divider>
        <AttachmentUpload
          :related-id="currentLeave.leaveId"
          related-type="leaveSchool"
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
.leave-review-container {
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
