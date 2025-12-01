<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <div class="empty-container">
    <el-card>
      <el-empty description="页面开发中...">
        <template #image>
          <el-icon :size="80" color="#909399"><Tools /></el-icon>
        </template>
        <p style="color: #909399;">当前路径: {{ route.path }}</p>
      </el-empty>
    </el-card>
  </div>
</template>

<style scoped>
.empty-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
