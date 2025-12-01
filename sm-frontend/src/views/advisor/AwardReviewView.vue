<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAwardList, updateAward } from '@/api/business'
import { getStudentStatusInfoList } from '@/api/student'
import type { Award, StudentStatusInfo } from '@/api/types'
import { ElMessage } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const userStore = useUserStore()
const loading = ref(false)
const awardList = ref<Award[]>([])
const statusList = ref<StudentStatusInfo[]>([])
const activeTab = ref('pending')
const detailVisible = ref(false)
const currentAward = ref<Award | null>(null)
const searchKeyword = ref('')
const filterType = ref('')
const filterLevel = ref('')

// 审核意见对话框
const opinionDialogVisible = ref(false)
const currentRow = ref<Award | null>(null)
const opinion = ref('')
const isApprove = ref(true)

const awardTypes = ['学业类', '科研类', '竞赛类', '社会实践类', '志愿服务类', '文体类', '创新创业类', '学生工作类']
const awardLevels = ['国家级', '省级', '市级', '校级', '院级', '企业级']

// 获取辅导员负责的学生ID列表
const myStudentIds = computed(() => {
  return statusList.value
    .filter(s => s.advisorId === userStore.userId)
    .map(s => s.stuId)
})

// 搜索过滤
const filterList = (list: Award[]) => {
  let result = list
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(a =>
      a.stuId?.toLowerCase().includes(keyword) ||
      a.awardName?.toLowerCase().includes(keyword)
    )
  }
  if (filterType.value) {
    result = result.filter(a => a.awardType === filterType.value)
  }
  if (filterLevel.value) {
    result = result.filter(a => a.awardLevel === filterLevel.value)
  }
  return result
}

// 待审批的奖励（辅导员待审批）
const pendingAwards = computed(() => {
  return filterList(awardList.value.filter(a => 
    myStudentIds.value.includes(a.stuId) && 
    a.advisorStatus === '待审批'
  ))
})

// 已处理的奖励
const processedAwards = computed(() => {
  return filterList(awardList.value.filter(a => 
    myStudentIds.value.includes(a.stuId) && 
    a.advisorStatus !== '待审批'
  ))
})

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
    const [awardRes, statusRes] = await Promise.all([
      getAwardList(),
      getStudentStatusInfoList()
    ])
    awardList.value = awardRes.data || []
    statusList.value = statusRes.data || []
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

// 打开审核意见对话框
const openOpinionDialog = (row: Award, approve: boolean) => {
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
    const updated: Award = {
      ...currentRow.value,
      advisorStatus: isApprove.value ? '已通过' : '未通过',
      advisorOpinion: opinion.value || (isApprove.value ? '同意' : ''),
      ...(isApprove.value ? {} : { awardStatus: '未通过' as const })
    }
    await updateAward(updated)
    ElMessage.success(isApprove.value ? '审批通过' : '已拒绝')
    opinionDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleViewDetail = (row: Award) => {
  currentAward.value = row
  detailVisible.value = true
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="award-review-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>奖励审批</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="奖励类型" clearable style="width: 120px; margin-right: 12px;">
              <el-option v-for="t in awardTypes" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="filterLevel" placeholder="奖励等级" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="l in awardLevels" :key="l" :label="l" :value="l" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索学号/名称" clearable style="width: 160px; margin-right: 12px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-tag type="danger" v-if="pendingAwards.length > 0">
              {{ pendingAwards.length }} 条待审批
            </el-tag>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审批" name="pending">
          <el-empty v-if="pendingAwards.length === 0" description="暂无待审批的奖励申请" />
          <el-table v-else :data="pendingAwards" v-loading="loading" stripe>
            <el-table-column prop="awardId" label="奖励编号" width="140" />
            <el-table-column prop="stuId" label="学号" width="120" />
            <el-table-column prop="awardName" label="奖励名称" min-width="150" />
            <el-table-column prop="awardType" label="类型" width="100" />
            <el-table-column prop="awardLevel" label="等级" width="90" />
            <el-table-column prop="awardAmount" label="金额" width="90">
              <template #default="{ row }">
                ¥{{ row.awardAmount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="issueOrg" label="颁发单位" width="120" />
            <el-table-column prop="awardDate" label="获奖日期" width="110" />
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
          <el-empty v-if="processedAwards.length === 0" description="暂无已处理的奖励申请" />
          <el-table v-else :data="processedAwards" v-loading="loading" stripe>
            <el-table-column prop="awardId" label="奖励编号" width="140" />
            <el-table-column prop="stuId" label="学号" width="120" />
            <el-table-column prop="awardName" label="奖励名称" min-width="150" />
            <el-table-column prop="awardType" label="类型" width="100" />
            <el-table-column prop="awardLevel" label="等级" width="90" />
            <el-table-column prop="awardDate" label="获奖日期" width="110" />
            <el-table-column prop="advisorStatus" label="我的审批" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.advisorStatus)" size="small">
                  {{ row.advisorStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="awardStatus" label="最终状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.awardStatus)">
                  {{ row.awardStatus }}
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
    <el-dialog v-model="detailVisible" title="奖励详情" width="600px">
      <template v-if="currentAward">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="奖励编号">{{ currentAward.awardId }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ currentAward.stuId }}</el-descriptions-item>
          <el-descriptions-item label="奖励名称">{{ currentAward.awardName }}</el-descriptions-item>
          <el-descriptions-item label="奖励类型">{{ currentAward.awardType }}</el-descriptions-item>
          <el-descriptions-item label="奖励等级">{{ currentAward.awardLevel }}</el-descriptions-item>
          <el-descriptions-item label="奖金金额">¥{{ currentAward.awardAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="颁发单位">{{ currentAward.issueOrg }}</el-descriptions-item>
          <el-descriptions-item label="获奖日期">{{ currentAward.awardDate }}</el-descriptions-item>
          <el-descriptions-item label="申请日期">{{ currentAward.applyDate }}</el-descriptions-item>
          <el-descriptions-item label="我的审批">
            <el-tag :type="getStatusType(currentAward.advisorStatus)" size="small">
              {{ currentAward.advisorStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="我的意见" :span="2">
            {{ currentAward.advisorOpinion || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="教务审批">
            <el-tag :type="getStatusType(currentAward.adminStatus)" size="small">
              {{ currentAward.adminStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="教务意见" :span="2">
            {{ currentAward.adminOpinion || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最终状态">
            <el-tag :type="getStatusType(currentAward.awardStatus)">
              {{ currentAward.awardStatus }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">证明材料</el-divider>
        <AttachmentUpload
          :related-id="currentAward.awardId"
          related-type="award"
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
      :title="isApprove ? '审批通过' : '审批拒绝'" 
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="奖励名称">
          <span>{{ currentRow?.awardName }}</span>
        </el-form-item>
        <el-form-item label="学号">
          <span>{{ currentRow?.stuId }}</span>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="opinion"
            type="textarea"
            :rows="3"
            :placeholder="isApprove ? '请输入审批意见（可选）' : '请输入拒绝理由'"
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
  </div>
</template>

<style scoped lang="less">
.award-review-container {
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
