<template>
  <div class="page-shell dashboard-page">
    <section class="page-hero hero-panel">
      <div class="hero-main">
        <div class="hero-eyebrow">
          <el-icon><DataAnalysis /></el-icon>
          可信流转总览
        </div>
        <h1 class="hero-title">围绕链上存证、数字签名和质量监管构建可信闭环。</h1>
        <p class="hero-subtitle">
          首页聚焦系统运行状态、关键监管指标和可信流转逻辑，便于从业务视角解释“为什么是区块链 + 数字签名”，而不是普通的记录管理系统。
        </p>
      </div>

      <div class="hero-side">
        <div class="hero-stat">
          <span class="hero-stat-label">链状态</span>
          <strong>{{ blockchainInfo?.chainValid ? '校验正常' : '待人工核查' }}</strong>
          <small>{{ blockchainInfo?.totalBlocks || 0 }} 个区块 · {{ blockchainInfo?.totalTransactions || 0 }} 条交易</small>
        </div>
        <div class="hero-stat warm">
          <span class="hero-stat-label">监管关注</span>
          <strong>{{ overview?.summary?.warningProducts || 0 }} 个产品待关注</strong>
          <small>{{ overview?.summary?.trustedProducts || 0 }} 个产品达到可信展示条件</small>
        </div>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="item in statCards" :key="item.label" class="metric-card dashboard-metric">
        <div class="metric-top">
          <div class="metric-icon" :style="{ background: item.background }">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
          </div>
          <el-tag :type="item.tagType" effect="light">{{ item.tag }}</el-tag>
        </div>
        <div class="metric-label">{{ item.label }}</div>
        <div class="metric-value">{{ item.value }}</div>
        <div class="metric-note">{{ item.note }}</div>
      </article>
    </section>

    <section class="detail-grid">
      <article class="section-card trusted-panel trusted-panel-wide">
        <div class="section-head">
          <div>
            <h3 class="section-title">可信运行状态</h3>
            <p class="section-desc">把链状态、签名记录和质量检测指标放到同一个视图里，便于解释平台当前是否处于可展示状态。</p>
          </div>
          <el-tag :type="blockchainInfo?.chainValid ? 'success' : 'danger'">
            {{ blockchainInfo?.chainValid ? '运行稳定' : '需要排查' }}
          </el-tag>
        </div>

        <div class="trusted-grid">
          <div class="trusted-item">
            <span class="trusted-label">链校验状态</span>
            <strong>{{ blockchainInfo?.chainValid ? '通过' : '异常' }}</strong>
          </div>
          <div class="trusted-item">
            <span class="trusted-label">签名通过记录</span>
            <strong>{{ statistics?.verifiedTraceRecords || 0 }}</strong>
          </div>
          <div class="trusted-item">
            <span class="trusted-label">检测记录数</span>
            <strong>{{ statistics?.inspectionRecords || 0 }}</strong>
          </div>
          <div class="trusted-item">
            <span class="trusted-label">
              挖矿难度
              <el-tooltip content="表示新区块生成时需要满足的哈希计算门槛，当前难度 4 代表区块哈希前 4 位需为 0。" placement="top">
                <el-icon class="tip-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </span>
            <strong>{{ blockchainInfo?.difficulty || 4 }}</strong>
          </div>
        </div>

        <div class="chip-list">
          <div class="info-chip">
            <span class="chip-dot success" />
            认证方式：JWT Bearer Token
          </div>
          <div class="info-chip">
            <span class="chip-dot primary" />
            签名算法：RSA / SHA256withRSA
          </div>
          <div class="info-chip">
            <span class="chip-dot accent" />
            链状态存储：持久化区块链文件
          </div>
        </div>
      </article>

      <article class="section-card">
        <div class="section-head">
          <div>
            <h3 class="section-title">治理摘要</h3>
            <p class="section-desc">从监管角度快速判断当前展示重点，避免首页只剩数字堆叠。</p>
          </div>
        </div>

        <div class="summary-list">
          <div class="summary-item">
            <span class="summary-title">数字签名</span>
            <p>关键流转记录由业务主体签名，便于确认“是谁提交了什么内容”。</p>
          </div>
          <div class="summary-item">
            <span class="summary-title">区块链存证</span>
            <p>交易进入区块后与前序区块哈希关联，用于说明历史数据不易被悄悄修改。</p>
          </div>
          <div class="summary-item">
            <span class="summary-title">检测监管</span>
            <p>检测结果、证书编号、文档哈希进入流转记录，可用于监管抽检和公开展示。</p>
          </div>
          <div class="summary-item emphasis">
            <span class="summary-title">当前建议</span>
            <p>{{ overviewSuggestion }}</p>
          </div>
        </div>
      </article>

      <article class="section-card process-panel">
        <div class="section-head">
          <div>
            <h3 class="section-title">可信流转说明</h3>
            <p class="section-desc">把“业务数据为什么可信”拆成老师和评审更容易听懂的四个步骤。</p>
          </div>
        </div>

        <div class="process-list">
          <div v-for="(item, index) in processSteps" :key="item.title" class="process-item">
            <div class="process-index">{{ index + 1 }}</div>
            <div>
              <div class="process-title">{{ item.title }}</div>
              <div class="process-text">{{ item.text }}</div>
            </div>
          </div>
        </div>
      </article>

      <article class="section-card">
        <div class="section-head">
          <div>
            <h3 class="section-title">质量监管速览</h3>
            <p class="section-desc">展示最近检测记录和待关注产品，便于从“监管价值”而不是“页面数量”解释系统。</p>
          </div>
        </div>

        <div v-if="recentInspections.length" class="inspection-list">
          <div v-for="item in recentInspections" :key="item.recordId || item.transactionId" class="inspection-item">
            <div>
              <div class="inspection-name">{{ item.productName || '检测记录' }}</div>
              <div class="inspection-meta">
                {{ item.qualityCheckResult || '待补充结果' }} · {{ item.certificateNo || '无证书编号' }}
              </div>
            </div>
            <el-tag :type="inspectionTagType(item.qualityCheckResult)">
              {{ item.qualityCheckResult || '待检测' }}
            </el-tag>
          </div>
        </div>
        <el-empty v-else description="暂无检测记录" />

        <div class="warning-box">
          <div class="warning-head">
            <span>待关注产品</span>
            <el-tag :type="warningProducts.length ? 'warning' : 'success'">
              {{ warningProducts.length ? `${warningProducts.length} 个待关注` : '当前无预警' }}
            </el-tag>
          </div>
          <div v-if="warningProducts.length" class="warning-list">
            <div v-for="item in warningProducts" :key="item.productId" class="warning-item">
              <span>{{ item.productName }}</span>
              <small>{{ riskLabel(item.riskLevel) }}</small>
            </div>
          </div>
          <div v-else class="empty-tip">当前产品链状态和检测结果都处于较稳定区间。</div>
        </div>
      </article>
    </section>

    <section class="section-card">
      <div class="section-head">
        <div>
          <h3 class="section-title">快捷操作</h3>
          <p class="section-desc">保持高频业务入口清晰可见，避免答辩时在菜单里反复寻找。</p>
        </div>
      </div>

      <div class="action-grid">
        <button class="action-card action-primary" @click="$router.push('/products/register')">
          <div class="action-title">产品建档</div>
          <div class="action-text">为批次产品建立基础档案并生成初始交易。</div>
        </button>
        <button class="action-card action-success" @click="$router.push('/trace/add')">
          <div class="action-title">新增溯源记录</div>
          <div class="action-text">补充生产、加工、检测等关键环节记录。</div>
        </button>
        <button class="action-card action-warm" @click="$router.push('/blockchain')">
          <div class="action-title">查看区块链</div>
          <div class="action-text">进入区块浏览器核对区块、交易和链状态。</div>
        </button>
        <button class="action-card action-muted" @click="$router.push('/supervision')">
          <div class="action-title">进入质量监管</div>
          <div class="action-text">查看检测摘要、风险提示和治理重点。</div>
        </button>
      </div>
    </section>
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { DataAnalysis, QuestionFilled } from '@element-plus/icons-vue'

