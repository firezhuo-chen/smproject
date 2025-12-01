<script setup lang="ts">
import { computed } from 'vue'
import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'
import { useResponsive } from '@/composables/useResponsive'

const { isMobile, sidebarWidth, headerHeight, sidebarVisible, closeMobileSidebar } = useResponsive()

const contentStyle = computed(() => ({
  paddingTop: `${headerHeight.value}px`,
  marginLeft: isMobile.value ? '0' : `${sidebarWidth.value}px`,
  transition: 'margin-left 0.3s ease'
}))
</script>

<template>
  <div class="layout">
    <AppHeader />
    <AppSidebar />
    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && sidebarVisible" 
      class="sidebar-overlay"
      @click="closeMobileSidebar"
    ></div>
    <main class="pdm-content__wrapper" :style="contentStyle">
      <div class="pdm-content">
        <router-view />
      </div>
      <footer class="layout-footer">
        <span>学籍管理系统开发小组 &copy; 2025</span>
      </footer>
    </main>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f5f5f5;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
}
</style>
