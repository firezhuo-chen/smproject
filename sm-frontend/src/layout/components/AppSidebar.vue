<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { computed, watch } from 'vue'
import { useResponsive } from '@/composables/useResponsive'

const route = useRoute()
const userStore = useUserStore()
const { isMobile, sidebarCollapsed, sidebarVisible, closeMobileSidebar, headerHeight } = useResponsive()

// 路由变化时关闭移动端侧边栏
watch(() => route.path, () => {
  closeMobileSidebar()
})

// 根据用户类型显示不同菜单
const userType = computed(() => userStore.userType)
const adminRole = computed(() => userStore.adminRole)

// 学生菜单
const studentMenus = [
  { path: '/student/dashboard', title: '主页', icon: 'HomeFilled' },
  { path: '/student/profile', title: '个人信息', icon: 'User' },
  { path: '/student/status', title: '学籍信息', icon: 'Document' },
  { path: '/student/award', title: '奖励申请', icon: 'Trophy' },
  { path: '/student/punishment', title: '处分查看', icon: 'Warning' },
  { path: '/student/appeal', title: '申诉管理', icon: 'ChatDotRound' },
  { path: '/student/status-change', title: '学籍变动', icon: 'Switch' },
  { path: '/student/leave', title: '离校手续', icon: 'Promotion' },
  { path: '/student/notice', title: '系统通知', icon: 'Bell' },
  { path: '/student/user-center', title: '个人中心', icon: 'Setting' }
]

// 辅导员菜单
const advisorMenus = [
  { path: '/advisor/dashboard', title: '主页', icon: 'HomeFilled' },
  { path: '/advisor/students', title: '学生列表', icon: 'User' },
  { path: '/advisor/award-review', title: '奖励审批', icon: 'Trophy' },
  { path: '/advisor/appeal-review', title: '申诉审理', icon: 'ChatDotRound' },
  { path: '/advisor/status-change-review', title: '学籍变动审核', icon: 'Switch' },
  { path: '/advisor/notice', title: '系统通知', icon: 'Bell' },
  { path: '/advisor/user-center', title: '个人中心', icon: 'Setting' }
]

// 管理员菜单 - 根据角色动态生成
const adminMenus = computed(() => {
  const role = adminRole.value
  
  // 基础菜单（所有管理员都有）
  const baseMenus = [
    { path: '/admin/dashboard', title: '主页', icon: 'HomeFilled' },
  ]
  
  // 教务管理员拥有全部功能
  if (role === '教务管理员') {
    return [
      ...baseMenus,
      { path: '/admin/students', title: '学生管理', icon: 'User' },
      { path: '/admin/award-review', title: '奖励终审', icon: 'Trophy' },
      { path: '/admin/punishment', title: '处分审批', icon: 'Warning' },
      { path: '/admin/appeal-review', title: '申诉终审', icon: 'ChatDotRound' },
      { path: '/admin/status-change-review', title: '学籍变动终审', icon: 'Switch' },
      { path: '/admin/leave-review', title: '离校审核', icon: 'Promotion' },
      { path: '/admin/notice', title: '通知管理', icon: 'Bell' },
      { path: '/admin/log', title: '操作日志', icon: 'Notebook' },
      { path: '/admin/user-center', title: '个人中心', icon: 'Setting' }
    ]
  }
  
  // 宿管/图书馆/财务处管理员：离校审核 + 处分申请
  return [
    ...baseMenus,
    { path: '/admin/punishment', title: '处分申请', icon: 'Warning' },
    { path: '/admin/leave-review', title: '离校审核', icon: 'Promotion' },
    { path: '/admin/user-center', title: '个人中心', icon: 'Setting' }
  ]
})

const currentMenus = computed(() => {
  switch (userType.value) {
    case 'student': return studentMenus
    case 'advisor': return advisorMenus
    case 'admin': return adminMenus.value
    default: return []
  }
})
</script>

<template>
  <aside 
    class="pdm-sidebar pdm-sidebar--dark"
    :class="{
      'pdm-sidebar--collapsed': sidebarCollapsed && !isMobile,
      'pdm-sidebar--mobile': isMobile,
      'pdm-sidebar--mobile-visible': isMobile && sidebarVisible
    }"
    :style="{ top: `${headerHeight}px` }"
  >
    <div class="pdm-sidebar__inner">
      <el-menu
        :default-active="route.path"
        class="pdm-sidebar__menu"
        :collapse="sidebarCollapsed && !isMobile"
        router
      >
        <el-menu-item
          v-for="menu in currentMenus"
          :key="menu.path"
          :index="menu.path"
        >
          <el-icon class="pdm-sidebar__menu-icon">
            <component :is="menu.icon" />
          </el-icon>
          <template #title>
            <span>{{ menu.title }}</span>
          </template>
        </el-menu-item>
      </el-menu>
    </div>
  </aside>
</template>

<style scoped lang="less">
// 侧边栏过渡动画
.pdm-sidebar {
  transition: width 0.3s ease;
  
  .pdm-sidebar__inner {
    transition: width 0.3s ease;
  }
  
  .pdm-sidebar__menu.el-menu {
    transition: width 0.3s ease;
  }
}

.pdm-sidebar--collapsed {
  width: 64px !important;
  
  .pdm-sidebar__inner {
    width: 64px !important;
    overflow-y: hidden !important; // 隐藏垂直滚动条
  }
  
  .pdm-sidebar__menu.el-menu {
    width: 64px !important;
  }
}

.pdm-sidebar--mobile {
  transform: translateX(-100%);
  transition: transform 0.3s ease;
  z-index: 1001;
  top: 60px !important;
}

.pdm-sidebar--mobile-visible {
  transform: translateX(0);
}
</style>
