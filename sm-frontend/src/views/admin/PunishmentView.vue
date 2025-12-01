<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { getPunishmentPage, addPunishment, updatePunishment, exportPunishments } from '@/api/business'
import type { Punishment } from '@/api/types'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AttachmentUpload from '@/components/AttachmentUpload.vue'
import { confirmApprove, confirmDanger } from '@/utils/confirm'

const userStore = useUserStore()
const isEducationAdmin = computed(() => userStore.isEducationAdmin)
const adminRole = computed(() => userStore.adminRole)
const currentUserId = computed(() => userStore.userId)

const loading = ref(false)
const punishmentList = ref<Punishment[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增处分')
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const currentPunishment = ref<Punishment | null>(null)
const searchKeyword = ref('')
const filterType = ref('')
const filterStatus = ref('')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const punishmentTypes = ['警告', '严重警告', '记过', '留校察看', '开除学籍']
const statusOptions = ['审批中', '已生效', '已撤销', '申诉中']

// 前端过滤（用于非教务管理员和关键词搜索）
const filteredPunishments = computed(() => {
  let result = punishmentList.value
  
  // 非教务管理员只能看到自己提交的处分申请
  if (!isEducationAdmin.value) {
    result = result.filter(p => p.applicantId === currentUserId.value)
  }
  
  // 关键词搜索（前端过滤）
  if (searchKeyword.value) {
    result = result.filter(p =>
      p.stuId?.includes(searchKeyword.value) ||
      p.punishmentReason?.includes(searchKeyword.value)
    )
  }
  
  // 处分类型过滤（前端过滤）
  if (filterType.value) {
    result = result.filter(p => p.punishmentType === filterType.value)
  }
  
  return result
})

// 生成处分编号
const generatePunishmentId = () => {
  const date = new Date()
  const dateStr = date.toISOString().split('T')[0]!.replace(/-/g, '')
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  return `P${dateStr}${random}`
}

// 根据角色设置决定单位
const getIssueOrg = () => {
  switch (adminRole.value) {
    case '宿管管理员': return '宿管处'
    case '图书馆管理员': return '图书馆'
    case '财务处管理员': return '财务处'
    default: return '教务处'
  }
}

const defaultForm = (): Partial<Punishment> => ({
  punishmentId: '',
  stuId: '',
  punishmentType: undefined,
  punishmentReason: '',
  issueOrg: getIssueOrg(),
  applyDate: new Date().toISOString().split('T')[0],
  punishmentDate: '',
  applicantId: currentUserId.value,
  applicantRole: adminRole.value || '',
  adminStatus: '待审批',
  punishmentStatus: '审批中'
})

const form = ref<Partial<Punishment>>(defaultForm())

const rules = ref<FormRules>({
  stuId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  punishmentType: [{ required: true, message: '请选择处分类型', trigger: 'change' }],
  punishmentReason: [{ required: true, message: '请输入处分原因', trigger: 'blur' }],
  punishmentDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
})

const getStatusType = (status: string) => {
  switch (status) {
    case '已生效': return 'danger'
    case '已撤销': return 'info'
    case '申诉中': return 'warning'
    case '审批中': return 'warning'
    default: return 'info'
  }
}

const getPunishmentTypeColor = (type: string) => {
  switch (type) {
    case '警告': return 'warning'
    case '严重警告': return 'warning'
    case '记过': return 'danger'
    case '留校察看': return 'danger'
    case '开除学籍': return 'danger'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPunishmentPage({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      punishmentStatus: filterStatus.value || undefined
    })
    punishmentList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取处分列表失败', error)
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

// 监听筛选条件变化
watch(filterStatus, () => {
  currentPage.value = 1
  fetchData()
})

const handleAdd = () => {
  dialogTitle.value = isEducationAdmin.value ? '新增处分' : '提交处分申请'
  isEdit.value = false
  form.value = defaultForm()
  form.value.punishmentId = generatePunishmentId()
  dialogVisible.value = true
}

const handleApprove = async (row: Punishment) => {
  const confirmed = await confirmApprove(`「${row.punishmentType}」处分`)
  if (!confirmed) return
  
  try {
    const updated = { 
      ...row, 
      adminStatus: '已通过' as const,
      punishmentStatus: '已生效' as const
    }
    await updatePunishment(updated)
    ElMessage.success('处分已生效')
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleRevoke = async (row: Punishment) => {
  const confirmed = await confirmDanger('撤销处分', `确定要撤销学生 ${row.stuId} 的「${row.punishmentType}」处分吗？`, '确认撤销')
  if (!confirmed) return
  
  try {
    const updated = { 
      ...row, 
      punishmentStatus: '已撤销' as const
    }
    await updatePunishment(updated)
    ElMessage.success('处分已撤销')
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    // 确保申请人信息已填入
    form.value.applicantId = currentUserId.value
    form.value.applicantRole = adminRole.value || ''
    
    await addPunishment(form.value as Punishment)
    ElMessage.success(isEducationAdmin.value ? '处分记录已添加' : '处分申请已提交，等待教务管理员审批')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '添加失败')
  }
}

// 是否可以上传附件（所有管理员都可以上传）
const canUploadAttachment = computed(() => {
  return ['宿管管理员', '图书馆管理员', '财务处管理员', '教务管理员'].includes(adminRole.value || '')
})

const handleViewDetail = (row: Punishment) => {
  currentPunishment.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="punishment-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEducationAdmin ? '处分审批' : '处分申请' }}</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="处分类型" clearable style="width: 120px; margin-right: 12px;">
              <el-option v-for="t in punishmentTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索学号/原因" clearable style="width: 160px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button v-if="isEducationAdmin" type="success" @click="exportPunishments()" style="margin-right: 12px;">
              <el-icon><Download /></el-icon>
              导出处分记录
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              {{ isEducationAdmin ? '新增处分' : '提交处分申请' }}
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredPunishments" v-loading="loading" stripe>
        <el-table-column prop="punishmentId" label="处分编号" width="140" />
        <el-table-column prop="stuId" label="学号" width="120" />
        <el-table-column prop="punishmentType" label="处分类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getPunishmentTypeColor(row.punishmentType)">
              {{ row.punishmentType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="punishmentReason" label="处分原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="issueOrg" label="决定单位" width="100" />
        <el-table-column prop="punishmentDate" label="生效日期" width="110" />
        <el-table-column prop="adminStatus" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.adminStatus)" size="small">
              {{ row.adminStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="punishmentStatus" label="当前状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.punishmentStatus)">
              {{ row.punishmentStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isEducationAdmin" prop="applicantRole" label="申请人" width="120">
          <template #default="{ row }">
            <span v-if="row.applicantRole">{{ row.applicantRole }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">
              详情
            </el-button>
            <!-- 教务管理员可以审批和撤销 -->
            <template v-if="isEducationAdmin">
              <el-button
                v-if="row.punishmentStatus === '审批中'"
                type="warning"
                size="small"
                link
                @click="handleApprove(row)"
              >
                批准生效
              </el-button>
              <el-button
                v-if="row.punishmentStatus === '已生效'"
                type="danger"
                size="small"
                link
                @click="handleRevoke(row)"
              >
                撤销
              </el-button>
            </template>
            <!-- 其他管理员显示状态标签 -->
            <template v-else>
              <el-tag v-if="row.punishmentStatus === '审批中'" type="info" size="small">等待审批</el-tag>
              <el-tag v-else-if="row.punishmentStatus === '已生效'" type="success" size="small">已通过</el-tag>
              <el-tag v-else-if="row.punishmentStatus === '已撤销'" type="info" size="small">已撤销</el-tag>
              <el-tag v-else type="warning" size="small">{{ row.punishmentStatus }}</el-tag>
            </template>
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

    <!-- 新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="处分编号">
          <el-input v-model="form.punishmentId" disabled />
        </el-form-item>
        <el-form-item label="学号" prop="stuId">
          <el-input v-model="form.stuId" placeholder="请输入学生学号" />
        </el-form-item>
        <el-form-item label="处分类型" prop="punishmentType">
          <el-select v-model="form.punishmentType" placeholder="请选择处分类型" style="width: 100%">
            <el-option v-for="type in punishmentTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item label="决定单位">
          <el-input v-model="form.issueOrg" />
        </el-form-item>
        <el-form-item label="生效日期" prop="punishmentDate">
          <el-date-picker
            v-model="form.punishmentDate"
            type="date"
            placeholder="请选择生效日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="处分原因" prop="punishmentReason">
          <el-input
            v-model="form.punishmentReason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明处分原因"
          />
        </el-form-item>
        <el-form-item v-if="canUploadAttachment" label="证据材料">
          <AttachmentUpload
            v-if="form.punishmentId"
            :related-id="form.punishmentId!"
            related-type="punishment"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="处分详情" width="650px">
      <template v-if="currentPunishment">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="处分编号">{{ currentPunishment.punishmentId }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentPunishment.stuId }}</el-descriptions-item>
          <el-descriptions-item label="处分类型">
            <el-tag :type="getPunishmentTypeColor(currentPunishment.punishmentType)">
              {{ currentPunishment.punishmentType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="决定单位">{{ currentPunishment.issueOrg }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ currentPunishment.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="生效日期">{{ currentPunishment.punishmentDate }}</el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="getStatusType(currentPunishment.adminStatus)" size="small">
              {{ currentPunishment.adminStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(currentPunishment.punishmentStatus)">
              {{ currentPunishment.punishmentStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentPunishment.applicantRole" label="申请人" :span="2">
            {{ currentPunishment.applicantRole }}
          </el-descriptions-item>
          <el-descriptions-item label="处分原因" :span="2">{{ currentPunishment.punishmentReason }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">证据材料</el-divider>
        <AttachmentUpload
          :related-id="currentPunishment.punishmentId"
          related-type="punishment"
          :readonly="!canUploadAttachment || currentPunishment.punishmentStatus === '已撤销'"
        />
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.punishment-container {
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
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
