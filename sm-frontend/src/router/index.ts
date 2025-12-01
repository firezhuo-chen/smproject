import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 布局组件
import MainLayout from '@/layout/MainLayout.vue'

// 页面组件
import LoginView from '@/views/LoginView.vue'
import EmptyView from '@/views/common/EmptyView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/login'
  },
  // 学生端路由
  {
    path: '/student',
    component: MainLayout,
    meta: { requiresAuth: true, role: 'student' },
    redirect: '/student/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/DashboardView.vue'),
        meta: { title: '主页' }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/views/student/ProfileView.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'status',
        name: 'StudentStatus',
        component: () => import('@/views/student/StatusView.vue'),
        meta: { title: '学籍信息' }
      },
      {
        path: 'award',
        name: 'StudentAward',
        component: () => import('@/views/student/AwardView.vue'),
        meta: { title: '奖励申请' }
      },
      {
        path: 'punishment',
        name: 'StudentPunishment',
        component: () => import('@/views/student/PunishmentView.vue'),
        meta: { title: '处分查看' }
      },
      {
        path: 'appeal',
        name: 'StudentAppeal',
        component: () => import('@/views/student/AppealView.vue'),
        meta: { title: '申诉管理' }
      },
      {
        path: 'status-change',
        name: 'StudentStatusChange',
        component: () => import('@/views/student/StatusChangeView.vue'),
        meta: { title: '学籍变动' }
      },
      {
        path: 'leave',
        name: 'StudentLeave',
        component: () => import('@/views/student/LeaveView.vue'),
        meta: { title: '离校手续' }
      },
      {
        path: 'notice',
        name: 'StudentNotice',
        component: () => import('@/views/student/NoticeView.vue'),
        meta: { title: '系统通知' }
      },
      {
        path: 'user-center',
        name: 'StudentUserCenter',
        component: () => import('@/views/common/UserCenterView.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // 辅导员端路由
  {
    path: '/advisor',
    component: MainLayout,
    meta: { requiresAuth: true, role: 'advisor' },
    redirect: '/advisor/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdvisorDashboard',
        component: () => import('@/views/advisor/DashboardView.vue'),
        meta: { title: '主页' }
      },
      {
        path: 'students',
        name: 'AdvisorStudents',
        component: () => import('@/views/advisor/StudentsView.vue'),
        meta: { title: '学生列表' }
      },
      {
        path: 'award-review',
        name: 'AdvisorAwardReview',
        component: () => import('@/views/advisor/AwardReviewView.vue'),
        meta: { title: '奖励审批' }
      },
      {
        path: 'appeal-review',
        name: 'AdvisorAppealReview',
        component: () => import('@/views/advisor/AppealReviewView.vue'),
        meta: { title: '申诉审理' }
      },
      {
        path: 'status-change-review',
        name: 'AdvisorStatusChangeReview',
        component: () => import('@/views/advisor/StatusChangeReviewView.vue'),
        meta: { title: '学籍变动审核' }
      },
      {
        path: 'notice',
        name: 'AdvisorNotice',
        component: () => import('@/views/advisor/NoticeView.vue'),
        meta: { title: '系统通知' }
      },
      {
        path: 'user-center',
        name: 'AdvisorUserCenter',
        component: () => import('@/views/common/UserCenterView.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // 管理员端路由
  {
    path: '/admin',
    component: MainLayout,
    meta: { requiresAuth: true, role: 'admin' },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '主页' }
      },
      {
        path: 'students',
        name: 'AdminStudents',
        component: () => import('@/views/admin/StudentsView.vue'),
        meta: { title: '学生管理' }
      },
      {
        path: 'award-review',
        name: 'AdminAwardReview',
        component: () => import('@/views/admin/AwardReviewView.vue'),
        meta: { title: '奖励终审' }
      },
      {
        path: 'punishment',
        name: 'AdminPunishment',
        component: () => import('@/views/admin/PunishmentView.vue'),
        meta: { title: '处分管理' }
      },
      {
        path: 'appeal-review',
        name: 'AdminAppealReview',
        component: () => import('@/views/admin/AppealReviewView.vue'),
        meta: { title: '申诉终审' }
      },
      {
        path: 'status-change-review',
        name: 'AdminStatusChangeReview',
        component: () => import('@/views/admin/StatusChangeReviewView.vue'),
        meta: { title: '学籍变动终审' }
      },
      {
        path: 'leave-review',
        name: 'AdminLeaveReview',
        component: () => import('@/views/admin/LeaveReviewView.vue'),
        meta: { title: '离校审核' }
      },
      {
        path: 'notice',
        name: 'AdminNotice',
        component: () => import('@/views/admin/NoticeView.vue'),
        meta: { title: '通知管理' }
      },
      {
        path: 'log',
        name: 'AdminLog',
        component: () => import('@/views/admin/LogView.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'user-center',
        name: 'AdminUserCenter',
        component: () => import('@/views/common/UserCenterView.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userType = localStorage.getItem('userType')
  
  // 设置页面标题
  document.title = (to.meta.title as string) || '学生学籍管理系统'
  
  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    next()
    return
  }
  
  // 需要认证但未登录
  if (!token) {
    next('/login')
    return
  }
  
  // 检查角色权限
  const routeRole = to.matched.find(record => record.meta.role)?.meta.role
  if (routeRole && routeRole !== userType) {
    // 角色不匹配，跳转到对应角色首页
    switch (userType) {
      case 'student':
        next('/student/dashboard')
        break
      case 'advisor':
        next('/advisor/dashboard')
        break
      case 'admin':
        next('/admin/dashboard')
        break
      default:
        next('/login')
    }
    return
  }
  
  next()
})

export default router
