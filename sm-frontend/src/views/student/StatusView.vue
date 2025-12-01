<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getStudentStatusInfoById } from '@/api/student'
import type { StudentStatusInfo } from '@/api/types'

const userStore = useUserStore()
const loading = ref(false)
const statusInfo = ref<StudentStatusInfo | null>(null)

const getWarningLevelType = (level: string) => {
  switch (level) {
    case '无': return 'success'
    case '一级': return 'warning'
    case '二级': return 'danger'
    case '三级': return 'danger'
    default: return 'info'
  }
}

onMounted(async () => {
  if (userStore.userId) {
    loading.value = true
    try {
      const res = await getStudentStatusInfoById(userStore.userId)
      statusInfo.value = res.data
    } catch (error) {
      console.error('获取学籍信息失败', error)
    } finally {
      loading.value = false
    }
  }
})
</script>

<template>
  <div class="status-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>学籍信息</span>
        </div>
      </template>
      
      <el-descriptions v-if="statusInfo" :column="2" border>
        <el-descriptions-item label="学号">{{ statusInfo.stuId }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ statusInfo.department }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ statusInfo.major }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ statusInfo.className }}</el-descriptions-item>
        <el-descriptions-item label="学籍状态">
          <el-tag :type="statusInfo.academicStatus === '在读' ? 'success' : 'info'">
            {{ statusInfo.academicStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入学日期">{{ statusInfo.admissionDate }}</el-descriptions-item>
        <el-descriptions-item label="预计毕业日期">{{ statusInfo.graduationDate }}</el-descriptions-item>
        <el-descriptions-item label="学业警示等级">
          <el-tag :type="getWarningLevelType(statusInfo.warningLevel)">
            {{ statusInfo.warningLevel }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册状态">
          <el-tag :type="statusInfo.registerStatus === '已注册' ? 'success' : 'danger'">
            {{ statusInfo.registerStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="辅导员">{{ statusInfo.advisor }}</el-descriptions-item>
      </el-descriptions>
      
      <el-empty v-else description="暂无数据" />
    </el-card>
  </div>
</template>

<style scoped lang="less">
.status-container {
  .card-header {
    font-size: 16px;
    font-weight: bold;
  }
}
</style>