export default {
  name: 'Dashboard',
  components: {
    DataAnalysis,
    QuestionFilled
  },
  setup() {
    const store = useStore()
    const overview = ref(null)

    const statistics = computed(() => store.state.statistics || {})
    const blockchainInfo = computed(() => store.state.blockchainInfo || {})

    const statCards = computed(() => [
      {
        label: '产品总数',
        value: statistics.value.totalProducts || 0,
        icon: 'Goods',
        tag: '业务基数',
        tagType: 'success',
        note: '当前平台纳入可信流转链路的产品批次数量。',
        background: 'linear-gradient(135deg, rgba(47, 125, 88, 0.18), rgba(47, 125, 88, 0.06))'
      },
      {
        label: '溯源记录',
        value: statistics.value.totalTraceRecords || 0,
        icon: 'DocumentChecked',
        tag: '链上关联',
        tagType: 'primary',
        note: '覆盖生产、加工、运输、检测等各环节操作。',
        background: 'linear-gradient(135deg, rgba(69, 126, 209, 0.18), rgba(69, 126, 209, 0.06))'
      },
      {
        label: '签名通过',
        value: statistics.value.verifiedTraceRecords || 0,
        icon: 'Checked',
        tag: '可信指标',
        tagType: 'success',
        note: '已完成验签并可用于展示可信性的记录数量。',
        background: 'linear-gradient(135deg, rgba(93, 187, 131, 0.2), rgba(93, 187, 131, 0.08))'
      },
      {
        label: '检测记录',
        value: statistics.value.inspectionRecords || 0,
        icon: 'DataAnalysis',
        tag: '监管视角',
        tagType: 'warning',
        note: '可在监管页和公开查询页联动展示的检测凭证信息。',
        background: 'linear-gradient(135deg, rgba(196, 138, 67, 0.2), rgba(196, 138, 67, 0.08))'
      }
    ])

    const processSteps = [
      {
        title: '业务主体录入流转信息',
        text: '生产、加工、运输和检测主体提交业务记录，形成完整的产品履历。'
      },
      {
        title: '关键记录使用私钥签名',
        text: '系统对关键交易执行数字签名，确保记录来源可验证、内容被修改后可被识别。'
      },
      {
        title: '交易进入区块形成存证',
        text: '验签通过后写入区块链，通过区块哈希和前后链接提升篡改成本。'
      },
      {
        title: '公开查询与监管复核联动',
        text: '消费者看到履历，监管端持续检查检测记录、验签结果和数据库一致性。'
      }
    ]

    const recentInspections = computed(() => overview.value?.recentInspections || [])
    const warningProducts = computed(() => overview.value?.warningProducts || [])

    const overviewSuggestion = computed(() => {
      if (!overview.value?.summary) {
        return '系统已具备监管视图，可结合质量监管页展示检测记录、风险提示与链上核验。'
      }
      if (overview.value.summary.warningProducts > 0) {
        return '当前存在待关注产品，适合结合质量监管页说明预警依据和后续复核流程。'
      }
      return '当前链状态、验签结果和检测记录较完整，适合完整展示“可追溯 + 可验签 + 可监管”的闭环能力。'
    })

    const fetchOverview = async () => {
      const result = await store.dispatch('fetchSupervisionOverview')
      if (result.success) {
        overview.value = result.data
      } else {
        ElMessage.error(result.message || '获取监管概览失败')
      }
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
        return '需关注'
      }
      return '正常'
    }

    onMounted(async () => {
      await Promise.all([
        store.dispatch('fetchStatistics'),
        store.dispatch('fetchBlockchainInfo'),
        fetchOverview()
      ])
    })

    return {
      blockchainInfo,
      inspectionTagType,
      overview,
      overviewSuggestion,
      processSteps,
      recentInspections,
      riskLabel,
      statCards,
      statistics,
      warningProducts
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  gap: 24px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 360px);
  gap: 22px;
}

