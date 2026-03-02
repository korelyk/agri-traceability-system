<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
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
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <breadcrumb />
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ user?.realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'

export default {
  name: 'Layout',
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()
    
    const user = computed(() => store.state.user)
    
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
          break
        case 'settings':
          break
      }
    }
    
    return {
      user,
      menuItems,
      handleCommand
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
</style>
