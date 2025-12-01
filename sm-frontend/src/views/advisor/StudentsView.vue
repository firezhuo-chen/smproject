<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getStudentBasicInfoList } from '@/api/student'
import { getStudentStatusInfoList } from '@/api/student'
import type { StudentBasicInfo, StudentStatusInfo } from '@/api/types'

const userStore = useUserStore()
const loading = ref(false)
const studentList = ref<StudentBasicInfo[]>([])
const statusList = ref<StudentStatusInfo[]>([])
const searchKeyword = ref('')
const filterStatus = ref('')
const filterWarning = ref('')
const detailVisible = ref(false)
const currentStudent = ref<any>(null)

const statusOptions = ['在读', '毕业', '退学', '休学', '转学', '保留学籍']
const warningOptions = ['无', '一级', '二级', '三级']

const handleViewDetail = (row: any) => {
  currentStudent.value = { ...row }
  detailVisible.value = true
}

// 合并学生基本信息和学籍信息
const mergedStudents = computed(() => {
  return studentList.value.map(student => {
    const status = statusList.value.find(s => s.stuId === student.stuId)
    return { ...student, ...status }
  })
})

// 只显示当前辅导员负责的学生
const myStudents = computed(() => {
  return mergedStudents.value.filter(s => s.advisorId === userStore.userId)
})

// 搜索过滤
const filteredStudents = computed(() => {
  let result = myStudents.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(s => 
      s.stuId?.toLowerCase().includes(keyword) ||
      s.name?.toLowerCase().includes(keyword) ||
      s.major?.toLowerCase().includes(keyword) ||
      s.className?.toLowerCase().includes(keyword)
    )
  }
  if (filterStatus.value) {
    result = result.filter(s => s.academicStatus === filterStatus.value)
  }
  if (filterWarning.value) {
    result = result.filter(s => s.warningLevel === filterWarning.value)
  }
  return result
})

const getWarningLevelType = (level: string) => {
  switch (level) {
    case '无': return 'success'
    case '一级': return 'warning'
    case '二级': return 'danger'
    case '三级': return 'danger'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const [basicRes, statusRes] = await Promise.all([
      getStudentBasicInfoList(),
      getStudentStatusInfoList()
    ])
    studentList.value = basicRes.data || []
    statusList.value = statusRes.data || []
  } catch (error) {
    console.error('获取学生列表失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="students-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我负责的学生</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="学籍状态" clearable style="width: 110px; margin-right: 12px;">
              <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
            </el-select>
            <el-select v-model="filterWarning" placeholder="警示等级" clearable style="width: 100px; margin-right: 12px;">
              <el-option v-for="w in warningOptions" :key="w" :label="w" :value="w" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索学号/姓名/专业/班级"
              style="width: 200px;"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-table :data="filteredStudents" v-loading="loading" stripe>
        <el-table-column prop="stuId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70" />
        <el-table-column prop="department" label="院系" width="140" />
        <el-table-column prop="major" label="专业" width="160" />
        <el-table-column prop="className" label="班级" width="100" />
        <el-table-column prop="academicStatus" label="学籍状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.academicStatus === '在读' ? 'success' : 'info'" size="small">
              {{ row.academicStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warningLevel" label="警示等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getWarningLevelType(row.warningLevel)" size="small">
              {{ row.warningLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerStatus" label="注册状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.registerStatus === '已注册' ? 'success' : 'danger'" size="small">
              {{ row.registerStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="summary" style="margin-top: 16px; color: #909399;">
        共 {{ filteredStudents.length }} 名学生
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="学生详细信息" width="800px">
      <template v-if="currentStudent">
        <el-divider content-position="left">基本信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学号">{{ currentStudent.stuId }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentStudent.gender }}</el-descriptions-item>
          <el-descriptions-item label="民族">{{ currentStudent.nation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ currentStudent.birthDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentStudent.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="籍贯">{{ currentStudent.nativePlace || '-' }}</el-descriptions-item>
          <el-descriptions-item label="政治面貌">{{ currentStudent.politicalStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentStudent.phone || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">学籍信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="院系">{{ currentStudent.department }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
          <el-descriptions-item label="学籍状态">
            <el-tag :type="currentStudent.academicStatus === '在读' ? 'success' : 'info'" size="small">
              {{ currentStudent.academicStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="入学日期">{{ currentStudent.admissionDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预计毕业">{{ currentStudent.graduationDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="警示等级">
            <el-tag :type="getWarningLevelType(currentStudent.warningLevel)" size="small">
              {{ currentStudent.warningLevel }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册状态">
            <el-tag :type="currentStudent.registerStatus === '已注册' ? 'success' : 'danger'" size="small">
              {{ currentStudent.registerStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="辅导员">{{ currentStudent.advisor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="辅导员工号">{{ currentStudent.advisorId || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.students-container {
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
