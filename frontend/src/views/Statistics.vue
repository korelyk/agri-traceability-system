<template>
  <div class="statistics-page">
    <el-row :gutter="20" class="summary-row">
      <el-col :span="6" v-for="item in summaryCards" :key="item.label">
        <el-card class="summary-card" shadow="hover">
          <div class="summary-item">
            <el-icon :size="36" :color="item.color">
              <component :is="item.icon" />
            </el-icon>
            <div class="summary-content">
              <div class="summary-value">{{ item.value }}</div>
              <div class="summary-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="summary-row">
      <el-col :span="6" v-for="item in blockchainCards" :key="item.label">
        <el-card class="summary-card" shadow="hover">
          <div class="summary-item">
            <el-icon :size="36" :color="item.color">
              <component :is="item.icon" />
            </el-icon>
            <div class="summary-content">
              <div class="summary-value">{{ item.value }}</div>
              <div class="summary-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>产品类别分布</template>
          <el-empty v-if="categoryRows.length === 0" description="暂无产品数据" />
          <el-table v-else :data="categoryRows" stripe>
            <el-table-column prop="label" label="类别" />
            <el-table-column prop="count" label="数量" width="100" />
            <el-table-column label="占比">
              <template #default="{ row }">
                <el-progress :percentage="row.percentage" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header>产品状态分布</template>
          <el-empty v-if="statusRows.length === 0" description="暂无状态数据" />
          <el-table v-else :data="statusRows" stripe>
            <el-table-column prop="label" label="状态" />
            <el-table-column prop="count" label="数量" width="100" />
            <el-table-column label="占比">
              <template #default="{ row }">
                <el-progress :percentage="row.percentage" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header>操作类型分布</template>
          <el-empty v-if="operationRows.length === 0" description="暂无溯源记录" />
          <el-table v-else :data="operationRows" stripe>
            <el-table-column prop="label" label="操作类型" />
            <el-table-column prop="count" label="数量" width="100" />
            <el-table-column label="占比">
              <template #default="{ row }">
                <el-progress :percentage="row.percentage" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="summary-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>用户类型统计</template>
          <el-empty v-if="userRows.length === 0" description="暂无用户数据" />
          <el-table v-else :data="userRows" stripe>
            <el-table-column prop="label" label="用户类型" />
            <el-table-column prop="count" label="数量" width="100" />
            <el-table-column label="占比">
              <template #default="{ row }">
                <el-progress :percentage="row.percentage" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <template #header>区块链运行状态</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="链状态">
              <el-tag :type="blockchainInfo?.chainValid ? 'success' : 'danger'">
                {{ blockchainInfo?.chainValid ? '正常' : '异常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前难度">
              {{ blockchainInfo?.difficulty ?? 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="待处理交易">
              {{ blockchainInfo?.pendingTransactions ?? 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="已追踪产品">
              {{ blockchainInfo?.productsTraced ?? 0 }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { computed, onMounted } from 'vue'
import { useStore } from 'vuex'

function toRows(mapData, labelMap = {}) {
  const entries = Object.entries(mapData || {})
  const total = entries.reduce((sum, [, count]) => sum + Number(count || 0), 0)
  return entries
    .map(([key, count]) => ({
      key,
      label: labelMap[key] || key,
      count: Number(count || 0),
      percentage: total > 0 ? Math.round((Number(count || 0) / total) * 100) : 0
    }))
    .sort((a, b) => b.count - a.count)
}

export default {
  name: 'Statistics',
  setup() {
    const store = useStore()

    const statistics = computed(() => store.state.statistics || {})
    const blockchainInfo = computed(() => store.state.blockchainInfo || {})

    const summaryCards = computed(() => [
      { label: '产品总数', value: statistics.value.totalProducts || 0, icon: 'Goods', color: '#409EFF' },
      { label: '溯源记录', value: statistics.value.totalTraceRecords || 0, icon: 'DocumentChecked', color: '#67C23A' },
      { label: '用户总数', value: statistics.value.totalUsers || 0, icon: 'User', color: '#E6A23C' },
      { label: '区块总数', value: statistics.value.totalBlocks || 0, icon: 'Collection', color: '#F56C6C' }
    ])

    const blockchainCards = computed(() => [
      { label: '链上交易', value: blockchainInfo.value.totalTransactions || 0, icon: 'Promotion', color: '#409EFF' },
      { label: '待处理交易', value: blockchainInfo.value.pendingTransactions || 0, icon: 'Timer', color: '#E6A23C' },
      { label: '已追踪产品', value: blockchainInfo.value.productsTraced || 0, icon: 'Tickets', color: '#67C23A' },
      { label: '链验证状态', value: blockchainInfo.value.chainValid ? '正常' : '异常', icon: 'CircleCheck', color: blockchainInfo.value.chainValid ? '#67C23A' : '#F56C6C' }
    ])

    const categoryRows = computed(() => toRows(statistics.value.productsByCategory))
    const statusRows = computed(() => toRows(statistics.value.productsByStatus, {
      CREATED: '已创建',
      PROCESS: '加工中',
      TRANSPORT: '运输中',
      STORAGE: '仓储中',
      SALE: '销售中'
    }))
    const operationRows = computed(() => toRows(statistics.value.recordsByOperationType, {
      PRODUCE: '生产',
      PROCESS: '加工',
      TRANSPORT: '运输',
      STORAGE: '仓储',
      SALE: '销售',
      INSPECT: '检测',
      PACKAGE: '包装'
    }))
    const userRows = computed(() => toRows(statistics.value.usersByType, {
      PRODUCER: '生产者',
      PROCESSOR: '加工商',
      LOGISTICS: '物流商',
      RETAILER: '销售商',
      INSPECTOR: '检测机构',
      ADMIN: '管理员'
    }))

    onMounted(async () => {
      await Promise.all([
        store.dispatch('fetchStatistics'),
        store.dispatch('fetchBlockchainInfo')
      ])
    })

    return {
      blockchainCards,
      blockchainInfo,
      categoryRows,
      operationRows,
      statistics,
      statusRows,
      summaryCards,
      userRows
    }
  }
}
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  min-height: 110px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.summary-content {
  display: flex;
  flex-direction: column;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-top: 6px;
}
</style>
