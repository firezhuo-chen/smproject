<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getLeaveSchoolList, addLeaveSchool, deleteLeaveSchool } from '@/api/business'
import type { LeaveSchool } from '@/api/types'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'
import { confirmRevoke } from '@/utils/confirm'

const userStore = useUserStore()
const loading = ref(false)
const leaveList = ref<LeaveSchool[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref<FormInstance>()
const currentLeave = ref<LeaveSchool | null>(null)
const searchKeyword = ref('')
const filterStatus = ref('')
const filterType = ref('')

const statusOptions = ['审核中', '已通过', '未通过']

// 只显示当前学生的离校记录
const myLeaves = computed(() => {
  let result = leaveList.value.filter(item => item.stuId === userStore.userId)
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(l =>
      l.leaveReason?.toLowerCase().includes(keyword)
    )
  }
  if (filterStatus.value) {
    result = result.filter(l => l.overallStatus === filterStatus.value)
  }
  if (filterType.value) {
    result = result.filter(l => l.leaveType === filterType.value)
  }
  return result
})

const leaveTypes = ['毕业', '退学', '休学', '转学', '交换']

// 生成离校编号
const generateLeaveId = () => {
  const date = new Date()
  const dateStr = date.toISOString().split('T')[0]!.replace(/-/g, '')
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  return `L${dateStr}${random}`
}

const defaultForm = (): Partial<LeaveSchool> => ({
  leaveId: '',
  stuId: userStore.userId,
  leaveType: undefined,
  leaveReason: '',
  applyDate: new Date().toISOString().split('T')[0],
  leaveDate: '',
  dormitoryStatus: '待审核',
  libraryStatus: '待审核',
  financeStatus: '待审核',
  adminStatus: '待审核',
  overallStatus: '审核中'
})

const form = ref<Partial<LeaveSchool>>(defaultForm())

