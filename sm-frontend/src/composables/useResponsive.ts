import { ref, onMounted, onUnmounted, computed } from 'vue'

// 断点定义
const BREAKPOINTS = {
  xs: 480,
  sm: 768,
  md: 992,
  lg: 1200,
  xl: 1600
}

// 全局响应式状态
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)
const sidebarCollapsed = ref(false)
const sidebarVisible = ref(true)

export function useResponsive() {
  // 屏幕尺寸判断
  const isMobile = computed(() => windowWidth.value < BREAKPOINTS.sm)
  const isTablet = computed(() => windowWidth.value >= BREAKPOINTS.sm && windowWidth.value < BREAKPOINTS.lg)
  const isDesktop = computed(() => windowWidth.value >= BREAKPOINTS.lg)
  
  // 当前断点
  const currentBreakpoint = computed(() => {
    const width = windowWidth.value
    if (width < BREAKPOINTS.xs) return 'xs'
    if (width < BREAKPOINTS.sm) return 'sm'
    if (width < BREAKPOINTS.md) return 'md'
    if (width < BREAKPOINTS.lg) return 'lg'
    return 'xl'
  })

  // 侧边栏宽度
  const sidebarWidth = computed(() => {
    if (isMobile.value) return 0
    if (sidebarCollapsed.value) return 64
    return 210
  })

  // 顶栏高度
  const headerHeight = computed(() => isMobile.value ? 60 : 90)

  // 更新窗口宽度
  const updateWidth = () => {
    windowWidth.value = window.innerWidth
    // 移动端默认隐藏侧边栏
    if (isMobile.value) {
      sidebarVisible.value = false
    } else {
      sidebarVisible.value = true
    }
  }

  // 切换侧边栏折叠状态
  const toggleSidebar = () => {
    if (isMobile.value) {
      sidebarVisible.value = !sidebarVisible.value
    } else {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }
  }

  // 关闭移动端侧边栏
  const closeMobileSidebar = () => {
    if (isMobile.value) {
      sidebarVisible.value = false
    }
  }

  onMounted(() => {
    updateWidth()
    window.addEventListener('resize', updateWidth)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateWidth)
  })

  return {
    windowWidth,
    isMobile,
    isTablet,
    isDesktop,
    currentBreakpoint,
    sidebarCollapsed,
    sidebarVisible,
    sidebarWidth,
    headerHeight,
    toggleSidebar,
    closeMobileSidebar
  }
}
