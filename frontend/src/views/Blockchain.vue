<template>
  <div class="blockchain-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>区块链浏览器</span>
          <el-tag :type="blockchainInfo?.chainValid ? 'success' : 'danger'">
            {{ blockchainInfo?.chainValid ? '链有效' : '链无效' }}
          </el-tag>
        </div>
      </template>
      
      <el-descriptions :column="3" border class="chain-info">
        <el-descriptions-item label="总区块数">
          {{ blockchainInfo?.totalBlocks || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="总交易数">
          {{ blockchainInfo?.totalTransactions || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="待处理交易">
          {{ blockchainInfo?.pendingTransactions || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="挖矿难度">
          {{ blockchainInfo?.difficulty || 4 }}
        </el-descriptions-item>
        <el-descriptions-item label="溯源产品数">
          {{ blockchainInfo?.productsTraced || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="链状态">
          {{ blockchainInfo?.chainValid ? '有效' : '无效' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- 区块列表 -->
    <el-card class="blocks-card">
      <template #header>
        <span>区块列表</span>
      </template>
      
      <el-collapse v-model="activeBlocks">
        <el-collapse-item
          v-for="block in blocks"
          :key="block.index"
          :title="`区块 #${block.index} - ${block.blockHash?.substring(0, 20)}...`"
          :name="block.index"
        >
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="区块哈希">
              <code>{{ block.blockHash }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="前一区块哈希">
              <code>{{ block.previousHash }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="Merkle根">
              <code>{{ block.merkleRoot }}</code>
            </el-descriptions-item>
            <el-descriptions-item label="时间戳">
              {{ formatTime(block.timestamp) }}
            </el-descriptions-item>
            <el-descriptions-item label="随机数">
              {{ block.nonce }}
            </el-descriptions-item>
            <el-descriptions-item label="难度">
              {{ block.difficulty }}
            </el-descriptions-item>
            <el-descriptions-item label="交易数量">
              {{ block.transactions?.length || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="产品ID">
              {{ block.productId }}
            </el-descriptions-item>
            <el-descriptions-item label="操作类型">
              {{ block.operationType }}
            </el-descriptions-item>
          </el-descriptions>
          
          <div v-if="block.transactions && block.transactions.length > 0" class="transactions">
            <h4>交易列表</h4>
            <el-table :data="block.transactions" size="small">
              <el-table-column prop="transactionId" label="交易ID" width="180">
                <template #default="scope">
                  <code>{{ scope.row.transactionId?.substring(0, 16) }}...</code>
                </template>
              </el-table-column>
              <el-table-column prop="productId" label="产品ID" />
              <el-table-column prop="operationType" label="操作类型" />
              <el-table-column prop="operatorName" label="操作者" />
            </el-table>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'

export default {
  name: 'Blockchain',
  setup() {
    const store = useStore()
    const activeBlocks = ref([0])
    
    const blockchainInfo = computed(() => store.state.blockchainInfo)
    
    // 模拟区块数据
    const blocks = ref([
      {
        index: 0,
        blockHash: '0000a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef12',
        previousHash: '0',
        merkleRoot: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
        timestamp: Date.now() / 1000 - 86400 * 7,
        nonce: 12345,
        difficulty: 4,
        transactions: [],
        productId: 'GENESIS',
        operationType: '创世区块'
      },
      {
        index: 1,
        blockHash: '0000b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef1234',
        previousHash: '0000a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef12',
        merkleRoot: 'a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456',
        timestamp: Date.now() / 1000 - 86400 * 5,
        nonce: 23456,
        difficulty: 4,
        transactions: [
          {
            transactionId: 'tx001',
            productId: 'AP202401010001',
            operationType: 'PRODUCE',
            operatorName: '张三'
          }
        ],
        productId: 'AP202401010001',
        operationType: '生产'
      },
      {
        index: 2,
        blockHash: '0000c3d4e5f6789012345678901234567890abcdef1234567890abcdef12345',
        previousHash: '0000b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef1234',
        merkleRoot: 'b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef12345678',
        timestamp: Date.now() / 1000 - 86400 * 3,
        nonce: 34567,
        difficulty: 4,
        transactions: [
          {
            transactionId: 'tx002',
            productId: 'GR202401010002',
            operationType: 'PRODUCE',
            operatorName: '李四'
          }
        ],
        productId: 'GR202401010002',
        operationType: '生产'
      }
    ])
    
    const formatTime = (timestamp) => {
      if (!timestamp) return ''
      return new Date(timestamp * 1000).toLocaleString('zh-CN')
    }
    
    onMounted(() => {
      store.dispatch('fetchBlockchainInfo')
    })
    
    return {
      blockchainInfo,
      blocks,
      activeBlocks,
      formatTime
    }
  }
}
</script>

<style scoped>
.blockchain-page {
  padding: 20px;
}

.chain-info {
  margin-bottom: 20px;
}

.blocks-card {
  margin-top: 20px;
}

.transactions {
  margin-top: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

code {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  word-break: break-all;
}
</style>
