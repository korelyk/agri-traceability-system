<template>
  <view class="detail-container" v-if="product">
    <view class="product-header">
      <view class="title-row">
        <text class="product-name">{{ product.productName }}</text>
        <view class="status-tag" :class="getStatusClass(product.currentStatus)">
          {{ getStatusText(product.currentStatus) }}
        </view>
      </view>
      <text class="product-desc">{{ product.description }}</text>
      
      <view class="info-grid">
        <view class="info-item">
          <text class="label">产品批次</text>
          <text class="value">{{ product.batchNumber }}</text>
        </view>
        <view class="info-item">
          <text class="label">生产商</text>
          <text class="value">{{ product.producerName }}</text>
        </view>
        <view class="info-item">
          <text class="label">产地</text>
          <text class="value">{{ product.origin }}</text>
        </view>
        <view class="info-item">
          <text class="label">生产时间</text>
          <text class="value">{{ formatDate(product.productionDate) }}</text>
        </view>
      </view>

      <view class="blockchain-info">
        <uni-icons type="link" size="16" color="#07c160"></uni-icons>
        <text class="hash-text">区块哈希: {{ formatHash(product.blockHash) }}</text>
      </view>
    </view>

    <view class="trace-section">
      <view class="section-title">
        <text class="title-text">区块链溯源档案</text>
        <view class="verify-badge" v-if="isValid">
          <uni-icons type="checkbox-filled" size="16" color="#07c160"></uni-icons>
          <text>数据未篡改</text>
        </view>
      </view>
      
      <view class="timeline">
        <view class="timeline-item" v-for="(record, index) in traceRecords" :key="index">
          <view class="timeline-dot"></view>
          <view class="timeline-content">
            <view class="record-header">
              <text class="op-type">{{ getOperationTypeText(record.operationType) }}</text>
              <text class="op-time">{{ formatDate(record.operationTime) }}</text>
            </view>
            <view class="record-body">
              <text class="record-detail">{{ record.operationDetail }}</text>
              <view class="operator-info">
                <text>操作人: {{ record.operatorName }}</text>
                <text>地点: {{ record.location }}</text>
              </view>
              <view class="env-data" v-if="record.temperature || record.humidity">
                <text class="env-item" v-if="record.temperature">温度: {{ record.temperature }}℃</text>
                <text class="env-item" v-if="record.humidity">湿度: {{ record.humidity }}%</text>
              </view>
            </view>
            <view class="record-footer">
              <text class="tx-id">交易: {{ formatHash(record.transactionId) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <view class="auth-actions" v-if="isLoggedIn && canAddTrace">
      <button class="add-btn" type="primary" @click="goAddTrace">添加流转记录</button>
    </view>
  </view>
  <view class="loading-state" v-else>
    <uni-load-more status="loading" />
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request'

const productId = ref('')
const product = ref(null)
const traceRecords = ref([])
const isValid = ref(false)

const isLoggedIn = computed(() => {
  return !!uni.getStorageSync('token')
})

const canAddTrace = computed(() => {
  // Only logic simplified for demo: any logged in user can add trace
  return true
})

onLoad((options) => {
  if (options.id) {
    productId.value = options.id
    fetchTraceDetail()
  } else {
    uni.showToast({ title: '缺少产品ID', icon: 'none' })
  }
})

const fetchTraceDetail = async () => {
  try {
    const res = await request.get(`/api/trace/${productId.value}`)
    if (res.success) {
      product.value = res.data.product
      // The API returns history ordered by time ASC. Let's reverse it to show latest first.
      traceRecords.value = (res.data.traceHistory || []).reverse()
      isValid.value = res.data.blockchainValid && res.data.dataConsistent
    }
  } catch (e) {
    console.error(e)
  }
}

const goAddTrace = () => {
  uni.navigateTo({
    url: `/pages/trace/add?productId=${productId.value}`
  })
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const formatHash = (hash) => {
  if (!hash) return ''
  return hash.substring(0, 10) + '...' + hash.substring(hash.length - 8)
}

const getStatusText = (status) => {
  const texts = {
    CREATED: '已创建',
    PRODUCE: '生产中',
    PROCESS: '加工中',
    TRANSPORT: '运输中',
    STORAGE: '仓储中',
    SALE: '销售中'
  }
  return texts[status] || status || '-'
}

const getOperationTypeText = (type) => {
  const texts = {
    PRODUCE: '生产',
    PROCESS: '加工',
    PACKAGE: '包装',
    STORAGE: '仓储',
    TRANSPORT: '运输',
    SALE: '销售',
    INSPECT: '检测'
  }
  return texts[type] || type || '-'
}

const getStatusClass = (status) => {
  if (status === 'CREATED' || status === 'PRODUCE') return 'status-primary'
  if (status === 'PROCESS' || status === '加工') return 'status-warning'
  if (status === 'SALE' || status === '销售') return 'status-success'
  return 'status-default'
}
</script>

<style lang="scss">
.detail-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 120rpx;
}
.loading-state {
  padding-top: 200rpx;
}
.product-header {
  background-color: #fff;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.02);
  
  .title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    
    .product-name {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
    }
    .status-tag {
      padding: 4rpx 16rpx;
      border-radius: 8rpx;
      font-size: 24rpx;
      &.status-primary { background: #e1f3d8; color: #67c23a; }
      &.status-warning { background: #faecd8; color: #e6a23c; }
      &.status-success { background: #f0f9eb; color: #67c23a; }
      &.status-default { background: #f4f4f5; color: #909399; }
    }
  }
  
  .product-desc {
    font-size: 28rpx;
    color: #666;
    margin-bottom: 30rpx;
    display: block;
  }
  
  .info-grid {
    display: flex;
    flex-wrap: wrap;
    background: #f8f9fa;
    padding: 20rpx;
    border-radius: 12rpx;
    margin-bottom: 20rpx;
    
    .info-item {
      width: 50%;
      margin-bottom: 16rpx;
      display: flex;
      flex-direction: column;
      
      .label {
        font-size: 24rpx;
        color: #999;
        margin-bottom: 4rpx;
      }
      .value {
        font-size: 28rpx;
        color: #333;
      }
    }
  }
  
  .blockchain-info {
    display: flex;
    align-items: center;
    background: #e8f5e9;
    padding: 10rpx 20rpx;
    border-radius: 8rpx;
    .hash-text {
      font-size: 24rpx;
      color: #07c160;
      margin-left: 10rpx;
      font-family: monospace;
    }
  }
}

.trace-section {
  background-color: #fff;
  padding: 40rpx 30rpx;
  
  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 40rpx;
    
    .title-text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      position: relative;
      padding-left: 20rpx;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 10%;
        height: 80%;
        width: 8rpx;
        background-color: #07c160;
        border-radius: 4rpx;
      }
    }
    
    .verify-badge {
      display: flex;
      align-items: center;
      background: #eaffea;
      padding: 6rpx 16rpx;
      border-radius: 30rpx;
      text {
        font-size: 24rpx;
        color: #07c160;
        margin-left: 8rpx;
      }
    }
  }
  
  .timeline {
    position: relative;
    padding-left: 30rpx;
    
    &::before {
      content: '';
      position: absolute;
      left: 38rpx;
      top: 20rpx;
      bottom: 20rpx;
      width: 4rpx;
      background-color: #e4e7ed;
    }
    
    .timeline-item {
      position: relative;
      margin-bottom: 40rpx;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .timeline-dot {
        position: absolute;
        left: -4rpx;
        top: 10rpx;
        width: 20rpx;
        height: 20rpx;
        background-color: #07c160;
        border-radius: 50%;
        border: 4rpx solid #fff;
        box-shadow: 0 0 0 4rpx rgba(7, 193, 96, 0.2);
        z-index: 2;
      }
      
      .timeline-content {
        margin-left: 40rpx;
        background: #f8f9fa;
        padding: 24rpx;
        border-radius: 12rpx;
        
        .record-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16rpx;
          border-bottom: 2rpx solid #eee;
          padding-bottom: 12rpx;
          
          .op-type {
            font-size: 30rpx;
            font-weight: bold;
            color: #333;
          }
          .op-time {
            font-size: 24rpx;
            color: #999;
          }
        }
        
        .record-body {
          margin-bottom: 16rpx;
          
          .record-detail {
            font-size: 28rpx;
            color: #444;
            display: block;
            margin-bottom: 12rpx;
          }
          
          .operator-info {
            display: flex;
            justify-content: space-between;
            font-size: 24rpx;
            color: #666;
            margin-bottom: 12rpx;
          }
          
          .env-data {
            background: #eef1f6;
            padding: 10rpx 16rpx;
            border-radius: 8rpx;
            display: flex;
            gap: 20rpx;
            
            .env-item {
              font-size: 24rpx;
              color: #555;
            }
          }
        }
        
        .record-footer {
          .tx-id {
            font-size: 22rpx;
            color: #aaa;
            font-family: monospace;
          }
        }
      }
    }
  }
}

.auth-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background-color: #fff;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
  z-index: 10;
  
  .add-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    background-color: #07c160;
    font-size: 32rpx;
    border-radius: 44rpx;
  }
}
</style>
