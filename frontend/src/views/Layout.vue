<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon size="32" color="#67C23A"><Connection /></el-icon>
        <span class="logo-text">农产品溯源</span>
      </div>

      <el-menu
        :default-active="$route.path"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <breadcrumb />
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="profileVisible" title="个人信息" width="420px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="用户名">{{ user?.username || '-' }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ user?.realName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user?.userTypeName || user?.userType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="企业">{{ user?.companyName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
      <el-descriptions-item label="电话">{{ user?.phone || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>

  <el-dialog v-model="settingsVisible" title="系统设置" width="460px">
    <el-alert
      type="info"
      :closable="false"
      title="当前为毕业设计演示版，以下内容用于展示当前运行配置。"
      class="settings-alert"
    />
    <el-descriptions :column="1" border>
      <el-descriptions-item label="前端模式">Web 演示版</el-descriptions-item>
      <el-descriptions-item label="接口地址">同源 /api 代理</el-descriptions-item>
      <el-descriptions-item label="认证方式">JWT Bearer Token</el-descriptions-item>
      <el-descriptions-item label="区块链状态">本地持久化</el-descriptions-item>
      <el-descriptions-item label="部署域名">bishe.yyy999.my</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'Layout',
  setup() {
    const store = useStore()
    const router = useRouter()

    const profileVisible = ref(false)
    const settingsVisible = ref(false)
    const user = computed(() => store.state.user)
    const displayName = computed(() => {
      const currentUser = user.value
      if (!currentUser) {
        return '用户'
      }
      return currentUser.realName || currentUser.username || currentUser.companyName || '用户'
    })

    const menuItems = [
      { path: '/dashboard', title: '系统概览', icon: 'Odometer' },
      { path: '/products', title: '产品管理', icon: 'Goods' },
      { path: '/trace', title: '溯源查询', icon: 'Search' },
      { path: '/blockchain', title: '区块链浏览器', icon: 'Link' },
      { path: '/users', title: '用户管理', icon: 'User' },
      { path: '/statistics', title: '统计分析', icon: 'TrendCharts' }
    ]

    const handleCommand = (command) => {
      switch (command) {
        case 'logout':
          store.dispatch('logout')
          router.push('/login')
          break
        case 'profile':
          profileVisible.value = true
          break
        case 'settings':
          settingsVisible.value = true
          break
        default:
          ElMessage.info('功能暂未开放')
      }
    }

    return {
      displayName,
      handleCommand,
      menuItems,
      profileVisible,
      settingsVisible,
      user
    }
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.sidebar {
  background-color: #304156;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 15px;
  background-color: #2b3649;
  border-bottom: 1px solid #1f2d3d;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  margin-left: 10px;
}

.sidebar-menu {
  border-right: none;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: fixed;
  top: 0;
  right: 0;
  left: 220px;
  z-index: 999;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

.main-content {
  margin-left: 220px;
  margin-top: 60px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.settings-alert {
  margin-bottom: 16px;
}
</style>