.hero-side {
  display: grid;
  gap: 14px;
}

.hero-stat {
  padding: 20px 22px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(47, 125, 88, 0.12);
}

.hero-stat.warm {
  background: linear-gradient(180deg, rgba(255, 248, 236, 0.9), rgba(255, 255, 255, 0.84));
}

.hero-stat-label {
  color: #6b7b71;
  font-size: 13px;
}

.hero-stat strong {
  display: block;
  margin-top: 12px;
  font-size: 24px;
}

.hero-stat small {
  display: block;
  margin-top: 8px;
  color: #75857a;
  line-height: 1.7;
}

.dashboard-metric {
  min-height: 190px;
}

.metric-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.metric-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  color: #234334;
}

.trusted-panel-wide {
  grid-column: span 7;
}

.trusted-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.trusted-item {
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(237, 246, 242, 0.9), rgba(255, 255, 255, 0.94));
  border: 1px solid rgba(47, 125, 88, 0.12);
}

.trusted-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #627168;
  font-size: 13px;
}

.trusted-item strong {
  display: block;
  margin-top: 10px;
  font-size: 26px;
}

.tip-icon {
  color: #6f8176;
}

.chip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.chip-dot.success {
  background: #5dbb83;
}

.chip-dot.primary {
  background: #4a85d0;
}

