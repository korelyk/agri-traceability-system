<template>
  <div class="public-trace-page">
    <div class="header">
      <el-icon size="48" color="#67C23A"><Connection /></el-icon>
      <h1>农产品防伪溯源查询</h1>
      <p>基于区块链与数字签名技术</p>
    </div>

    <el-card class="search-card">
      <el-input v-model="searchQuery" placeholder="请输入产品ID" size="large" class="search-input">
        <template #append>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </template>
      </el-input>
    </el-card>

    <div v-if="traceResult" class="result-section">
      <el-card>
        <template #header>
          <div class="result-header">
            <span>溯源结果</span>
            <el-tag :type="traceResult.verified ? 'success' : 'warning'">
              {{ traceResult.verified ? '验证通过' : '数据异常' }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品名称">{{ traceResult.productName }}</el-descriptions-item>
          <el-descriptions-item label="产品类别">{{ traceResult.category }}</el-descriptions-item>
          <el-descriptions-item label="生产者">{{ traceResult.producer }}</el-descriptions-item>
          <el-descriptions-item label="产地">{{ traceResult.origin }}</el-descriptions-item>
        </el-descriptions>

        <h3 class="timeline-title">溯源历程</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in traceResult.history"
            :key="index"
            :timestamp="item.time"
            :type="index === 0 ? 'primary' : ''"
          >
            <el-card>
              <h4>{{ item.stage }}</h4>
              <p>{{ item.detail }}</p>
              <p>操作人: {{ item.operator }}</p>
              <p>地点: {{ item.location }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>

        <div class="verification-info">
          <el-alert title="链上验证信息" type="success" :closable="false">
            <p>区块哈希: {{ traceResult.blockHash }}</p>
            <p>交易ID: {{ traceResult.transactionId }}</p>
            <p>数字签名: {{ traceResult.signature || '无' }}</p>
          </el-alert>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export default {
  name: 'PublicTrace',
  setup() {
    const route = useRoute()
    const store = useStore()
    const searchQuery = ref('')
    const traceResult = ref(null)
    const loading = ref(false)

    const mapResult = (data) => ({
      verified: data.blockchainValid && data.dataConsistent,
      productName: data.product?.productName,
      category: data.product?.productCategory,
      producer: data.product?.producerName,
      origin: data.product?.origin,
      blockHash: data.product?.blockHash,
      transactionId: data.product?.transactionId,
      signature: data.traceHistory?.[0]?.digitalSignature,
      history: (data.traceHistory || []).map((record) => ({
        stage: record.operationTypeName || record.operationType,
        time: record.operationTime ? new Date(record.operationTime).toLocaleString('zh-CN') : '',
        detail: record.operationDetail,
        operator: record.operatorName,
        location: record.location
      }))
    })

    const handleSearch = async () => {
      if (!searchQuery.value.trim()) {
        ElMessage.warning('请输入产品ID')
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

    onMounted(() => {
      if (route.params.productId) {
        searchQuery.value = route.params.productId
        handleSearch()
      }
    })

    return {
      searchQuery,
      traceResult,
      loading,
      handleSearch
    }
  }
}
</script>

<style scoped>
.public-trace-page {
  min-height: 100vh;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  text-align: center;
  color: #fff;
  margin-bottom: 40px;
}

.header h1 {
  font-size: 32px;
  margin: 15px 0 5px;
}

.header p {
  font-size: 16px;
  opacity: 0.9;
}

.search-card {
  max-width: 600px;
  margin: 0 auto;
}

.search-input {
  width: 100%;
}

.result-section {
  max-width: 800px;
  margin: 30px auto;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timeline-title {
  margin: 30px 0 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
}

.verification-info {
  margin-top: 30px;
}
</style>
