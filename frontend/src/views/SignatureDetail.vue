<template>
  <div class="signature-page" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>签名验签详情</span>
          <div class="header-actions">
            <el-button @click="$router.back()">返回</el-button>
            <el-button type="primary" plain @click="fetchDetail">刷新校验</el-button>
          </div>
        </div>
      </template>

      <div v-if="detail" class="detail-body">
        <el-row :gutter="16" class="status-grid">
          <el-col :md="8" :sm="12" :xs="24">
            <div class="status-card">
              <div class="status-label">数字签名</div>
              <div class="status-value" :class="statusClass(detail.verification?.signatureValid)">
                {{ detail.verification?.signatureValid ? '验证通过' : '验证失败' }}
              </div>
              <div class="status-note">证明这条交易确实由对应主体签发</div>
            </div>
          </el-col>
          <el-col :md="8" :sm="12" :xs="24">
            <div class="status-card">
              <div class="status-label">交易哈希</div>
              <div class="status-value" :class="statusClass(detail.verification?.transactionHashValid)">
                {{ detail.verification?.transactionHashValid ? '一致' : '不一致' }}
              </div>
              <div class="status-note">说明交易内容没有在链外被改写</div>
            </div>
          </el-col>
          <el-col :md="8" :sm="12" :xs="24">
            <div class="status-card">
              <div class="status-label">数据库对照</div>
              <div class="status-value" :class="statusClass(detail.verification?.dbConsistent)">
                {{ detail.verification?.dbConsistent ? '一致' : '异常' }}
              </div>
              <div class="status-note">校验数据库记录与链上交易是否一致</div>
            </div>
          </el-col>
          <el-col :md="12" :sm="12" :xs="24">
            <div class="status-card">
              <div class="status-label">所属区块</div>
              <div class="status-value" :class="statusClass(detail.verification?.blockValid)">
                {{ detail.verification?.blockValid ? '有效区块' : '区块异常' }}
              </div>
              <div class="status-note">验证该交易所在区块自身哈希是否有效</div>
            </div>
          </el-col>
          <el-col :md="12" :sm="12" :xs="24">
            <div class="status-card">
              <div class="status-label">整条区块链</div>
              <div class="status-value" :class="statusClass(detail.verification?.chainValid)">
                {{ detail.verification?.chainValid ? '链校验通过' : '链状态异常' }}
              </div>
              <div class="status-note">验证前后区块哈希关系与 Merkle 根是否正确</div>
            </div>
          </el-col>
        </el-row>

        <el-alert
          class="overview-alert"
          :type="detail.verification?.overallPassed ? 'success' : 'warning'"
          :title="detail.verification?.overallPassed ? '该交易已通过验签与链上校验' : '该交易存在待核查项'"
          :description="detail.verification?.overallPassed
            ? '可用于说明系统如何通过数字签名和区块链保证关键溯源记录可信。'
            : '建议重点检查签名、交易哈希、数据库一致性和区块状态。'"
          show-icon
          :closable="false"
        />

        <el-row :gutter="18">
          <el-col :lg="12" :xs="24">
            <el-card class="section-card">
              <template #header>
                <span>交易信息</span>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="交易 ID">{{ detail.transaction?.transactionId }}</el-descriptions-item>
                <el-descriptions-item label="产品 ID">{{ detail.transaction?.productId || '-' }}</el-descriptions-item>
                <el-descriptions-item label="产品名称">{{ readable(detail.product?.productName || detail.transaction?.productName) }}</el-descriptions-item>
                <el-descriptions-item label="操作类型">{{ operationTypeLabel(detail.transaction?.operationType) }}</el-descriptions-item>
                <el-descriptions-item label="操作人">{{ readable(detail.transaction?.operatorName) }}</el-descriptions-item>
                <el-descriptions-item label="地点">{{ readable(detail.transaction?.location) }}</el-descriptions-item>
                <el-descriptions-item label="上链时间">{{ formatUnix(detail.transaction?.timestamp) }}</el-descriptions-item>
                <el-descriptions-item label="交易哈希">
                  <code>{{ detail.transaction?.transactionHash }}</code>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>

          <el-col :lg="12" :xs="24">
            <el-card class="section-card">
              <template #header>
                <span>区块信息</span>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="区块序号">#{{ detail.block?.index ?? '-' }}</el-descriptions-item>
                <el-descriptions-item label="区块哈希">
                  <code>{{ detail.block?.blockHash || '-' }}</code>
                </el-descriptions-item>
                <el-descriptions-item label="前一区块哈希">
                  <code>{{ detail.block?.previousHash || '-' }}</code>
                </el-descriptions-item>
                <el-descriptions-item label="Merkle 根">
                  <code>{{ detail.block?.merkleRoot || '-' }}</code>
                </el-descriptions-item>
                <el-descriptions-item label="时间戳">{{ formatUnix(detail.block?.timestamp) }}</el-descriptions-item>
                <el-descriptions-item label="难度 / Nonce">
                  {{ detail.block?.difficulty ?? '-' }} / {{ detail.block?.nonce ?? '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="18">
          <el-col :lg="12" :xs="24">
            <el-card class="section-card">
              <template #header>
                <span>签名信息</span>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="签名算法">{{ detail.signature?.algorithm }}</el-descriptions-item>
                <el-descriptions-item label="公钥指纹">{{ detail.signature?.publicKeyFingerprint || '-' }}</el-descriptions-item>
                <el-descriptions-item label="区块链地址">{{ detail.signature?.blockchainAddress || '-' }}</el-descriptions-item>
                <el-descriptions-item label="数字签名">
                  <code>{{ detail.signature?.digitalSignature || '-' }}</code>
                </el-descriptions-item>
                <el-descriptions-item label="公钥">
                  <code>{{ detail.signature?.publicKey || '-' }}</code>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>

          <el-col :lg="12" :xs="24">
            <el-card class="section-card">
              <template #header>
                <span>签名载荷</span>
              </template>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="transactionId">{{ detail.payloadFields?.transactionId }}</el-descriptions-item>
                <el-descriptions-item label="timestamp">{{ detail.payloadFields?.timestamp }}</el-descriptions-item>
                <el-descriptions-item label="productId">{{ detail.payloadFields?.productId }}</el-descriptions-item>
                <el-descriptions-item label="productName">{{ readable(detail.payloadFields?.productName) }}</el-descriptions-item>
                <el-descriptions-item label="operationType">{{ detail.payloadFields?.operationType }}</el-descriptions-item>
                <el-descriptions-item label="operatorId">{{ detail.payloadFields?.operatorId }}</el-descriptions-item>
                <el-descriptions-item label="location">{{ readable(detail.payloadFields?.location) }}</el-descriptions-item>
                <el-descriptions-item label="operationDetail">{{ readable(detail.payloadFields?.operationDetail) }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="section-card">
          <template #header>
            <span>实际参与签名的原始字符串</span>
          </template>
          <pre class="payload-code">{{ detail.payloadText }}</pre>
        </el-card>

        <el-card class="section-card">
          <template #header>
            <span>验签说明</span>
          </template>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="explain-index">1</div>
              <p>系统先把交易中的关键字段按固定顺序拼接成签名载荷。</p>
            </div>
            <div class="explain-item">
              <div class="explain-index">2</div>
              <p>操作人使用自己的私钥对这段载荷进行签名，生成数字签名值。</p>
            </div>
            <div class="explain-item">
              <div class="explain-index">3</div>
              <p>区块链在接收交易前，会用公钥对同一段载荷重新验签。</p>
            </div>
            <div class="explain-item">
              <div class="explain-index">4</div>
              <p>只要签名、交易哈希、区块哈希链任一环节被破坏，这一页就会显示异常。</p>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { operationTypeLabel, readableText } from '../utils/labels'

export default {
  name: 'SignatureDetail',
  setup() {
    const route = useRoute()
    const store = useStore()
    const loading = ref(false)
    const detail = ref(null)

    const readable = (value) => readableText(value) || '-'

    const formatUnix = (value) => {
      if (!value) {
        return '-'
      }
      return new Date(value * 1000).toLocaleString('zh-CN')
    }

    const statusClass = (valid) => (valid ? 'is-valid' : 'is-invalid')

    const fetchDetail = async () => {
      loading.value = true
      const result = await store.dispatch('fetchTransactionVerificationDetail', route.params.transactionId)
      loading.value = false

      if (result.success) {
        detail.value = result.data
      } else {
        detail.value = null
        ElMessage.error(result.message || '获取交易验签详情失败')
      }
    }

    onMounted(fetchDetail)

    return {
      detail,
      fetchDetail,
      formatUnix,
      loading,
      operationTypeLabel,
      readable,
      statusClass
    }
  }
}
</script>

<style scoped>
.signature-page {
  padding: 20px;
}

.card-header,
.header-actions {
  display: flex;
  align-items: center;
}

.card-header {
  justify-content: space-between;
}

.header-actions {
  gap: 10px;
}

.detail-body {
  display: grid;
  gap: 18px;
}

.status-grid {
  margin-bottom: 4px;
}

.status-card {
  height: 100%;
  padding: 18px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.status-label {
  color: #6b7280;
  font-size: 13px;
}

.status-value {
  margin-top: 10px;
  font-size: 24px;
  font-weight: 700;
}

.status-note {
  margin-top: 8px;
  color: #6b7280;
  line-height: 1.7;
  font-size: 13px;
}

.is-valid {
  color: #15803d;
}

.is-invalid {
  color: #b91c1c;
}

.overview-alert {
  margin-top: 4px;
}

.section-card {
  margin-top: 2px;
}

code,
.payload-code {
  font-family: 'Courier New', monospace;
}

code {
  font-size: 12px;
  word-break: break-all;
}

.payload-code {
  margin: 0;
  padding: 16px;
  border-radius: 12px;
  background: #111827;
  color: #f9fafb;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.8;
}

.explain-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.explain-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
}

.explain-item p {
  margin: 0;
  line-height: 1.8;
  color: #374151;
}

.explain-index {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #1d4ed8;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
}

@media (max-width: 900px) {
  .explain-grid {
    grid-template-columns: 1fr;
  }

  .card-header {
    align-items: flex-start;
    gap: 12px;
    flex-direction: column;
  }
}
</style>
