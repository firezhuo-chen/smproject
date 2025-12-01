<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { useResponsive } from '@/composables/useResponsive'
import { confirmLogout } from '@/utils/confirm'

const userStore = useUserStore()
const router = useRouter()
const { isMobile, toggleSidebar, sidebarCollapsed } = useResponsive()

const handleLogout = async () => {
  const confirmed = await confirmLogout()
  if (!confirmed) return
  
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="header" :class="{ 'header--mobile': isMobile }">
    <div class="header-menu">
      <div class="header-left">
        <!-- 菜单切换按钮 -->
        <button 
          class="menu-toggle" 
          @click="toggleSidebar"
          :title="sidebarCollapsed ? '展开菜单' : '收起菜单'"
        >
          <el-icon :size="20" color="#fff">
            <Fold v-if="!sidebarCollapsed && !isMobile" />
            <Expand v-else />
          </el-icon>
        </button>
        <div class="header-logo">
          <el-icon :size="isMobile ? 24 : 32" color="#fff"><School /></el-icon>
          <span class="header-title" v-show="!isMobile">学籍管理系统</span>
          <span class="header-title header-title--short" v-show="isMobile">学籍系统</span>
          <img src="@/assets/logo(no title).png" alt="校徽" class="header-logo-img" v-show="!isMobile" />
        </div>
      </div>
      <div class="header-rinfo">
        <el-icon><User /></el-icon>
        <span class="user-name">{{ userStore.userInfo?.userName || '未登录' }}</span>
        <a class="header-exit" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span class="exit-text">退出</span>
        </a>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.header-left {
  display: flex;
  align-items: center;
  height: 100%;
}

.menu-toggle {
  margin-right: 16px;
  padding: 10px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s ease;
  min-width: 40px;
  min-height: 40px;
  
  &:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: scale(1.05);
  }
  
  &:active {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(0.98);
  }
}

.header--mobile {
  height: 60px !important;
  padding: 0 8px !important;
  
  .header-menu {
    height: 60px !important;
    gap: 8px; // 元素间距
  }
  
  .header-left {
    flex: 1;
    min-width: 0; // 允许收缩
  }
  
  .menu-toggle {
    margin-right: 8px;
    padding: 6px;
    min-width: 32px;
    min-height: 32px;
  }
  
  .header-logo {
    flex: 1;
    min-width: 0;
    
    .el-icon {
      flex-shrink: 0;
    }
  }
  
  .header-title--short {
    font-size: 14px !important;
    margin-left: 6px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .user-name {
    display: none;
  }
  
  .exit-text {
    display: none;
  }
  
  .header-rinfo {
    margin-right: 24px; // 翻倍右边距，防止被滚动条截断
    padding-right: 8px; // 翻倍安全边距
    flex-shrink: 0; // 防止被压缩
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      font-size: 18px;
    }
    
    .header-exit {
      padding: 8px;
      border-radius: 4px;
      transition: background-color 0.2s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }
    }
  }
}

.header-rinfo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-right: max(32px, calc(32px + env(scrollbar-gutter))); // 翻倍右边距
  padding-right: 8px; // 翻倍安全边距
  
  .header-exit {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    border-radius: 6px;
    transition: background-color 0.2s;
    text-decoration: none;
    color: inherit;
    
    &:hover {
      background: rgba(255, 255, 255, 0.1);
    }
  }
}

.user-name {
  white-space: nowrap;
}

.exit-text {
  white-space: nowrap;
}

.header-logo-img {
  height: 48px;
  width: auto;
  margin-left: 12px;
}
</style>
