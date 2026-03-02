<template>
  <div class="statistics-page">
    <!-- 系统概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>产品统计</span>
          </template>
          <div class="chart-container" ref="productChart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>溯源记录统计</span>
          </template>
          <div class="chart-container" ref="traceChart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>用户统计</span>
          </template>
          <div class="chart-container" ref="userChart"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 区块链统计 -->
    <el-card class="blockchain-stats">
      <template #header>
        <span>区块链统计</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card-item">
            <el-icon size="48" color="#409EFF"><Link /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics?.totalBlocks || 0 }}</div>
              <div class="stat-label">总区块数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card-item">
            <el-icon size="48" color="#67C23A"><DocumentChecked /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics?.totalTransactions || 0 }}</div>
              <div class="stat-label">总交易数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card-item">
            <el-icon size="48" color="#E6A23C"><Timer /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics?.pendingTransactions || 0 }}</div>
              <div class="stat-label">待处理交易</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card-item">
            <el-icon size="48" color="#F56C6C"><Collection /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ statistics?.productsTraced || 0 }}</div>
              <div class="stat-label">溯源产品数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 详细数据表 -->
    <el-row :gutter="20" class="detail-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>产品类别分布</span>
          </template>
          <el-table :data="categoryData" style="width: 100%">
            <el-table-column prop="category" label="类别" />
            <el-table-column prop="count" label="数量" />
            <el-table-column label="占比">
              <template #default="scope">
                <el-progress :percentage="scope.row.percentage" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>操作类型分布</span>
          </template>
          <el-table :data="operationData" style="width: 100%">
            <el-table-column prop="operation" label="操作类型" />
            <el-table-column prop="count" label="数量" />
            <el-table-column label="占比">
              <template #default="scope">
                <el-progress :percentage="scope.row.percentage" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'

export default {
  name: 'Statistics',
  setup() {
    const store = useStore()
    const productChart = ref(null)
    const traceChart = ref(null)
    const userChart = ref(null)
    
    const statistics = computed(() => store.state.statistics)
    
    const categoryData = ref([
      { category: '水果', count: 45, percentage: 30 },
      { category: '蔬菜', count: 38, percentage: 25 },
      { category: '粮食', count: 30, percentage: 20 },
      { category: '茶叶', count: 23, percentage: 15 },
      { category: '其他', count: 15, percentage: 10 }
    ])
    
    const operationData = ref([
      { operation: '生产', count: 150, percentage: 35 },
      { operation: '加工', count: 100, percentage: 23 },
      { operation: '运输', count: 80, percentage: 19 },
      { operation: '仓储', count: 60, percentage: 14 },
      { operation: '销售', count: 40, percentage: 9 }
    ])
    
    onMounted(() => {
      store.dispatch('fetchStatistics')
      store.dispatch('fetchBlockchainInfo')
    })
    
    return {
      productChart,
      traceChart,
      userChart,
      statistics,
      categoryData,
      operationData
    }
  }
}
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}

.overview-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 250px;
}

.blockchain-stats {
  margin-bottom: 20px;
}

.stat-card-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-card-item .el-icon {
  margin-right: 15px;
}

.stat-card-item .stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-card-item .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.detail-row {
  margin-top: 20px;
}
</style>
