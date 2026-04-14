<template>
  <div class="trace-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>溯源详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <template v-if="traceData">
        <el-descriptions title="产品信息" :column="2" border>
          <el-descriptions-item label="产品 ID">{{ traceData.product?.productId }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ traceData.product?.productName }}</el-descriptions-item>
          <el-descriptions-item label="产品类别">{{ productCategoryLabel(traceData.product?.productCategory) }}</el-descriptions-item>
          <el-descriptions-item label="批次号">{{ traceData.product?.batchNumber }}</el-descriptions-item>
          <el-descriptions-item label="生产者">{{ traceData.product?.producerName }}</el-descriptions-item>
          <el-descriptions-item label="产地">{{ traceData.product?.origin }}</el-descriptions-item>
          <el-descriptions-item label="生产日期">{{ formatTime(traceData.product?.productionDate) }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(traceData.product?.currentStatus)">
              {{ statusLabel(traceData.product?.currentStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-row :gutter="16" class="trust-row">
          <el-col :span="8">
            <div class="trust-card">
              <div class="trust-label">链上数据一致性</div>
              <div class="trust-value" :class="traceData.dataConsistent ? 'ok' : 'bad'">
                {{ traceData.dataConsistent ? '一致' : '异常' }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="trust-card">
              <div class="trust-label">区块链状态</div>
              <div class="trust-value" :class="traceData.blockchainValid ? 'ok' : 'bad'">
                {{ traceData.blockchainValid ? '校验通过' : '链路异常' }}
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="trust-card">
              <div class="trust-label">风险等级</div>
              <div class="trust-value" :class="riskClass(traceData.trustSummary?.riskLevel)">
                {{ riskLabel(traceData.trustSummary?.riskLevel) }}
              </div>
            </div>
          </el-col>
        </el-row>

        <el-alert
          :title="traceData.dataConsistent ? '当前产品记录可用于展示可信溯源能力' : '当前产品存在待核查项'"
          :type="traceData.dataConsistent && traceData.blockchainValid ? 'success' : 'warning'"
          :description="traceData.trustSummary?.suggestion"
          show-icon
          class="verify-alert"
        />

        <div v-if="needsSupplement(traceData.trustSummary)" class="supplement-bar">
          <div>
            <div class="supplement-title">当前产品仍有信息待补全</div>
            <div class="supplement-text">可通过“新增溯源记录”并选择“检测”操作，补录检测结果、证书编号和文档哈希。</div>
          </div>
          <el-button type="primary" @click="goSupplement(traceData.product?.productId)">
            去补录记录
          </el-button>
        </div>

        <el-card shadow="never" class="inspection-card">
          <template #header>
            <span>监管摘要</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="已验签记录">
              {{ traceData.trustSummary?.verifiedRecords ?? 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="检测记录数">
              {{ traceData.trustSummary?.inspectionRecords ?? 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="最新检测结果">
              <el-tag :type="inspectionTagType(traceData.trustSummary?.latestInspection?.qualityCheckResult)">
                {{ traceData.trustSummary?.latestInspection?.qualityCheckResult || '暂无检测' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="证书编号">
              {{ traceData.trustSummary?.latestInspection?.certificateNo || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="文档哈希" :span="2">
              <code>{{ traceData.trustSummary?.latestInspection?.documentHash || '-' }}</code>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <h3 class="section-title">溯源历史</h3>
        <el-timeline>
          <el-timeline-item
            v-for="record in traceData.traceHistory"
            :key="record.recordId || record.transactionId"
            :timestamp="formatTime(record.operationTime)"
            placement="top"
          >
            <el-card>
              <template #header>
                <div class="record-header">
                  <span class="operation-type">{{ operationTypeDisplay(record.operationTypeName, record.operationType) }}</span>
                  <div class="record-tags">
                    <el-tag v-if="record.verified" type="success" size="small">已验签</el-tag>
                    <el-tag v-if="record.qualityCheckResult" :type="inspectionTagType(record.qualityCheckResult)" size="small">
                      {{ record.qualityCheckResult }}
                    </el-tag>
                    <el-button v-if="record.transactionId" link type="primary" @click="viewSignature(record.transactionId)">
                      验签详情
                    </el-button>
                  </div>
                </div>
              </template>

              <div class="record-content">
                <p><strong>操作人：</strong>{{ record.operatorName }}（{{ userTypeDisplay(record.operatorTypeName, record.operatorType) }}）</p>
                <p><strong>地点：</strong>{{ record.location }}</p>
                <p><strong>详情：</strong>{{ record.operationDetail }}</p>
                <p v-if="record.certificateNo"><strong>证书编号：</strong>{{ record.certificateNo }}</p>
                <p v-if="record.documentHash"><strong>文档哈希：</strong><code>{{ record.documentHash }}</code></p>
                <p v-if="record.transactionId"><strong>交易 ID：</strong><code>{{ record.transactionId }}</code></p>

                <div v-if="record.temperature || record.humidity" class="environment-data">
                  <el-tag v-if="record.temperature" size="small">温度：{{ record.temperature }}°C</el-tag>
                  <el-tag v-if="record.humidity" size="small">湿度：{{ record.humidity }}%</el-tag>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { operationTypeDisplay, productCategoryLabel, statusLabel, userTypeDisplay } from '../utils/labels'

export default {
  name: 'TraceDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()
    const loading = ref(false)
    const traceData = ref(null)

    const fetchTraceData = async () => {
      loading.value = true
      const result = await store.dispatch('traceProduct', route.params.productId)
      loading.value = false

      if (result.success) {
        traceData.value = result.data
      } else {
        ElMessage.error(result.message)
      }
    }

    const formatTime = (value) => {
      if (!value) {
        return '-'
      }
      return new Date(value).toLocaleString('zh-CN')
    }

    const getStatusType = (status) => {
      const types = {
        CREATED: 'info',
        PRODUCE: 'success',
        PROCESS: 'warning',
        TRANSPORT: 'primary',
        STORAGE: 'info',
        SALE: 'danger'
      }
      return types[status] || 'info'
    }

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

    const riskLabel = (value) => {
      if (value === 'HIGH') {
        return '高风险'
      }
      if (value === 'MEDIUM') {
        return '信息待补全'
      }
      return '正常'
    }

    const riskClass = (value) => {
      if (value === 'HIGH') {
        return 'bad'
      }
      if (value === 'MEDIUM') {
        return 'warn'
      }
      return 'ok'
    }

    const viewSignature = (transactionId) => {
      if (transactionId) {
        router.push(`/verify/transaction/${transactionId}`)
      }
    }

    const needsSupplement = (summary) => {
      if (!summary) {
        return false
      }
      return summary.riskLevel === 'MEDIUM' || (summary.issues || []).includes('缺少检测记录')
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

    onMounted(fetchTraceData)

    return {
      formatTime,
      getStatusType,
      inspectionTagType,
      loading,
      operationTypeDisplay,
      productCategoryLabel,
      goSupplement,
      riskClass,
      riskLabel,
      needsSupplement,
      statusLabel,
      traceData,
      userTypeDisplay,
      viewSignature
    }
  }
}
</script>

<style scoped>
.trace-detail-page {
  padding: 20px;
}

.card-header,
.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.trust-row {
  margin: 20px 0;
}

.trust-card {
  padding: 18px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #ebeef5;
}

.trust-label {
  color: #909399;
  font-size: 13px;
}

.trust-value {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 700;
}

.trust-value.ok {
  color: #67c23a;
}

.trust-value.warn {
  color: #e6a23c;
}

.trust-value.bad {
  color: #f56c6c;
}

.verify-alert,
.inspection-card {
  margin-bottom: 20px;
}

.supplement-bar {
  margin-bottom: 20px;
  padding: 16px 18px;
  border: 1px solid #f0c78a;
  border-radius: 14px;
  background: #fff8ec;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.supplement-title {
  font-weight: 700;
  color: #8a5c1a;
}

.supplement-text {
  margin-top: 6px;
  color: #8a6c46;
  line-height: 1.7;
  font-size: 13px;
}

.section-title {
  margin: 30px 0 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.record-tags {
  display: flex;
  align-items: center;
  gap: 10px;
}

.operation-type {
  font-weight: 700;
  font-size: 16px;
}

.record-content p {
  margin: 8px 0;
}

.record-content code {
  word-break: break-all;
  font-size: 12px;
}

.environment-data {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .record-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .supplement-bar {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