.chip-dot.accent {
  background: #c48a43;
}

.summary-list {
  display: grid;
  gap: 14px;
}

.summary-item {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(245, 248, 244, 0.9);
  border: 1px solid rgba(47, 125, 88, 0.08);
}

.summary-item.emphasis {
  background: linear-gradient(180deg, rgba(255, 248, 236, 0.95), rgba(255, 255, 255, 0.95));
}

.summary-title {
  display: block;
  font-weight: 700;
  margin-bottom: 8px;
}

.summary-item p,
.process-text,
.inspection-meta,
.empty-tip {
  margin: 0;
  color: #66756c;
  line-height: 1.75;
}

.process-panel {
  grid-column: span 5;
}

.process-list {
  display: grid;
  gap: 18px;
}

.process-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 14px;
}

.process-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 15px;
  background: linear-gradient(135deg, rgba(47, 125, 88, 0.16), rgba(196, 138, 67, 0.18));
  color: #1f5c41;
  font-weight: 700;
}

.process-title,
.inspection-name,
.warning-head {
  font-weight: 700;
}

.inspection-list {
  display: grid;
  gap: 12px;
}

.inspection-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(47, 125, 88, 0.08);
}

.warning-box {
  margin-top: 20px;
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255, 248, 236, 0.95), rgba(255, 255, 255, 0.92));
  border: 1px solid rgba(196, 138, 67, 0.16);
}

.warning-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.warning-list {
  display: grid;
  gap: 10px;
  margin-top: 14px;
}

.warning-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8);
}

.warning-item small {
  color: #a07031;
  font-size: 12px;
  font-weight: 700;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.action-card {
  padding: 20px;
  text-align: left;
  border: 1px solid transparent;
  border-radius: 22px;
  cursor: pointer;
  color: #193026;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 28px rgba(28, 47, 39, 0.08);
}

.action-primary {
  background: linear-gradient(180deg, rgba(237, 246, 242, 0.95), rgba(255, 255, 255, 0.95));
}

.action-success {
  background: linear-gradient(180deg, rgba(236, 247, 238, 0.95), rgba(255, 255, 255, 0.95));
}

.action-warm {
  background: linear-gradient(180deg, rgba(255, 248, 236, 0.95), rgba(255, 255, 255, 0.95));
}

.action-muted {
  background: linear-gradient(180deg, rgba(241, 244, 246, 0.95), rgba(255, 255, 255, 0.95));
}

.action-title {
  font-size: 18px;
  font-weight: 700;
}

.action-text {
  margin-top: 8px;
  color: #65756b;
  line-height: 1.8;
}

@media (max-width: 1200px) {
  .trusted-panel-wide,
  .process-panel {
    grid-column: span 12;
  }
}

@media (max-width: 992px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .trusted-grid {
    grid-template-columns: 1fr;
  }
}
</style>
