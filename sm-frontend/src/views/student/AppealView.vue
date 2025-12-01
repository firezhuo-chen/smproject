<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAppealList, addAppeal, getPunishmentList } from '@/api/business'
import type { Appeal, Punishment } from '@/api/types'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const userStore = useUserStore()
const loading = ref(false)
const appealList = ref<Appeal[]>([])
const punishmentList = ref<Punishment[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const formRef = ref<FormInstance>()
const currentAppeal = ref<Appeal | null>(null)
const searchKeyword = ref('')
const filterStatus = ref('')

const statusOptions = ['受理中', '已通过', '未通过']

// 只显示当前学生的申诉
const myAppeals = computed(() => {
  let result = appealList.value.filter(item => item.stuId === userStore.userId)
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(a =>
      a.appealReason?.toLowerCase().includes(keyword) ||
      a.punishmentId?.toLowerCase().includes(keyword)
    )
  }
  if (filterStatus.value) {
    result = result.filter(a => a.appealStatus === filterStatus.value)
  }
  return result
})

// 可申诉的处分（已生效且未申诉过的）
const appealablePunishments = computed(() => {
  const appealedIds = myAppeals.value.map(a => a.punishmentId)
  return punishmentList.value.filter(
    p => p.stuId === userStore.userId && 
         p.punishmentStatus === '已生效' && 
         !appealedIds.includes(p.punishmentId)
  )
})

// 生成申诉编号
const generateAppealId = () => {
  const date = new Date()
  const dateStr = date.toISOString().split('T')[0]!.replace(/-/g, '')
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  return `AP${dateStr}${random}`
}

const defaultForm = (): Partial<Appeal> => ({
  appealId: '',
  punishmentId: '',
  stuId: userStore.userId,
  appealReason: '',
  appealDate: new Date().toISOString().split('T')[0],
  advisorStatus: '待审理',
  adminStatus: '待审理',
  appealStatus: '受理中'
})

const form = ref<Partial<Appeal>>(defaultForm())

const rules = ref<FormRules>({
  punishmentId: [{ required: true, message: '请选择要申诉的处分', trigger: 'change' }],
  appealReason: [{ required: true, message: '请输入申诉理由', trigger: 'blur' }]
})

const getStatusType = (status: string) => {
  switch (status) {
    case '已通过': return 'success'
    case '未通过': return 'danger'
    case '待审理':
    case '受理中': return 'warning'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const [appealRes, punishmentRes] = await Promise.all([
      getAppealList(),
      getPunishmentList()
    ])
    appealList.value = appealRes.data || []
    punishmentList.value = punishmentRes.data || []
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  if (appealablePunishments.value.length === 0) {
    ElMessage.warning('暂无可申诉的处分记录')
    return
  }
  form.value = defaultForm()
  form.value.appealId = generateAppealId()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    await addAppeal(form.value as Appeal)
    ElMessage.success('申诉提交成功')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

// 获取处分信息用于显示
const getPunishmentInfo = (punishmentId: string) => {
  const p = punishmentList.value.find(item => item.punishmentId === punishmentId)
  return p ? `${p.punishmentType} - ${p.punishmentReason?.substring(0, 20)}...` : punishmentId
}

const handleViewDetail = (row: Appeal) => {
  currentAppeal.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="appeal-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的申诉</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索理由/编号" clearable style="width: 160px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              提交申诉
            </el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="myAppeals.length === 0 && !loading" description="暂无申诉记录" />

      <el-table v-else :data="myAppeals" v-loading="loading" stripe>
        <el-table-column prop="appealId" label="申诉编号" width="150" />
        <el-table-column prop="punishmentId" label="处分编号" width="140" />
        <el-table-column prop="appealReason" label="申诉理由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="appealDate" label="申诉日期" width="110" />
        <el-table-column prop="advisorStatus" label="辅导员审理" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.advisorStatus)" size="small">
              {{ row.advisorStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminStatus" label="教务处审理" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.adminStatus)" size="small">
              {{ row.adminStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="appealStatus" label="当前状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.appealStatus)">
              {{ row.appealStatus }}
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
    </el-card>

    <!-- 申诉对话框 -->
    <el-dialog v-model="dialogVisible" title="提交申诉" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="申诉编号">
          <el-input v-model="form.appealId" disabled />
        </el-form-item>
        <el-form-item label="选择处分" prop="punishmentId">
          <el-select v-model="form.punishmentId" placeholder="请选择要申诉的处分" style="width: 100%">
            <el-option
              v-for="p in appealablePunishments"
              :key="p.punishmentId"
              :label="`${p.punishmentId} - ${p.punishmentType}`"
              :value="p.punishmentId"
            >
              <div>
                <span style="font-weight: bold;">{{ p.punishmentId }}</span>
                <el-tag :type="p.punishmentType === '警告' ? 'warning' : 'danger'" size="small" style="margin-left: 8px;">
                  {{ p.punishmentType }}
                </el-tag>
              </div>
              <div style="font-size: 12px; color: #909399;">
                {{ p.punishmentReason?.substring(0, 30) }}...
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="申诉理由" prop="appealReason">
          <el-input
            v-model="form.appealReason"
            type="textarea"
            :rows="5"
            placeholder="请详细说明申诉理由"
          />
        </el-form-item>
        <el-form-item label="证据材料">
          <AttachmentUpload
            v-if="form.appealId"
            :related-id="form.appealId!"
            related-type="punishment"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交申诉</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="申诉详情" width="600px">
      <template v-if="currentAppeal">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申诉编号">{{ currentAppeal.appealId }}</el-descriptions-item>
          <el-descriptions-item label="处分编号">{{ currentAppeal.punishmentId }}</el-descriptions-item>
          <el-descriptions-item label="申诉日期">{{ currentAppeal.appealDate }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentAppeal.appealStatus)">
              {{ currentAppeal.appealStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="辅导员审理">
            <el-tag :type="getStatusType(currentAppeal.advisorStatus)" size="small">
              {{ currentAppeal.advisorStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="教务处审理">
            <el-tag :type="getStatusType(currentAppeal.adminStatus)" size="small">
              {{ currentAppeal.adminStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申诉理由" :span="2">{{ currentAppeal.appealReason }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">证据材料</el-divider>
        <AttachmentUpload
          :related-id="currentAppeal.appealId"
          related-type="punishment"
          :readonly="currentAppeal.appealStatus !== '受理中'"
        />
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.appeal-container {
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