const rules = ref<FormRules>({
  leaveType: [{ required: true, message: '请选择离校类型', trigger: 'change' }],
  leaveReason: [{ required: true, message: '请输入离校原因', trigger: 'blur' }],
  leaveDate: [{ required: true, message: '请选择离校日期', trigger: 'change' }]
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
    console.error('获取离校列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  // 检查是否有进行中的离校申请
  const pending = myLeaves.value.find(l => l.overallStatus === '审核中')
  if (pending) {
    ElMessage.warning('您已有进行中的离校申请，请等待审核完成')
    return
  }
  
  form.value = defaultForm()
  form.value.leaveId = generateLeaveId()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    await addLeaveSchool(form.value as LeaveSchool)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

// 撤回申请
const handleRevoke = async (row: LeaveSchool) => {
  const confirmed = await confirmRevoke('离校申请')
  if (!confirmed) return
  
  try {
    await deleteLeaveSchool(row.leaveId)
    ElMessage.success('申请已撤回')
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '撤回失败')
  }
}

// 是否可以撤回（所有审核状态都是待审核时才能撤回）
const canRevoke = (row: LeaveSchool) => {
  return row.dormitoryStatus === '待审核' &&
         row.libraryStatus === '待审核' &&
         row.financeStatus === '待审核' &&
         row.adminStatus === '待审核' &&
         row.overallStatus === '审核中'
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
  <div class="leave-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>离校手续办理</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="离校类型" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="t in leaveTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索原因" clearable style="width: 140px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              申请离校
            </el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="myLeaves.length === 0 && !loading" description="暂无离校记录" />

      <template v-else>
        <el-table :data="myLeaves" v-loading="loading" stripe>
          <el-table-column prop="leaveId" label="离校编号" width="140" />
          <el-table-column prop="leaveType" label="离校类型" width="100">
            <template #default="{ row }">
              <el-tag>{{ row.leaveType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="leaveReason" label="离校原因" min-width="150" show-overflow-tooltip />
          <el-table-column prop="leaveDate" label="离校日期" width="120" />
          <el-table-column prop="overallStatus" label="整体状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.overallStatus)">
                {{ row.overallStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="handleViewDetail(row)">
                详情
              </el-button>
              <el-button
                v-if="canRevoke(row)"
                type="danger"
                size="small"
                link
                @click="handleRevoke(row)"
              >
                撤回
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 审核进度详情 -->
        <div v-if="myLeaves.length > 0 && myLeaves[0]" style="margin-top: 24px;">
          <h4 style="margin-bottom: 16px;">最新申请审核进度</h4>
          <el-steps :active="getActiveStep(myLeaves[0])" align-center>
            <el-step title="宿管审核" :status="getStepStatus(myLeaves[0]?.dormitoryStatus)">
              <template #description>
                <el-tag :type="getStatusType(myLeaves[0]?.dormitoryStatus || '')" size="small">
                  {{ myLeaves[0]?.dormitoryStatus }}
                </el-tag>
              </template>
            </el-step>
            <el-step title="图书馆审核" :status="getStepStatus(myLeaves[0]?.libraryStatus)">
              <template #description>
                <el-tag :type="getStatusType(myLeaves[0]?.libraryStatus || '')" size="small">
                  {{ myLeaves[0]?.libraryStatus }}
                </el-tag>
              </template>
            </el-step>
            <el-step title="财务处审核" :status="getStepStatus(myLeaves[0]?.financeStatus)">
              <template #description>
                <el-tag :type="getStatusType(myLeaves[0]?.financeStatus || '')" size="small">
                  {{ myLeaves[0]?.financeStatus }}
                </el-tag>
              </template>
            </el-step>
            <el-step title="教务处审核" :status="getStepStatus(myLeaves[0]?.adminStatus)">
              <template #description>
                <el-tag :type="getStatusType(myLeaves[0]?.adminStatus || '')" size="small">
                  {{ myLeaves[0]?.adminStatus }}
                </el-tag>
              </template>
            </el-step>
          </el-steps>
        </div>
      </template>
    </el-card>

    <!-- 离校说明 -->
    <el-card style="margin-top: 16px;">
      <template #header>
        <span>离校手续说明</span>
      </template>
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          离校手续需要依次通过以下部门审核：
        </template>
        <ol style="margin: 8px 0 0 20px; line-height: 2;">
          <li><strong>宿管中心</strong>：确认宿舍物品归还、退宿手续</li>
          <li><strong>图书馆</strong>：确认图书归还、无欠款</li>
          <li><strong>财务处</strong>：确认学费缴纳、无欠费</li>
          <li><strong>教务处</strong>：最终审核确认</li>
        </ol>
      </el-alert>
    </el-card>

    <!-- 申请对话框 -->
    <el-dialog v-model="dialogVisible" title="申请离校" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="离校编号">
          <el-input v-model="form.leaveId" disabled />
        </el-form-item>
        <el-form-item label="离校类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择离校类型" style="width: 100%">
            <el-option v-for="type in leaveTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="离校日期" prop="leaveDate">
          <el-date-picker
            v-model="form.leaveDate"
            type="date"
            placeholder="请选择离校日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="离校原因" prop="leaveReason">
          <el-input
            v-model="form.leaveReason"
            type="textarea"
            :rows="4"
            placeholder="请说明离校原因"
          />
        </el-form-item>
        <el-form-item label="证明材料">
          <AttachmentUpload
            v-if="form.leaveId"
            :related-id="form.leaveId!"
            related-type="leaveSchool"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="离校详情" width="600px">
      <template v-if="currentLeave">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="离校编号">{{ currentLeave.leaveId }}</el-descriptions-item>
          <el-descriptions-item label="离校类型">
            <el-tag>{{ currentLeave.leaveType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="离校日期">{{ currentLeave.leaveDate }}</el-descriptions-item>
          <el-descriptions-item label="整体状态">
            <el-tag :type="getStatusType(currentLeave.overallStatus)">
              {{ currentLeave.overallStatus }}
            </el-tag>
          </el-descriptions-item>
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
          <el-descriptions-item label="离校原因" :span="2">{{ currentLeave.leaveReason }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">证明材料</el-divider>
        <AttachmentUpload
          :related-id="currentLeave.leaveId"
          related-type="leaveSchool"
          :readonly="currentLeave.overallStatus !== '审核中'"
        />
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
// 辅助函数
function getActiveStep(leave: LeaveSchool): number {
  if (leave.adminStatus === '已通过') return 4
  if (leave.financeStatus === '已通过') return 3
  if (leave.libraryStatus === '已通过') return 2
  if (leave.dormitoryStatus === '已通过') return 1
  return 0
}

function getStepStatus(status: string): 'wait' | 'process' | 'finish' | 'error' | 'success' {
  switch (status) {
    case '已通过': return 'success'
    case '未通过': return 'error'
    case '待审核': return 'wait'
    default: return 'wait'
  }
}
</script>

<style scoped lang="less">
.leave-container {
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
