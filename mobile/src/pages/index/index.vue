<template>
  <view class="container">
    <view class="header">
      <image class="logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="title">农产品防伪溯源平台</text>
      <text class="subtitle">基于区块链与数字签名的可信溯源</text>
    </view>

    <view class="action-card">
      <view class="scan-btn" @click="handleScan">
        <uni-icons type="scan" size="48" color="#ffffff"></uni-icons>
        <text class="btn-text">扫码溯源</text>
      </view>
    </view>

    <view class="manual-input">
      <text class="label">或者手动输入产品溯源码：</text>
      <view class="input-group">
        <input type="text" v-model="productId" placeholder="请输入产品编号" class="input" />
        <button type="primary" size="mini" class="search-btn" @click="handleManualSearch">查询</button>
      </view>
    </view>

    <view class="stats-card" v-if="stats">
      <view class="stats-item">
        <text class="stats-num">{{ stats.totalProducts || 0 }}</text>
        <text class="stats-label">已保护产品</text>
      </view>
      <view class="stats-item">
        <text class="stats-num">{{ stats.totalBlocks || 1 }}</text>
        <text class="stats-label">区块高度</text>
      </view>
      <view class="stats-item">
        <text class="stats-num">{{ stats.totalTraceRecords || 0 }}</text>
        <text class="stats-label">溯源记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const productId = ref('')
const stats = ref(null)

const fetchStats = async () => {
  try {
    const res = await request.get('/api/statistics')
    if (res.success) {
      stats.value = res.data
    }
  } catch (e) {
    console.error('Failed to load stats', e)
  }
}

onMounted(() => {
  fetchStats()
})

const handleScan = () => {
  uni.scanCode({
    success: (res) => {
      console.log('Scan result:', res.result)
      // Assume the QR code contains the product ID directly
      navigateToDetail(res.result)
    },
    fail: (err) => {
      uni.showToast({ title: '扫码失败或取消', icon: 'none' })
    }
  })
}

const handleManualSearch = () => {
  if (!productId.value) {
    return uni.showToast({ title: '请输入产品编号', icon: 'none' })
  }
  navigateToDetail(productId.value)
}

const navigateToDetail = (id) => {
  uni.navigateTo({
    url: `/pages/product/detail?id=${id}`
  })
}
</script>

<style lang="scss">
.container {
  padding: 30rpx;
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  margin-top: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;

  .logo {
    width: 160rpx;
    height: 160rpx;
    margin-bottom: 20rpx;
    border-radius: 50%;
    background-color: #fff;
    padding: 10rpx;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
  }

  .title {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 10rpx;
  }

  .subtitle {
    font-size: 26rpx;
    color: #666;
  }
}

.action-card {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 60rpx;

  .scan-btn {
    width: 320rpx;
    height: 320rpx;
    background: linear-gradient(135deg, #07c160, #10a355);
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: 0 16rpx 40rpx rgba(7, 193, 96, 0.4);
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.95);
      box-shadow: 0 8rpx 20rpx rgba(7, 193, 96, 0.3);
    }

    .btn-text {
      color: #ffffff;
      font-size: 32rpx;
      margin-top: 20rpx;
      font-weight: 500;
    }
  }
}

.manual-input {
  width: 100%;
  background: #ffffff;
  padding: 30rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
  margin-bottom: 60rpx;
  box-sizing: border-box;

  .label {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
    display: block;
  }

  .input-group {
    display: flex;
    align-items: center;

    .input {
      flex: 1;
      height: 80rpx;
      background: #f8f9fa;
      border-radius: 12rpx;
      padding: 0 20rpx;
      font-size: 28rpx;
      margin-right: 20rpx;
    }

    .search-btn {
      margin: 0;
      height: 80rpx;
      line-height: 80rpx;
      border-radius: 12rpx;
      background-color: #07c160;
    }
  }
}

.stats-card {
  width: 100%;
  display: flex;
  justify-content: space-between;
  background: #ffffff;
  padding: 40rpx 30rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
  box-sizing: border-box;

  .stats-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    border-right: 2rpx solid #eee;

    &:last-child {
      border-right: none;
    }

    .stats-num {
      font-size: 40rpx;
      font-weight: bold;
      color: #07c160;
      margin-bottom: 10rpx;
    }

    .stats-label {
      font-size: 24rpx;
      color: #666;
    }
  }
}
</style>
