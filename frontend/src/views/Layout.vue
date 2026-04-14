<template>
  <el-container class="layout-shell">
    <el-aside width="272px" class="sidebar">
      <div class="brand-panel">
        <div class="brand-mark">
          <el-icon :size="22"><Connection /></el-icon>
        </div>
        <div>
          <div class="brand-title">农产品溯源平台</div>
          <div class="brand-subtitle">可信流转 · 链上存证 · 数字签名</div>
        </div>
      </div>

      <div class="sidebar-status">
        <span class="status-dot" :class="{ active: chainHealthy }" />
        <div>
          <div class="status-title">{{ chainHealthy ? '系统运行稳定' : '待人工核查' }}</div>
          <div class="status-text">
            {{ chainHealthy ? '链状态正常，适合展示业务流转与验签结果。' : '链校验存在异常，请先进入区块链浏览器核对。' }}
          </div>
        </div>
      </div>

      <el-menu
        :default-active="$route.path"
        router
        class="sidebar-menu"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-foot">
        <div class="sidebar-foot-label">公开访问地址</div>
        <div class="sidebar-foot-value">trace2026.yyy999.my</div>
      </div>
    </el-aside>

    <el-container class="content-shell">
      <el-header class="topbar">
        <div class="topbar-left">
          <div class="topbar-title-wrap">
            <div class="topbar-kicker">当前模块</div>
            <div class="topbar-title">{{ currentTitle }}</div>
          </div>
          <el-breadcrumb separator="·" class="topbar-breadcrumb">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="topbar-right">
          <div class="quick-chip">
            <span class="chip-label">认证方式</span>
            <span class="chip-value">JWT</span>
          </div>
          <div class="quick-chip">
            <span class="chip-label">签名算法</span>
            <span class="chip-value">RSA</span>
          </div>

          <el-dropdown @command="handleCommand">
            <button class="profile-button">
              <el-avatar :size="38" :icon="UserFilled" />
              <div class="profile-meta">
                <span class="profile-name">{{ displayName }}</span>
                <span class="profile-role">{{ user?.userTypeName || user?.userType || '系统用户' }}</span>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </button>

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

  <el-dialog v-model="profileVisible" title="个人信息" width="460px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="用户名">{{ user?.username || '-' }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ readableValue(user?.realName) }}</el-descriptions-item>
      <el-descriptions-item label="用户类型">{{ user?.userTypeName || user?.userType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="所属单位">{{ readableValue(user?.companyName) }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
      <el-descriptions-item label="电话">{{ user?.phone || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>

  <el-dialog v-model="settingsVisible" title="系统设置" width="540px">
    <el-alert
      type="info"
      :closable="false"
      title="当前为成品展示环境，以下配置用于说明系统的可信追溯机制与线上接入方式。"
      class="settings-alert"
    />
    <el-descriptions :column="1" border>
      <el-descriptions-item label="前端模式">Web 成品界面</el-descriptions-item>
      <el-descriptions-item label="接口地址">同源 /api 代理</el-descriptions-item>
      <el-descriptions-item label="认证方式">JWT Bearer Token</el-descriptions-item>
      <el-descriptions-item label="签名算法">RSA / SHA256withRSA</el-descriptions-item>
      <el-descriptions-item label="链状态存储">持久化区块链状态文件</el-descriptions-item>
      <el-descriptions-item label="公开访问地址">https://trace2026.yyy999.my</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Connection, UserFilled } from '@element-plus/icons-vue'

function isReadableText(value) {
  return value && typeof value === 'string' && value.trim() && !/^\?+$/.test(value)
}

export default {
  name: 'Layout',
  components: {
    ArrowDown,
    Connection,
    UserFilled
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()

    const profileVisible = ref(false)
    const settingsVisible = ref(false)
    const user = computed(() => store.state.user)
    const blockchainInfo = computed(() => store.state.blockchainInfo || {})

    const readableValue = (value) => (isReadableText(value) ? value : '-')

    const displayName = computed(() => {
      const currentUser = user.value
      if (!currentUser) {
        return '系统用户'
      }
      const candidates = [
        currentUser.realName,
        currentUser.username,
        currentUser.companyName
      ]
      return candidates.find((value) => isReadableText(value)) || '系统用户'
    })

    const breadcrumbs = computed(() => route.matched
      .filter((item) => item.meta?.title)
      .map((item) => ({
        path: item.path,
        title: item.meta.title
      })))

    const currentTitle = computed(() => breadcrumbs.value.at(-1)?.title || '系统总览')
    const chainHealthy = computed(() => blockchainInfo.value?.chainValid !== false)

    const menuItems = [
      { path: '/dashboard', title: '系统总览', icon: 'Odometer' },
      { path: '/products', title: '产品管理', icon: 'Goods' },
      { path: '/trace', title: '溯源查询', icon: 'Search' },
      { path: '/blockchain', title: '区块链浏览器', icon: 'Link' },
      { path: '/supervision', title: '质量监管', icon: 'DataAnalysis' },
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
      }
    }

    onMounted(() => {
      if (!store.state.blockchainInfo) {
        store.dispatch('fetchBlockchainInfo')
      }
    })

    return {
      breadcrumbs,
      chainHealthy,
      currentTitle,
      displayName,
      handleCommand,
      menuItems,
      profileVisible,
      readableValue,
      settingsVisible,
      user,
      UserFilled
    }
  }
}
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
}

.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  padding: 22px 18px 18px;
  background:
    radial-gradient(circle at top, rgba(79, 146, 108, 0.22), transparent 28%),
    linear-gradient(180deg, #173b2b 0%, #11291e 100%);
  color: #f3fbf5;
  box-shadow: 18px 0 40px rgba(17, 41, 30, 0.18);
}

.brand-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 8px 20px;
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(135deg, #5dbb83, #2f7d58);
  color: #fff;
  box-shadow: 0 12px 22px rgba(39, 97, 68, 0.35);
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.brand-subtitle {
  margin-top: 4px;
  color: rgba(240, 247, 242, 0.72);
  font-size: 12px;
}

.sidebar-status {
  display: flex;
  gap: 12px;
  padding: 16px;
  margin-bottom: 18px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.06);
}

.status-dot {
  width: 12px;
  height: 12px;
  margin-top: 4px;
  border-radius: 50%;
  background: #f0ad4e;
  box-shadow: 0 0 0 6px rgba(240, 173, 78, 0.18);
}

.status-dot.active {
  background: #77d28e;
  box-shadow: 0 0 0 6px rgba(119, 210, 142, 0.18);
}

.status-title {
  font-size: 14px;
  font-weight: 700;
}

.status-text {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.7;
  color: rgba(240, 247, 242, 0.74);
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 50px;
  margin-bottom: 8px;
  border-radius: 16px;
  color: rgba(240, 247, 242, 0.82);
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
}

.sidebar-menu :deep(.is-active) {
  background: linear-gradient(135deg, rgba(93, 187, 131, 0.22), rgba(196, 138, 67, 0.18));
  color: #ffffff;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.08);
}

.sidebar-foot {
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.05);
}

.sidebar-foot-label {
  color: rgba(240, 247, 242, 0.64);
  font-size: 12px;
}

.sidebar-foot-value {
  margin-top: 6px;
  word-break: break-all;
  font-size: 13px;
  font-weight: 600;
}

.content-shell {
  margin-left: 272px;
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 998;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 84px;
  padding: 18px 28px;
  background: rgba(245, 247, 242, 0.82);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(35, 67, 52, 0.08);
}

.topbar-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.topbar-kicker {
  color: #6a7b71;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.topbar-title {
  font-size: 26px;
  font-weight: 700;
  color: #193026;
}

.topbar-breadcrumb {
  color: #7b8a80;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.quick-chip {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 14px;
  border: 1px solid rgba(47, 125, 88, 0.12);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.68);
}

.chip-label {
  color: #7b8a80;
  font-size: 11px;
}

.chip-value {
  font-size: 13px;
  font-weight: 700;
}

.profile-button {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px 8px 8px;
  border: 1px solid rgba(47, 125, 88, 0.12);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  color: inherit;
  cursor: pointer;
}

.profile-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.profile-name {
  font-size: 14px;
  font-weight: 700;
}

.profile-role {
  color: #74847a;
  font-size: 12px;
}

.main-content {
  padding: 28px;
}

.settings-alert {
  margin-bottom: 16px;
}

@media (max-width: 1200px) {
  .quick-chip {
    display: none;
  }
}

@media (max-width: 992px) {
  .sidebar {
    position: static;
    width: 100%;
    height: auto;
  }

  .content-shell {
    margin-left: 0;
  }

  .layout-shell {
    flex-direction: column;
  }

  .topbar {
    position: static;
    align-items: flex-start;
    flex-direction: column;
  }

  .topbar-right {
    width: 100%;
    justify-content: space-between;
  }

  .main-content {
    padding: 20px;
  }
}

@media (max-width: 640px) {
  .profile-role,
  .topbar-breadcrumb {
    display: none;
  }

  .profile-button {
    padding-right: 10px;
  }

  .topbar-title {
    font-size: 22px;
  }
}
</style>
