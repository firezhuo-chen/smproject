<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getStudentBasicInfoById } from '@/api/student'
import type { StudentBasicInfo } from '@/api/types'

const userStore = useUserStore()
const loading = ref(false)
const studentInfo = ref<StudentBasicInfo | null>(null)

onMounted(async () => {
  if (userStore.userId) {
    loading.value = true
    try {
      const res = await getStudentBasicInfoById(userStore.userId)
      studentInfo.value = res.data
    } catch (error) {
      console.error('获取学生信息失败', error)
    } finally {
      loading.value = false
    }
  }
})
</script>

<template>
  <div class="profile-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>个人基本信息</span>
        </div>
      </template>
      
      <el-descriptions v-if="studentInfo" :column="2" border>
        <el-descriptions-item label="学号">{{ studentInfo.stuId }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ studentInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ studentInfo.gender }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ studentInfo.idCard }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ studentInfo.birthDate }}</el-descriptions-item>
        <el-descriptions-item label="国籍">{{ studentInfo.nationality }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ studentInfo.nation }}</el-descriptions-item>
        <el-descriptions-item label="籍贯">{{ studentInfo.nativePlace }}</el-descriptions-item>
        <el-descriptions-item label="政治面貌">{{ studentInfo.politicalStatus }}</el-descriptions-item>
        <el-descriptions-item label="手机号码">{{ studentInfo.phone }}</el-descriptions-item>
      </el-descriptions>
      
      <el-empty v-else description="暂无数据" />
    </el-card>
  </div>
</template>

<style scoped lang="less">
.profile-container {
  .card-header {
    font-size: 16px;
    font-weight: bold;
  }
}
</style>
