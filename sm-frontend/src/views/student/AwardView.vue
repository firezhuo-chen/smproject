<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAwardList, addAward, updateAward } from '@/api/business'
import type { Award } from '@/api/types'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'
import { confirmRevoke } from '@/utils/confirm'

const userStore = useUserStore()
const loading = ref(false)
const awardList = ref<Award[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('申请奖励')
const formRef = ref<FormInstance>()
const currentAward = ref<Award | null>(null)
const searchKeyword = ref('')
const filterStatus = ref('')

// 只显示当前学生的奖励
const myAwards = computed(() => {
  let result = awardList.value.filter(item => item.stuId === userStore.userId)
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(a =>
      a.awardName?.toLowerCase().includes(keyword) ||
      a.awardType?.toLowerCase().includes(keyword)
    )
  }
  if (filterStatus.value) {
    result = result.filter(a => a.awardStatus === filterStatus.value)
  }
  return result
})

const statusOptions = ['审批中', '已通过', '未通过']

const defaultForm = (): Partial<Award> => ({
  awardId: '',
  stuId: userStore.userId,
  awardType: '',
  awardLevel: '',
  awardName: '',
  awardAmount: 0,
  issueOrg: '',
  applyDate: new Date().toISOString().split('T')[0],
  awardDate: '',
  advisorStatus: '待审批',
  adminStatus: '待审批',
  awardStatus: '审批中'
})

const form = ref<Partial<Award>>(defaultForm())

const rules = ref<FormRules>({
  awardType: [{ required: true, message: '请选择奖励类型', trigger: 'change' }],
  awardLevel: [{ required: true, message: '请选择奖励等级', trigger: 'change' }],
  awardName: [{ required: true, message: '请输入奖励名称', trigger: 'blur' }],
  issueOrg: [{ required: true, message: '请输入颁发单位', trigger: 'blur' }],
  awardDate: [{ required: true, message: '请选择获奖日期', trigger: 'change' }]
})

const awardTypes = ['学业类', '科研类', '竞赛类', '社会实践类', '志愿服务类', '文体类', '创新创业类', '学生工作类']
const awardLevels = ['国家级', '省级', '市级', '校级', '院级', '企业级']

// 生成奖励编号
const generateAwardId = () => {
  const date = new Date()
  const dateStr = date.toISOString().split('T')[0]!.replace(/-/g, '')
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  return `A${dateStr}${random}`
}

const getStatusType = (status: string) => {
  switch (status) {
    case '已通过': return 'success'
    case '未通过': return 'danger'
    case '待审批':
    case '审批中': return 'warning'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAwardList()
    awardList.value = res.data || []
  } catch (error) {
    console.error('获取奖励列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '申请奖励'
  form.value = defaultForm()
  form.value.awardId = generateAwardId()
  dialogVisible.value = true
}

const handleViewDetail = (row: Award) => {
  currentAward.value = row
  detailVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    await addAward(form.value as Award)
    ElMessage.success('申请提交成功')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

const handleCancel = async (row: Award) => {
  if (row.awardStatus !== '审批中') {
    ElMessage.warning('只能撤销审批中的申请')
    return
  }
  
  const confirmed = await confirmRevoke(row.awardName)
  if (!confirmed) return
  
  try {
    // 通过更新状态为"未通过"来模拟撤销
    const updated = { ...row, awardStatus: '未通过' as const }
    await updateAward(updated)
    ElMessage.success('已撤销')
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '撤销失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="award-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的奖励</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索名称/类型" clearable style="width: 160px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              申请奖励
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="myAwards" v-loading="loading" stripe>
        <el-table-column prop="awardId" label="奖励编号" width="140" />
        <el-table-column prop="awardName" label="奖励名称" min-width="150" />
        <el-table-column prop="awardType" label="奖励类型" width="120" />
        <el-table-column prop="awardLevel" label="奖励等级" width="100" />
        <el-table-column prop="awardAmount" label="奖金金额" width="100">
          <template #default="{ row }">
            ¥{{ row.awardAmount?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="issueOrg" label="颁发单位" width="120" />
        <el-table-column prop="awardDate" label="获奖日期" width="110" />
        <el-table-column prop="advisorStatus" label="辅导员审批" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.advisorStatus)" size="small">
              {{ row.advisorStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminStatus" label="教务处审批" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.adminStatus)" size="small">
              {{ row.adminStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="awardStatus" label="当前状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.awardStatus)">
              {{ row.awardStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button
              v-if="row.awardStatus === '审批中'"
              type="danger"
              size="small"
              link
              @click="handleCancel(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 申请对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="奖励编号">
          <el-input v-model="form.awardId" disabled />
        </el-form-item>
        <el-form-item label="奖励类型" prop="awardType">
          <el-select v-model="form.awardType" placeholder="请选择奖励类型" style="width: 100%">
            <el-option v-for="type in awardTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖励等级" prop="awardLevel">
          <el-select v-model="form.awardLevel" placeholder="请选择奖励等级" style="width: 100%">
            <el-option v-for="level in awardLevels" :key="level" :label="level" :value="level" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖励名称" prop="awardName">
          <el-input v-model="form.awardName" placeholder="请输入奖励名称" />
        </el-form-item>
        <el-form-item label="奖金金额">
          <el-input-number v-model="form.awardAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="颁发单位" prop="issueOrg">
          <el-input v-model="form.issueOrg" placeholder="请输入颁发单位" />
        </el-form-item>
        <el-form-item label="获奖日期" prop="awardDate">
          <el-date-picker
            v-model="form.awardDate"
            type="date"
            placeholder="请选择获奖日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="证明材料">
          <AttachmentUpload
            v-if="form.awardId"
            :related-id="form.awardId!"
            related-type="award"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="奖励详情" width="600px">
      <template v-if="currentAward">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="奖励编号">{{ currentAward.awardId }}</el-descriptions-item>
          <el-descriptions-item label="奖励名称">{{ currentAward.awardName }}</el-descriptions-item>
          <el-descriptions-item label="奖励类型">{{ currentAward.awardType }}</el-descriptions-item>
          <el-descriptions-item label="奖励等级">{{ currentAward.awardLevel }}</el-descriptions-item>
          <el-descriptions-item label="奖金金额">¥{{ currentAward.awardAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="颁发单位">{{ currentAward.issueOrg }}</el-descriptions-item>
          <el-descriptions-item label="获奖日期">{{ currentAward.awardDate }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ currentAward.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="辅导员审批">
            <el-tag :type="getStatusType(currentAward.advisorStatus)" size="small">
              {{ currentAward.advisorStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="教务审批">
            <el-tag :type="getStatusType(currentAward.adminStatus)" size="small">
              {{ currentAward.adminStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最终状态" :span="2">
            <el-tag :type="getStatusType(currentAward.awardStatus)">
              {{ currentAward.awardStatus }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">证明材料</el-divider>
        <AttachmentUpload
          :related-id="currentAward.awardId"
          related-type="award"
          :readonly="currentAward.awardStatus !== '审批中'"
        />
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.award-container {
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
  .text-gray {
    color: #909399;
  }
}
</style>
