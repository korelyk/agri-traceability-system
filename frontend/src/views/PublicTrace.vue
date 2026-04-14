<template>
  <div class="public-page">
    <section class="public-hero">
      <div class="public-hero-inner">
        <div class="hero-copy">
          <div class="hero-eyebrow">
            <el-icon><Connection /></el-icon>
            公开溯源查询
          </div>
          <h1 class="hero-title">面向消费者与监管方的可信查询入口</h1>
          <p class="hero-subtitle">
            输入产品编号或扫描二维码后，系统会同时展示产品履历、检测摘要和链上校验结果，帮助说明“记录能查到”和“记录值得信任”是两件不同的事。
          </p>
        </div>

        <div class="hero-panel search-panel">
          <div class="search-label">输入产品编号</div>
          <el-input v-model="searchQuery" placeholder="例如：FR202603172130115413" size="large" class="search-input">
            <template #append>
              <el-button type="primary" :loading="loading" @click="handleSearch">
                <el-icon><Search /></el-icon>
                立即查询
              </el-button>
            </template>
          </el-input>
          <div class="search-tip">支持扫码跳转，也支持直接输入产品 ID 进行公开查询。</div>
        </div>
      </div>
    </section>

    <section v-if="traceResult" class="result-wrap">
      <article class="section-card product-overview">
        <div class="section-head">
          <div>
            <h3 class="section-title">{{ traceResult.productName }}</h3>
            <p class="section-desc">{{ traceResult.origin }} · {{ traceResult.producer }}</p>
          </div>
          <el-tag :type="traceResult.verified ? 'success' : 'warning'" size="large">
            {{ traceResult.verified ? '校验通过' : '建议人工复核' }}
          </el-tag>
        </div>

        <div class="overview-grid">
          <div class="overview-card">
            <span class="overview-label">产品类别</span>
            <strong>{{ traceResult.category }}</strong>
          </div>
          <div class="overview-card">
            <span class="overview-label">链状态</span>
            <strong :class="traceResult.blockchainValid ? 'text-success' : 'text-danger'">
              {{ traceResult.blockchainValid ? '链校验通过' : '链状态异常' }}
            </strong>
          </div>
          <div class="overview-card">
            <span class="overview-label">最新检测</span>
            <strong :class="inspectionClass(traceResult.latestInspection?.qualityCheckResult)">
              {{ traceResult.latestInspection?.qualityCheckResult || '暂无检测结果' }}
            </strong>
          </div>
          <div class="overview-card">
            <span class="overview-label">风险等级</span>
            <strong :class="riskClass(traceResult.riskLevel)">
              {{ riskLabel(traceResult.riskLevel) }}
            </strong>
          </div>
        </div>

        <el-alert
          class="trust-alert"
          :type="traceResult.verified ? 'success' : 'warning'"
          :closable="false"
          :title="traceResult.suggestion || '当前产品记录已进入公开查询视图。'"
        />
      </article>

      <section class="result-grid">
        <article class="section-card timeline-panel">
          <div class="section-head">
            <div>
              <h3 class="section-title">流转时间线</h3>
              <p class="section-desc">从产品建档到后续流转与检测，按时间顺序展示完整履历。</p>
            </div>
          </div>

          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in traceResult.history"
              :key="`${item.stage}-${index}`"
              :timestamp="item.time"
              :type="index === traceResult.history.length - 1 ? 'primary' : ''"
              placement="top"
            >
              <div class="timeline-card">
                <div class="timeline-stage">{{ item.stage }}</div>
                <div class="timeline-detail">{{ item.detail }}</div>
                <div class="timeline-meta">
                  <span>操作人：{{ item.operator || '-' }}</span>
                  <span>地点：{{ item.location || '-' }}</span>
                </div>
                <div v-if="item.qualityCheckResult || item.certificateNo" class="timeline-extra">
                  <span v-if="item.qualityCheckResult">检测结果：{{ item.qualityCheckResult }}</span>
                  <span v-if="item.certificateNo">证书编号：{{ item.certificateNo }}</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </article>

        <div class="side-panels">
          <article class="section-card">
            <div class="section-head">
              <div>
                <h3 class="section-title">检测与证据</h3>
                <p class="section-desc">把检测结果和链上字段放在一起看，更容易解释系统可信性的来源。</p>
              </div>
            </div>

            <el-descriptions :column="1" border>
              <el-descriptions-item label="证书编号">
                {{ traceResult.latestInspection?.certificateNo || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="文档哈希">
                <code>{{ traceResult.latestInspection?.documentHash || '-' }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="签名通过记录数">
                {{ traceResult.verifiedRecords }}
              </el-descriptions-item>
            </el-descriptions>
          </article>

          <article class="section-card">
            <div class="section-head">
              <div>
                <h3 class="section-title">链上验证信息</h3>
                <p class="section-desc">便于说明查询页并不是普通详情页，而是带可信校验的公开视图。</p>
              </div>
            </div>

            <div class="verification-list">
              <div class="verification-item">
                <span>区块哈希</span>
                <code>{{ traceResult.blockHash || '-' }}</code>
              </div>
              <div class="verification-item">
                <span>交易 ID</span>
                <code>{{ traceResult.transactionId || '-' }}</code>
              </div>
            </div>
          </article>
        </div>
      </section>
    </section>
  </div>
</template>

<script>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Connection, Search } from '@element-plus/icons-vue'
import { operationTypeDisplay, productCategoryLabel } from '../utils/labels'

const operationOrder = {
  PRODUCE: 1,
  PROCESS: 2,
  PACKAGE: 3,
  STORAGE: 4,
  TRANSPORT: 5,
  SALE: 6,
  INSPECT: 7
}

export default {
  name: 'PublicTrace',
  components: {
    Connection,
    Search
  },
  setup() {
    const route = useRoute()
    const store = useStore()
    const searchQuery = ref('')
    const traceResult = ref(null)
    const loading = ref(false)

    const mapResult = (data) => {
      const history = [...(data.traceHistory || [])]
        .sort((a, b) => {
          const timeA = a.operationTime ? new Date(a.operationTime).getTime() : 0
          const timeB = b.operationTime ? new Date(b.operationTime).getTime() : 0
          if (timeA !== timeB) {
            return timeA - timeB
          }
          return (operationOrder[a.operationType] || 99) - (operationOrder[b.operationType] || 99)
        })
        .map((record) => ({
          stage: operationTypeDisplay(record.operationTypeName, record.operationType),
          time: record.operationTime ? new Date(record.operationTime).toLocaleString('zh-CN') : '',
          detail: record.operationDetail,
          operator: record.operatorName,
          location: record.location,
          qualityCheckResult: record.qualityCheckResult,
          certificateNo: record.certificateNo
        }))

      return {
        verified: data.blockchainValid && data.dataConsistent,
        blockchainValid: data.blockchainValid,
        productName: data.product?.productName,
        category: productCategoryLabel(data.product?.productCategory),
        producer: data.product?.producerName,
        origin: data.product?.origin,
        blockHash: data.product?.blockHash,
        transactionId: data.product?.transactionId,
        latestInspection: data.trustSummary?.latestInspection,
        riskLevel: data.trustSummary?.riskLevel,
        suggestion: data.trustSummary?.suggestion,
        verifiedRecords: data.trustSummary?.verifiedRecords || 0,
        history
      }
    }

    const handleSearch = async () => {
      if (!searchQuery.value.trim()) {
        ElMessage.warning('请输入产品 ID')
        return
      }

      loading.value = true
      const result = await store.dispatch('traceProduct', searchQuery.value.trim())
      loading.value = false

      if (result.success) {
        traceResult.value = mapResult(result.data)
      } else {
        traceResult.value = null
        ElMessage.error(result.message || '查询失败')
      }
    }

    const syncProductIdFromRoute = () => {
      const productId = route.params.productId || route.query.productId
      if (productId) {
        searchQuery.value = productId
        handleSearch()
      }
    }

    const inspectionClass = (value) => {
      if (!value) {
        return 'text-warning'
      }
      if (value.includes('不合格')) {
        return 'text-danger'
      }
      if (value.includes('复检')) {
        return 'text-warning'
      }
      return 'text-success'
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
        return 'text-danger'
      }
      if (value === 'MEDIUM') {
        return 'text-warning'
      }
      return 'text-success'
    }

    onMounted(syncProductIdFromRoute)
    watch(() => [route.params.productId, route.query.productId], syncProductIdFromRoute)

    return {
      handleSearch,
      inspectionClass,
      loading,
      riskClass,
      riskLabel,
      searchQuery,
      traceResult
    }
  }
}
</script>

<style scoped>
.public-page {
  min-height: 100vh;
  padding: 32px 20px 42px;
  background:
    radial-gradient(circle at top left, rgba(93, 187, 131, 0.16), transparent 26%),
    radial-gradient(circle at right top, rgba(196, 138, 67, 0.16), transparent 24%),
    linear-gradient(180deg, #f4f8f2 0%, #eef3ef 100%);
}

.public-hero {
  max-width: 1200px;
  margin: 0 auto;
}

.public-hero-inner {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(320px, 420px);
  gap: 24px;
  align-items: center;
}

.hero-copy {
  padding: 16px 8px;
}

.search-panel {
  padding: 24px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(47, 125, 88, 0.1);
  box-shadow: 0 20px 38px rgba(28, 47, 39, 0.08);
  backdrop-filter: blur(16px);
}

.search-label {
  margin-bottom: 12px;
  color: #5e7065;
  font-size: 14px;
  font-weight: 600;
}

.search-tip {
  margin-top: 12px;
  color: #7a887f;
  font-size: 13px;
  line-height: 1.7;
}

.result-wrap {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 1200px;
  margin: 30px auto 0;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.overview-card {
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(245, 248, 244, 0.95), rgba(255, 255, 255, 0.95));
  border: 1px solid rgba(47, 125, 88, 0.1);
}

.overview-label {
  display: block;
  color: #6d7c72;
  font-size: 13px;
}

.overview-card strong {
  display: block;
  margin-top: 10px;
  font-size: 22px;
  line-height: 1.4;
}

.trust-alert {
  margin-top: 8px;
}

.result-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  gap: 24px;
}

.timeline-panel {
  min-width: 0;
}

.timeline-card {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(47, 125, 88, 0.08);
}

.timeline-stage {
  font-size: 18px;
  font-weight: 700;
}

.timeline-detail {
  margin-top: 10px;
  color: #66756c;
  line-height: 1.8;
}

.timeline-meta,
.timeline-extra {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
  color: #7a867e;
  font-size: 13px;
}

.side-panels {
  display: grid;
  gap: 24px;
}

.verification-list {
  display: grid;
  gap: 14px;
}

.verification-item {
  padding: 16px 18px;
  border-radius: 16px;
  background: rgba(245, 248, 244, 0.92);
  border: 1px solid rgba(47, 125, 88, 0.08);
}

.verification-item span {
  display: block;
  margin-bottom: 8px;
  color: #6d7c72;
  font-size: 13px;
}

.verification-item code {
  word-break: break-all;
}

@media (max-width: 1100px) {
  .public-hero-inner,
  .result-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 540px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
