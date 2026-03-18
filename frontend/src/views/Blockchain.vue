<template>
  <div class="blockchain-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>区块链浏览器</span>
          <el-tag :type="blockchainInfo?.chainValid ? 'success' : 'danger'">
            {{ blockchainInfo?.chainValid ? '链状态正常' : '链状态异常' }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="3" border class="chain-info">
        <el-descriptions-item label="区块总数">
          {{ blockchainInfo?.totalBlocks || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="交易总数">
          {{ blockchainInfo?.totalTransactions || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="待打包交易">
          {{ blockchainInfo?.pendingTransactions || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="挖矿难度">
          {{ blockchainInfo?.difficulty || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="已追踪产品">
          {{ blockchainInfo?.productsTraced || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="数据校验">
          {{ blockchainInfo?.chainValid ? '通过' : '失败' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="blocks-card">
      <template #header>
        <div class="card-header">
          <span>区块列表</span>
          <el-button text type="primary" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <el-empty v-if="!loading && blocks.length === 0" description="暂无区块数据" />

      <el-collapse v-else v-model="activeBlocks">
        <el-collapse-item
          v-for="block in blocks"
          :key="block.index"
          :name="block.index"
          :title="`区块 #${block.index} · ${shortHash(block.blockHash)}`"
        >
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="区块哈希">
              <code>{{ block.blockHash }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="前一区块哈希">
              <code>{{ block.previousHash }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="Merkle 根">
              <code>{{ block.merkleRoot }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="时间">
              {{ formatTime(block.timestamp) }}
            </el-descriptions-item>
            <el-descriptions-item label="Nonce">
              {{ block.nonce }}
            </el-descriptions-item>
            <el-descriptions-item label="难度">
              {{ block.difficulty }}
            </el-descriptions-item>
            <el-descriptions-item label="关联产品">
              {{ block.productId || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="操作类型">
              {{ block.operationType || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="交易数量">
              {{ block.transactions?.length || 0 }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="block.transactions?.length" class="transactions">
            <h4>交易明细</h4>
            <el-table :data="block.transactions" size="small">
              <el-table-column prop="transactionId" label="交易ID" min-width="180">
                <template #default="{ row }">
                  <code>{{ shortHash(row.transactionId) }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="productId" label="产品ID" min-width="160" />
              <el-table-column prop="operationType" label="操作类型" width="120" />
              <el-table-column prop="operatorName" label="操作人" width="140" />
              <el-table-column prop="location" label="地点" min-width="140" />
            </el-table>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export default {
  name: 'Blockchain',
  setup() {
    const store = useStore()
    const loading = ref(false)
    const blocks = ref([])
    const activeBlocks = ref([])

    const blockchainInfo = computed(() => store.state.blockchainInfo)

    const fetchData = async () => {
      loading.value = true
      const [infoResult, blocksResult] = await Promise.all([
        store.dispatch('fetchBlockchainInfo'),
        store.dispatch('fetchBlocks')
      ])
      loading.value = false

      if (infoResult.success === false) {
        ElMessage.error(infoResult.message || '获取区块链统计失败')
      }

      if (blocksResult.success) {
        blocks.value = blocksResult.data || []
        activeBlocks.value = blocks.value.length ? [blocks.value[0].index] : []
      } else {
        blocks.value = []
        ElMessage.error(blocksResult.message || '获取区块列表失败')
      }
    }

    const formatTime = (timestamp) => {
      if (!timestamp) {
        return '-'
      }
      return new Date(timestamp * 1000).toLocaleString('zh-CN')
    }

    const shortHash = (value) => {
      if (!value) {
        return '-'
      }
      return value.length > 24 ? `${value.slice(0, 12)}...${value.slice(-8)}` : value
    }

    onMounted(fetchData)

    return {
      loading,
      blocks,
      activeBlocks,
      blockchainInfo,
      fetchData,
      formatTime,
      shortHash
    }
  }
}
</script>

<style scoped>
.blockchain-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chain-info {
  margin-bottom: 20px;
}

.blocks-card {
  margin-top: 20px;
}

.transactions {
  margin-top: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.transactions h4 {
  margin: 0 0 12px;
}

code {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  word-break: break-all;
}
</style>
