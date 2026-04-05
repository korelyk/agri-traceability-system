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
              <span class="status-label with-help">
                挖矿难度
                <el-tooltip
                  content="表示新区块生成时需要满足的哈希计算门槛，当前难度 4 代表区块哈希前 4 位需为 0。"
                  placement="top"
                >
                  <el-icon class="help-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </span>
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
            <div v-for="item in announcements" :key="item.title" class="announcement-item">
              <div class="announcement-icon" :style="{ backgroundColor: item.softColor }">
                <el-icon :color="item.color">
                  <component :is="item.icon" />
                </el-icon>
              </div>
              <div class="announcement-content">
                <div class="announcement-title-row">
                  <span class="announcement-title">{{ item.title }}</span>
                  <el-tag size="small" :type="item.tagType">{{ item.tagText }}</el-tag>
                </div>
                <p class="announcement-text">{{ item.content }}</p>
              </div>
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
    const announcements = computed(() => [
      {
        title: '系统状态',
        content: blockchainInfo.value?.chainValid
          ? '当前链上校验通过，产品建档、流转记录、公开查询与区块验证服务运行正常。'
          : '当前链状态异常，建议及时核查服务状态、链校验结果与数据同步情况。',
        icon: blockchainInfo.value?.chainValid ? 'SuccessFilled' : 'WarningFilled',
        color: blockchainInfo.value?.chainValid ? '#67C23A' : '#E6A23C',
        softColor: blockchainInfo.value?.chainValid ? '#F0F9EB' : '#FDF6EC',
        tagType: blockchainInfo.value?.chainValid ? 'success' : 'warning',
        tagText: blockchainInfo.value?.chainValid ? '正常' : '预警'
      },
      {
        title: '业务流程',
        content: '系统已覆盖产品建档、加工流转、公开溯源、链上校验等核心环节，支持完整业务闭环管理。',
        icon: 'InfoFilled',
        color: '#409EFF',
        softColor: '#ECF5FF',
        tagType: 'info',
        tagText: '已启用'
      },
      {
        title: '数据质量',
        content: '当前产品档案、主体信息、产地信息与流转记录已完成规范化处理，支持统一中文展示与查询。',
        icon: 'DocumentChecked',
        color: '#8E44AD',
        softColor: '#F4ECF7',
        tagType: 'success',
        tagText: '已校准'
      },
      {
        title: '运维提示',
        content: '建议定期检查账号鉴权状态、服务健康接口与公开访问链路，确保系统持续稳定运行。',
        icon: 'Bell',
        color: '#909399',
        softColor: '#F4F4F5',
        tagType: 'info',
        tagText: '关注'
      }
    ])

    onMounted(() => {
      store.dispatch('fetchStatistics')
      store.dispatch('fetchBlockchainInfo')
    })

    return {
      statistics,
      blockchainInfo,
      announcements
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

.with-help {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.help-icon {
  color: #909399;
  cursor: help;
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
  align-items: flex-start;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #EBEEF5;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.announcement-content {
  flex: 1;
  min-width: 0;
}

.announcement-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.announcement-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.announcement-text {
  margin: 8px 0 0;
  line-height: 1.65;
  color: #606266;
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
