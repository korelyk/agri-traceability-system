<template>
  <div class="dashboard">
    <el-row :gutter="20" class="statistics-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #409EFF;">
            <el-icon size="32" color="#fff"><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics?.totalProducts || 0 }}</div>
            <div class="stat-label">产品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #67C23A;">
            <el-icon size="32" color="#fff"><DocumentChecked /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics?.totalTraceRecords || 0 }}</div>
            <div class="stat-label">溯源记录</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #E6A23C;">
            <el-icon size="32" color="#fff"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics?.totalUsers || 0 }}</div>
            <div class="stat-label">注册用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: #909399;">
            <el-icon size="32" color="#fff"><Link /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ blockchainInfo?.totalBlocks || 0 }}</div>
            <div class="stat-label">区块数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="status-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>区块链状态</span>
              <el-tag :type="blockchainInfo?.chainValid ? 'success' : 'danger'">
                {{ blockchainInfo?.chainValid ? '运行正常' : '状态异常' }}
              </el-tag>
            </div>
          </template>
          <div class="blockchain-status">
            <div class="status-item">
              <span class="status-label">链有效性</span>
              <el-icon :size="20" :color="blockchainInfo?.chainValid ? '#67C23A' : '#F56C6C'">
                <CircleCheck v-if="blockchainInfo?.chainValid" />
                <CircleClose v-else />
              </el-icon>
            </div>
            <div class="status-item">
              <span class="status-label">总区块数</span>
              <span class="status-value">{{ blockchainInfo?.totalBlocks || 0 }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">总交易数</span>
              <span class="status-value">{{ blockchainInfo?.totalTransactions || 0 }}</span>
            </div>
            <div class="status-item">
              <span class="status-label">挖矿难度</span>
              <span class="status-value">{{ blockchainInfo?.difficulty || 4 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统公告</span>
            </div>
          </template>
          <div class="announcements">
            <div class="announcement-item">
              <el-icon color="#409EFF"><InfoFilled /></el-icon>
              <span>欢迎使用农产品防伪溯源系统。</span>
            </div>
            <div class="announcement-item">
              <el-icon color="#67C23A"><SuccessFilled /></el-icon>
              <span>当前区块链校验状态正常，可用于答辩演示。</span>
            </div>
            <div class="announcement-item">
              <el-icon color="#E6A23C"><WarningFilled /></el-icon>
              <span>请妥善保管管理员和演示账号密码。</span>
            </div>
            <div class="announcement-item">
              <el-icon color="#909399"><QuestionFilled /></el-icon>
              <span>建议答辩前先走一遍完整演示流程。</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="quick-actions">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="$router.push('/products/register')">
          <el-icon><Plus /></el-icon>
          注册产品
        </el-button>
        <el-button type="success" size="large" @click="$router.push('/trace/add')">
          <el-icon><Edit /></el-icon>
          新增溯源记录
        </el-button>
        <el-button type="info" size="large" @click="$router.push('/trace')">
          <el-icon><Search /></el-icon>
          溯源查询
        </el-button>
        <el-button type="warning" size="large" @click="$router.push('/blockchain')">
          <el-icon><View /></el-icon>
          查看区块链
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { computed, onMounted } from 'vue'
import { useStore } from 'vuex'

export default {
  name: 'Dashboard',
  setup() {
    const store = useStore()

    const statistics = computed(() => store.state.statistics)
    const blockchainInfo = computed(() => store.state.blockchainInfo)

    onMounted(() => {
      store.dispatch('fetchStatistics')
      store.dispatch('fetchBlockchainInfo')
    })

    return {
      statistics,
      blockchainInfo
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.statistics-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.status-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.blockchain-status {
  padding: 10px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}

.status-item:last-child {
  border-bottom: none;
}

.status-label {
  color: #606266;
}

.status-value {
  font-weight: bold;
  color: #303133;
}

.announcements {
  padding: 10px;
}

.announcement-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-item .el-icon {
  margin-right: 10px;
}

.quick-actions {
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  padding: 20px;
}

.action-buttons .el-button {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
