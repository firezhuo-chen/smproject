<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getPunishmentList } from '@/api/business'
import type { Punishment } from '@/api/types'

const userStore = useUserStore()
const loading = ref(false)
const punishmentList = ref<Punishment[]>([])
const searchKeyword = ref('')
const filterStatus = ref('')
const filterType = ref('')

const statusOptions = ['审批中', '已生效', '申诉中', '已撤销']
const typeOptions = ['警告', '严重警告', '记过', '留校察看', '开除学籍']

// 只显示当前学生的处分
const myPunishments = computed(() => {
  let result = punishmentList.value.filter(item => item.stuId === userStore.userId)
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(p =>
      p.punishmentReason?.toLowerCase().includes(keyword) ||
      p.issueOrg?.toLowerCase().includes(keyword)
    )
  }
  if (filterStatus.value) {
    result = result.filter(p => p.punishmentStatus === filterStatus.value)
  }
  if (filterType.value) {
    result = result.filter(p => p.punishmentType === filterType.value)
  }
  return result
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
    const res = await getPunishmentList()
    punishmentList.value = res.data || []
  } catch (error) {
    console.error('获取处分列表失败', error)
  } finally {
    loading.value = false
  }
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
          <span>我的处分记录</span>
          <div class="header-actions">
            <el-select v-model="filterType" placeholder="处分类型" clearable style="width: 120px; margin-right: 12px;">
              <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-input v-model="searchKeyword" placeholder="搜索原因/单位" clearable style="width: 160px;">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
        </div>
      </template>

      <el-alert
        v-if="myPunishments.length === 0 && !loading"
        title="恭喜！您没有任何处分记录"
        type="success"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-table v-if="myPunishments.length > 0" :data="myPunishments" v-loading="loading" stripe>
        <el-table-column prop="punishmentId" label="处分编号" width="140" />
        <el-table-column prop="punishmentType" label="处分类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getPunishmentTypeColor(row.punishmentType)">
              {{ row.punishmentType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="punishmentReason" label="处分原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="issueOrg" label="决定单位" width="120" />
        <el-table-column prop="punishmentDate" label="生效日期" width="110" />
        <el-table-column prop="adminStatus" label="教务处审批" width="110">
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.punishmentStatus === '已生效'"
              type="primary"
              size="small"
              link
              @click="$router.push('/student/appeal')"
            >
              去申诉
            </el-button>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 处分说明 -->
    <el-card style="margin-top: 16px;">
      <template #header>
        <span>处分类型说明</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="警告">
          <el-tag type="warning" size="small">轻微</el-tag>
          对违反校规校纪情节较轻的学生给予的处分
        </el-descriptions-item>
        <el-descriptions-item label="严重警告">
          <el-tag type="warning" size="small">较轻</el-tag>
          对违反校规校纪情节较重的学生给予的处分
        </el-descriptions-item>
        <el-descriptions-item label="记过">
          <el-tag type="danger" size="small">较重</el-tag>
          对违反校规校纪情节严重的学生给予的处分
        </el-descriptions-item>
        <el-descriptions-item label="留校察看">
          <el-tag type="danger" size="small">严重</el-tag>
          对违反校规校纪情节特别严重的学生给予的处分
        </el-descriptions-item>
        <el-descriptions-item label="开除学籍">
          <el-tag type="danger" size="small">最严重</el-tag>
          对违反校规校纪情节极其严重的学生给予的最高处分
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
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
}
</style>
