<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getStatusChangeList, addStatusChange, deleteStatusChange } from '@/api/business'
import { getStudentStatusInfoById } from '@/api/student'
import type { StatusChange, StudentStatusInfo } from '@/api/types'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'
import { confirmRevoke } from '@/utils/confirm'

const userStore = useUserStore()
const loading = ref(false)
const statusChangeList = ref<StatusChange[]>([])
const studentStatus = ref<StudentStatusInfo | null>(null)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref<FormInstance>()
const currentChange = ref<StatusChange | null>(null)
const searchKeyword = ref('')
const filterStatus = ref('')
const filterType = ref('')

const statusOptions = ['待审核', '已通过', '未通过']

// 只显示当前学生的学籍变动
const myStatusChanges = computed(() => {
  let result = statusChangeList.value.filter(item => item.stuId === userStore.userId)
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(s =>
      s.changeReason?.toLowerCase().includes(keyword) ||
      s.targetMajor?.toLowerCase().includes(keyword)
    )
  }
  if (filterStatus.value) {
    result = result.filter(s => s.applyStatus === filterStatus.value)
  }
  if (filterType.value) {
    result = result.filter(s => s.changeType === filterType.value)
  }
  return result
})

const changeTypes = ['转专业', '交换', '退学', '休学', '复学', '转学']

// 生成变动编号
const generateChangeId = () => {
  const date = new Date()
  const dateStr = date.toISOString().split('T')[0]!.replace(/-/g, '')
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  return `C${dateStr}${random}`
}

const defaultForm = (): Partial<StatusChange> => ({
  changeId: '',
  stuId: userStore.userId,
  changeType: undefined,
  changeReason: '',
  currentSchool: '',
  currentCollege: '',
  currentMajor: '',
  targetSchool: '',
  targetCollege: '',
  targetMajor: '',
  applyDate: new Date().toISOString().split('T')[0],
  advisorStatus: '待审核',
  adminStatus: '待审核',
  applyStatus: '待审核'
})

const form = ref<Partial<StatusChange>>(defaultForm())

const rules = ref<FormRules>({
  changeType: [{ required: true, message: '请选择变动类型', trigger: 'change' }],
  changeReason: [{ required: true, message: '请输入变动原因', trigger: 'blur' }]
})

const getStatusType = (status: string) => {
  switch (status) {
    case '已通过': return 'success'
    case '未通过': return 'danger'
    case '待审核': return 'warning'
    default: return 'info'
  }
}

// 是否需要填写目标信息
const needTargetInfo = computed(() => {
  return ['转专业', '交换', '转学'].includes(form.value.changeType || '')
})

// 是否需要填写结束日期
const needEndDate = computed(() => {
  return ['交换', '休学'].includes(form.value.changeType || '')
})

const fetchData = async () => {
  loading.value = true
  try {
    const [changeRes, statusRes] = await Promise.all([
      getStatusChangeList(),
      getStudentStatusInfoById(userStore.userId)
    ])
    statusChangeList.value = changeRes.data || []
    studentStatus.value = statusRes.data
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.value = defaultForm()
  form.value.changeId = generateChangeId()
  
  // 自动填充当前学籍信息
  if (studentStatus.value) {
    form.value.currentSchool = '北京科技大学' // 假设学校名称
    form.value.currentCollege = studentStatus.value.department
    form.value.currentMajor = studentStatus.value.major
  }
  
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    await addStatusChange(form.value as StatusChange)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

// 撤回申请
const handleRevoke = async (row: StatusChange) => {
  const confirmed = await confirmRevoke(`${row.changeType}申请`)
  if (!confirmed) return
  
  try {
    await deleteStatusChange(row.changeId)
    ElMessage.success('申请已撤回')
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '撤回失败')
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
  <div class="status-change-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学籍变动申请</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="变动类型" clearable style="width: 110px; margin-right: 12px;">
              <el-option v-for="t in changeTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索原因/专业" clearable style="width: 150px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              申请变动
            </el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="myStatusChanges.length === 0 && !loading" description="暂无学籍变动记录" />

      <el-table v-else :data="myStatusChanges" v-loading="loading" stripe>
        <el-table-column prop="changeId" label="变动编号" width="140" />
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
        <el-table-column prop="advisorStatus" label="辅导员审核" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.advisorStatus)" size="small">
              {{ row.advisorStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminStatus" label="教务审核" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.adminStatus)" size="small">
              {{ row.adminStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyStatus" label="最终状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.applyStatus)">
              {{ row.applyStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button
              v-if="row.advisorStatus === '待审核'"
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
    </el-card>

    <!-- 申请对话框 -->
    <el-dialog v-model="dialogVisible" title="申请学籍变动" width="650px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="变动编号">
          <el-input v-model="form.changeId" disabled />
        </el-form-item>
        <el-form-item label="变动类型" prop="changeType">
          <el-select v-model="form.changeType" placeholder="请选择变动类型" style="width: 100%">
            <el-option v-for="type in changeTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        
        <el-divider content-position="left">当前信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="当前学校">
              <el-input v-model="form.currentSchool" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="当前学院">
              <el-input v-model="form.currentCollege" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="当前专业">
              <el-input v-model="form.currentMajor" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        
        <template v-if="needTargetInfo">
          <el-divider content-position="left">目标信息</el-divider>
          <el-row :gutter="16">
            <el-col :span="8" v-if="form.changeType !== '转专业'">
              <el-form-item label="目标学校">
                <el-input v-model="form.targetSchool" placeholder="请输入" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="目标学院">
                <el-input v-model="form.targetCollege" placeholder="请输入" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="目标专业">
                <el-input v-model="form.targetMajor" placeholder="请输入" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
        
        <el-divider content-position="left">时间信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="期望生效日期">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="请选择"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="needEndDate">
            <el-form-item label="预计结束日期">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="请选择"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="变动原因" prop="changeReason">
          <el-input
            v-model="form.changeReason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明学籍变动原因"
          />
        </el-form-item>
        <el-form-item label="证明材料">
          <AttachmentUpload
            v-if="form.changeId"
            :related-id="form.changeId!"
            related-type="statusChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="学籍变动详情" width="650px">
      <template v-if="currentChange">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="变动编号">{{ currentChange.changeId }}</el-descriptions-item>
          <el-descriptions-item label="变动类型">
            <el-tag>{{ currentChange.changeType }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前学校">{{ currentChange.currentSchool }}</el-descriptions-item>
          <el-descriptions-item label="当前学院">{{ currentChange.currentCollege }}</el-descriptions-item>
          <el-descriptions-item label="当前专业">{{ currentChange.currentMajor }}</el-descriptions-item>
          <el-descriptions-item label="目标专业">{{ currentChange.targetMajor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ currentChange.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="生效日期">{{ currentChange.startDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="辅导员审核">
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
          :readonly="currentChange.advisorStatus !== '待审核'"
        />
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.status-change-container {
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
