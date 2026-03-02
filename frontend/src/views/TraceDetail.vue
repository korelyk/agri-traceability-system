<template>
  <div class="trace-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>溯源详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      
      <div v-if="traceData">
        <!-- 产品信息 -->
        <el-descriptions title="产品信息" :column="2" border>
          <el-descriptions-item label="产品ID">
            {{ traceData.product?.productId }}
          </el-descriptions-item>
          <el-descriptions-item label="产品名称">
            {{ traceData.product?.productName }}
          </el-descriptions-item>
          <el-descriptions-item label="产品类别">
            {{ traceData.product?.productCategory }}
          </el-descriptions-item>
          <el-descriptions-item label="批次号">
            {{ traceData.product?.batchNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="生产者">
            {{ traceData.product?.producerName }}
          </el-descriptions-item>
          <el-descriptions-item label="产地">
            {{ traceData.product?.origin }}
          </el-descriptions-item>
          <el-descriptions-item label="生产日期">
            {{ formatTime(traceData.product?.productionDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(traceData.product?.currentStatus)">
              {{ getStatusText(traceData.product?.currentStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 验证信息 -->
        <el-alert
          :title="traceData.dataConsistent ? '数据验证通过' : '数据存在异常'"
          :type="traceData.dataConsistent ? 'success' : 'error'"
          :description="traceData.blockchainValid ? '区块链验证通过' : '区块链验证失败'"
          show-icon
          class="verify-alert"
        />
        
        <!-- 溯源时间线 -->
        <h3 class="section-title">溯源历史</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(record, index) in traceData.traceHistory"
            :key="index"
            :type="getTimelineType(index)"
            :color="getTimelineColor(index)"
            :timestamp="formatTime(record.operationTime)"
            placement="top"
          >
            <el-card>
              <template #header>
                <div class="record-header">
                  <span class="operation-type">{{ record.operationTypeName }}</span>
                  <el-tag v-if="record.verified" type="success" size="small">
                    <el-icon><CircleCheck /></el-icon> 已验证
                  </el-tag>
                </div>
              </template>
              <div class="record-content">
                <p><strong>操作者:</strong> {{ record.operatorName }} ({{ record.operatorType }})</p>
                <p><strong>操作地点:</strong> {{ record.location }}</p>
                <p><strong>操作详情:</strong> {{ record.operationDetail }}</p>
                <div v-if="record.temperature || record.humidity" class="environment-data">
                  <p><strong>环境数据:</strong></p>
                  <el-tag v-if="record.temperature" size="small">温度: {{ record.temperature }}°C</el-tag>
                  <el-tag v-if="record.humidity" size="small">湿度: {{ record.humidity }}%</el-tag>
                </div>
                <div v-if="record.blockHash" class="blockchain-info">
                  <p><strong>区块哈希:</strong></p>
                  <code>{{ record.blockHash }}</code>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        
        <!-- 统计信息 -->
        <el-card class="statistics-card">
          <template #header>
            <span>统计信息</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ traceData.statistics?.totalRecords }}</div>
                <div class="stat-label">总记录数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ traceData.statistics?.verifiedRecords }}</div>
                <div class="stat-label">已验证记录</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-value">{{ traceData.statistics?.operationTypes?.length }}</div>
                <div class="stat-label">操作类型</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export default {
  name: 'TraceDetail',
  setup() {
    const route = useRoute()
    const store = useStore()
    const loading = ref(false)
    const traceData = ref(null)
    
    const fetchTraceData = async () => {
      loading.value = true
      const productId = route.params.productId
      
      const result = await store.dispatch('traceProduct', productId)
      
      if (result.success) {
        traceData.value = result.data
      } else {
        ElMessage.error(result.message)
      }
      
      loading.value = false
    }
    
    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    }
    
    const getStatusType = (status) => {
      const types = {
        'CREATED': 'info',
        'PRODUCE': 'success',
        'PROCESS': 'warning',
        'TRANSPORT': 'primary',
        'STORAGE': 'info',
        'SALE': 'danger'
      }
      return types[status] || 'info'
    }
    
    const getStatusText = (status) => {
      const texts = {
        'CREATED': '已创建',
        'PRODUCE': '生产中',
        'PROCESS': '加工中',
        'TRANSPORT': '运输中',
        'STORAGE': '仓储中',
        'SALE': '销售中'
      }
      return texts[status] || status
    }
    
    const getTimelineType = (index) => {
      if (index === 0) return 'primary'
      return ''
    }
    
    const getTimelineColor = (index) => {
      const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
      return colors[index % colors.length]
    }
    
    onMounted(() => {
      fetchTraceData()
    })
    
    return {
      loading,
      traceData,
      formatTime,
      getStatusType,
      getStatusText,
      getTimelineType,
      getTimelineColor
    }
  }
}
</script>

<style scoped>
.trace-detail-page {
  padding: 20px;
}

.verify-alert {
  margin: 20px 0;
}

.section-title {
  margin: 30px 0 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
  color: #303133;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.operation-type {
  font-weight: bold;
  font-size: 16px;
}

.record-content p {
  margin: 8px 0;
}

.environment-data {
  margin-top: 10px;
}

.environment-data .el-tag {
  margin-right: 10px;
}

.blockchain-info {
  margin-top: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.blockchain-info code {
  font-size: 11px;
  word-break: break-all;
}

.statistics-card {
  margin-top: 30px;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  margin-top: 10px;
  color: #909399;
}
</style>
