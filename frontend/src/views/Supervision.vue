<template>
  <div class="supervision-page" v-loading="loading">
    <el-row :gutter="20" class="summary-row">
      <el-col :span="6" v-for="item in summaryCards" :key="item.label">
        <el-card class="summary-card" shadow="hover">
          <div class="summary-item">
            <el-icon :size="34" :color="item.color">
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

    <el-row :gutter="20" class="panel-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>风险预警产品</span>
              <el-tag :type="warningProducts.length ? 'danger' : 'success'">
                {{ warningProducts.length ? `${warningProducts.length} 条预警` : '当前无预警' }}
              </el-tag>
            </div>
          </template>

          <el-empty v-if="warningProducts.length === 0" description="当前所有演示产品均通过监管校验" />

          <div v-else class="warning-list">
            <div v-for="item in warningProducts" :key="item.productId" class="warning-item">
              <div class="warning-head">
                <div>
                  <div class="warning-title">{{ item.productName }}</div>
                  <div class="warning-subtitle">{{ item.productId }} · {{ item.producerName || '主体未登记' }}</div>
                </div>
                <el-tag :type="riskTagType(item.riskLevel)">
                  {{ riskLabel(item.riskLevel) }}
                </el-tag>
              </div>

              <div class="warning-meta">
                <span>当前状态：{{ statusLabel(item.currentStatus) }}</span>
                <span>产地：{{ item.origin || '-' }}</span>
              </div>

              <div class="issue-tags">
                <el-tag
                  v-for="issue in item.issues || []"
                  :key="issue"
                  size="small"
                  effect="plain"
                  type="warning"
                >
                  {{ issue }}
                </el-tag>
              </div>

              <p class="warning-suggestion">{{ item.suggestion }}</p>

              <div class="warning-actions">
                <el-button link type="primary" @click="$router.push(`/trace/detail/${item.productId}`)">
                  查看溯源详情
                </el-button>
                <el-button
                  v-if="needsSupplement(item)"
                  link
                  type="warning"
                  @click="goSupplement(item.productId)"
                >
                  去补录记录
                </el-button>
                <el-button
                  v-if="item.latestInspection?.transactionId"
                  link
                  type="primary"
                  @click="$router.push(`/verify/transaction/${item.latestInspection.transactionId}`)"
                >
                  查看验签详情
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>可信监管机制</span>
            </div>
          </template>

          <div class="governance-list">
            <div v-for="(item, index) in governanceFocus" :key="index" class="governance-item">
              <div class="governance-index">{{ index + 1 }}</div>
              <div class="governance-text">{{ item }}</div>
            </div>
          </div>

          <el-alert
            class="chain-alert"
            :type="chainValid ? 'success' : 'warning'"
            :closable="false"
            :title="chainValid ? '当前整条链校验通过，可用于展示防篡改能力。' : '当前链状态存在异常，建议优先核查。'"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>近期检测记录</span>
          <el-tag type="info">参考国家平台“监管 + 检测”思路</el-tag>
        </div>
      </template>

      <el-empty v-if="recentInspections.length === 0" description="暂无检测记录，可通过新增溯源记录中的“检测”操作补录" />

      <el-table v-else :data="recentInspections" stripe>
        <el-table-column prop="productName" label="产品" min-width="180" />
        <el-table-column prop="qualityCheckResult" label="检测结果" width="110">
          <template #default="{ row }">
            <el-tag :type="inspectionTagType(row.qualityCheckResult)">
              {{ row.qualityCheckResult || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="certificateNo" label="证书编号" min-width="140" />
        <el-table-column label="文档哈希" min-width="220">
          <template #default="{ row }">
            <code class="hash-text">{{ shortHash(row.documentHash) }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="检测人" width="120" />
        <el-table-column prop="location" label="地点" width="140" />
        <el-table-column label="签名校验" width="110">
          <template #default="{ row }">
            <el-tag :type="row.verified ? 'success' : 'warning'">
              {{ row.verified ? '已验证' : '待核查' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.operationTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.transactionId"
              link
              type="primary"
              @click="$router.push(`/verify/transaction/${row.transactionId}`)"
            >
              验签详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { statusLabel } from '../utils/labels'

export default {
  name: 'Supervision',
  setup() {
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    const overview = ref(null)

    const fetchOverview = async () => {
      loading.value = true
      const result = await store.dispatch('fetchSupervisionOverview')
      loading.value = false

      if (result.success) {
        overview.value = result.data
      } else {
        ElMessage.error(result.message || '获取质量监管概览失败')
      }
    }

    const summaryCards = computed(() => {
      const summary = overview.value?.summary || {}
      return [
        { label: '检测记录', value: summary.totalInspectionRecords || 0, icon: 'DocumentChecked', color: '#409EFF' },
        { label: '合格批次', value: summary.passedInspectionRecords || 0, icon: 'CircleCheckFilled', color: '#67C23A' },
        { label: '风险预警', value: summary.warningProducts || 0, icon: 'WarningFilled', color: '#E6A23C' },
        { label: '已验签记录', value: summary.signatureVerifiedRecords || 0, icon: 'Lock', color: '#8E44AD' }
      ]
    })

    const recentInspections = computed(() => overview.value?.recentInspections || [])
    const warningProducts = computed(() => overview.value?.warningProducts || [])
    const governanceFocus = computed(() => overview.value?.governanceFocus || [])
    const chainValid = computed(() => overview.value?.summary?.chainValid !== false)

    const inspectionTagType = (value) => {
      if (!value) {
        return 'info'
      }
      if (value.includes('不合格')) {
        return 'danger'
      }
      if (value.includes('复检')) {
        return 'warning'
      }
      return 'success'
    }

    const riskTagType = (value) => {
      if (value === 'HIGH') {
        return 'danger'
      }
      if (value === 'MEDIUM') {
        return 'warning'
      }
      return 'success'
    }

    const riskLabel = (value) => {
      if (value === 'HIGH') {
        return '高风险'
      }
      if (value === 'MEDIUM') {
        return '信息待补全'
      }
      return '正常'
    }

    const needsSupplement = (item) => {
      return item?.riskLevel === 'MEDIUM' || (item?.issues || []).includes('缺少检测记录')
    }

    const goSupplement = (productId) => {
      if (!productId) {
        return
      }
      router.push({
        path: '/trace/add',
        query: {
          productId,
          operationType: 'INSPECT'
        }
      })
    }

    const shortHash = (value) => {
      if (!value) {
        return '-'
      }
      return `${value.slice(0, 12)}...${value.slice(-8)}`
    }

    const formatTime = (value) => {
      if (!value) {
        return '-'
      }
      return new Date(value).toLocaleString('zh-CN')
    }

    onMounted(fetchOverview)

    return {
      chainValid,
      formatTime,
      governanceFocus,
      inspectionTagType,
      loading,
      goSupplement,
      needsSupplement,
      recentInspections,
      riskLabel,
      riskTagType,
      shortHash,
      statusLabel,
      summaryCards,
      warningProducts
    }
  }
}
</script>

<style scoped>
.supervision-page {
  padding: 20px;
}

.summary-row,
.panel-row {
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

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.summary-label {
  margin-top: 6px;
  color: #909399;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.warning-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: #fcfcfd;
}

.warning-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.warning-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.warning-subtitle,
.warning-meta {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.warning-meta {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.issue-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.warning-suggestion {
  margin: 12px 0 0;
  line-height: 1.7;
  color: #606266;
}

.warning-actions {
  margin-top: 10px;
  display: flex;
  gap: 12px;
}

.governance-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.governance-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.governance-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  flex-shrink: 0;
}

.governance-text {
  line-height: 1.7;
  color: #606266;
}

.chain-alert {
  margin-top: 18px;
}

.hash-text {
  font-size: 12px;
}

@media (max-width: 1200px) {
  .summary-row :deep(.el-col),
  .panel-row :deep(.el-col) {
    width: 100%;
    max-width: 100%;
    flex: 0 0 100%;
  }
}
</style>
