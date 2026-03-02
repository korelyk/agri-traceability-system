<template>
  <div class="public-trace-page">
    <div class="header">
      <el-icon size="48" color="#67C23A"><Connection /></el-icon>
      <h1>农产品防伪溯源查询</h1>
      <p>基于区块链与数字签名技术</p>
    </div>
    
    <el-card class="search-card">
      <el-input
        v-model="searchQuery"
        placeholder="请输入产品ID"
        size="large"
        class="search-input"
      >
        <template #append>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
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
              {{ traceResult.verified ? '正品验证' : '未验证' }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品名称">
            {{ traceResult.productName }}
          </el-descriptions-item>
          <el-descriptions-item label="产品类别">
            {{ traceResult.category }}
          </el-descriptions-item>
          <el-descriptions-item label="生产者">
            {{ traceResult.producer }}
          </el-descriptions-item>
          <el-descriptions-item label="产地">
            {{ traceResult.origin }}
          </el-descriptions-item>
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
              <p>操作者: {{ item.operator }}</p>
              <p>地点: {{ item.location }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        
        <div class="verification-info">
          <el-alert
            title="区块链验证信息"
            type="success"
            :closable="false"
          >
            <p>区块哈希: {{ traceResult.blockHash }}</p>
            <p>交易ID: {{ traceResult.transactionId }}</p>
            <p>数字签名: {{ traceResult.signature }}</p>
          </el-alert>
        </div>
      </el-card>
    </div>
    
    <div class="footer">
      <p>© 2024 农产品防伪溯源系统 - 保障食品安全，追溯每一环节</p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'PublicTrace',
  setup() {
    const searchQuery = ref('')
    const traceResult = ref(null)
    
    const handleSearch = () => {
      // 模拟查询结果
      traceResult.value = {
        verified: true,
        productName: '山东红富士苹果',
        category: '水果',
        producer: '山东果园有限公司',
        origin: '山东烟台',
        blockHash: '0000a1b2c3d4e5f6...',
        transactionId: 'tx123456789',
        signature: '0xabcdef123456...',
        history: [
          {
            stage: '生产',
            time: '2024-01-01 08:00:00',
            detail: '苹果种植，使用有机肥料',
            operator: '张三',
            location: '山东烟台果园'
          },
          {
            stage: '采摘',
            time: '2024-01-15 10:00:00',
            detail: '成熟苹果采摘，质量检测合格',
            operator: '李四',
            location: '山东烟台果园'
          },
          {
            stage: '加工',
            time: '2024-01-16 14:00:00',
            detail: '清洗、分级、包装',
            operator: '王五',
            location: '烟台加工厂'
          },
          {
            stage: '运输',
            time: '2024-01-17 06:00:00',
            detail: '冷链运输，温度: 4°C',
            operator: '赵六',
            location: '烟台 -> 北京'
          },
          {
            stage: '销售',
            time: '2024-01-18 09:00:00',
            detail: '上架销售',
            operator: '钱七',
            location: '北京某超市'
          }
        ]
      }
    }
    
    return {
      searchQuery,
      traceResult,
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

.footer {
  text-align: center;
  color: #fff;
  margin-top: 40px;
  opacity: 0.8;
}
</style>
