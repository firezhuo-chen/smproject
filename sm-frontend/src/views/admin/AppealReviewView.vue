<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getAppealList, updateAppeal, getPunishmentList, updatePunishment } from '@/api/business'
import type { Appeal, Punishment } from '@/api/types'
import { ElMessage } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const loading = ref(false)
const appealList = ref<Appeal[]>([])
const punishmentList = ref<Punishment[]>([])
const activeTab = ref('pending')
const detailVisible = ref(false)
const currentAppeal = ref<Appeal | null>(null)
const searchKeyword = ref('')
const filterStatus = ref('')

// 审核意见对话框
const opinionDialogVisible = ref(false)
const currentRow = ref<Appeal | null>(null)
const opinion = ref('')
const isApprove = ref(true)

const statusOptions = ['受理中', '已通过', '未通过']

// 搜索过滤
const filterList = (list: Appeal[]) => {
  let result = list
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(a =>
      a.stuId?.toLowerCase().includes(keyword) ||
      a.appealReason?.toLowerCase().includes(keyword)
    )
  }
  if (filterStatus.value) {
    result = result.filter(a => a.appealStatus === filterStatus.value)
  }
  return result
}

// 待终审的申诉（辅导员已通过，教务处待审理）
const pendingAppeals = computed(() => {
  return filterList(appealList.value.filter(a => 
    a.advisorStatus === '已通过' && 
    a.adminStatus === '待审理'
  ))
})

// 已处理的申诉
const processedAppeals = computed(() => {
  return filterList(appealList.value.filter(a => a.adminStatus !== '待审理'))
})

// 获取处分信息
const getPunishmentInfo = (punishmentId: string) => {
  return punishmentList.value.find(p => p.punishmentId === punishmentId)
}

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

// 打开审核意见对话框
const openOpinionDialog = (row: Appeal, approve: boolean) => {
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
    ElMessage.warning('请填写驳回理由')
    return
  }
  
  try {
    // 更新申诉状态
    const updatedAppeal: Appeal = { 
      ...currentRow.value, 
      adminStatus: isApprove.value ? '已通过' : '未通过',
      adminOpinion: opinion.value || (isApprove.value ? '同意' : ''),
      appealStatus: isApprove.value ? '已通过' : '未通过'
    }
    await updateAppeal(updatedAppeal)
    
    // 如果通过，撤销对应处分
    if (isApprove.value) {
      const punishment = getPunishmentInfo(currentRow.value.punishmentId)
      if (punishment) {
        const updatedPunishment = {
          ...punishment,
          punishmentStatus: '已撤销' as const
        }
        await updatePunishment(updatedPunishment)
      }
    }
    
    ElMessage.success(isApprove.value ? '申诉通过，处分已撤销' : '已驳回')
    opinionDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
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
  <div class="appeal-review-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>申诉终审</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="申诉状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索学号/理由" clearable style="width: 150px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-tag type="danger" v-if="pendingAppeals.length > 0">
              {{ pendingAppeals.length }} 条待终审
            </el-tag>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待终审" name="pending">
          <el-empty v-if="pendingAppeals.length === 0" description="暂无待终审的申诉" />
          <el-table v-else :data="pendingAppeals" v-loading="loading" stripe>
            <el-table-column prop="appealId" label="申诉编号" width="150" />
            <el-table-column prop="stuId" label="学号" width="120" />
            <el-table-column prop="punishmentId" label="处分编号" width="140" />
            <el-table-column label="处分类型" width="100">
              <template #default="{ row }">
                <el-tag type="danger" size="small">
                  {{ getPunishmentInfo(row.punishmentId)?.punishmentType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="appealReason" label="申诉理由" min-width="200" show-overflow-tooltip />
            <el-table-column prop="appealDate" label="申诉日期" width="110" />
            <el-table-column prop="advisorStatus" label="辅导员审理" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.advisorStatus)" size="small">
                  {{ row.advisorStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="handleViewDetail(row)">
                  详情
                </el-button>
                <el-button type="success" size="small" link @click="openOpinionDialog(row, true)">
                  同意
                </el-button>
                <el-button type="danger" size="small" link @click="openOpinionDialog(row, false)">
                  驳回
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="已处理" name="processed">
          <el-empty v-if="processedAppeals.length === 0" description="暂无已处理的申诉" />
          <el-table v-else :data="processedAppeals" v-loading="loading" stripe>
            <el-table-column prop="appealId" label="申诉编号" width="150" />
            <el-table-column prop="stuId" label="学号" width="120" />
            <el-table-column prop="punishmentId" label="处分编号" width="140" />
            <el-table-column prop="appealReason" label="申诉理由" min-width="200" show-overflow-tooltip />
            <el-table-column prop="appealDate" label="申诉日期" width="110" />
            <el-table-column prop="advisorStatus" label="辅导员审理" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.advisorStatus)" size="small">
                  {{ row.advisorStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="adminStatus" label="教务处审理" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.adminStatus)" size="small">
                  {{ row.adminStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="appealStatus" label="最终状态" width="100">
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
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="申诉详情" width="600px">
      <template v-if="currentAppeal">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申诉编号">{{ currentAppeal.appealId }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentAppeal.stuId }}</el-descriptions-item>
          <el-descriptions-item label="处分编号">{{ currentAppeal.punishmentId }}</el-descriptions-item>
          <el-descriptions-item label="处分类型">
            <el-tag type="danger" size="small">
              {{ getPunishmentInfo(currentAppeal.punishmentId)?.punishmentType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申诉日期">{{ currentAppeal.appealDate }}</el-descriptions-item>
          <el-descriptions-item label="最终状态">
            <el-tag :type="getStatusType(currentAppeal.appealStatus)">
              {{ currentAppeal.appealStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="辅导员审理">
            <el-tag :type="getStatusType(currentAppeal.advisorStatus)" size="small">
              {{ currentAppeal.advisorStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="辅导员意见">
            {{ currentAppeal.advisorOpinion || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="教务处审理">
            <el-tag :type="getStatusType(currentAppeal.adminStatus)" size="small">
              {{ currentAppeal.adminStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="教务意见">
            {{ currentAppeal.adminOpinion || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="申诉理由" :span="2">{{ currentAppeal.appealReason }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">证据材料</el-divider>
        <AttachmentUpload
          :related-id="currentAppeal.appealId"
          related-type="punishment"
          :readonly="true"
        />
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审核意见对话框 -->
    <el-dialog 
      v-model="opinionDialogVisible" 
      :title="isApprove ? '终审同意' : '终审驳回'" 
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="申诉编号">
          <span>{{ currentRow?.appealId }}</span>
        </el-form-item>
        <el-form-item label="学号">
          <span>{{ currentRow?.stuId }}</span>
        </el-form-item>
        <el-form-item v-if="isApprove">
          <el-alert type="warning" :closable="false" show-icon>
            同意申诉后，对应的处分将被撤销
          </el-alert>
        </el-form-item>
        <el-form-item label="审理意见">
          <el-input
            v-model="opinion"
            type="textarea"
            :rows="3"
            :placeholder="isApprove ? '请输入审理意见（可选）' : '请输入驳回理由'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="opinionDialogVisible = false">取消</el-button>
        <el-button :type="isApprove ? 'success' : 'danger'" @click="submitReview">
          {{ isApprove ? '确认同意' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.appeal-review-container {
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
