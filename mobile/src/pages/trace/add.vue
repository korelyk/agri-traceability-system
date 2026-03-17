<template>
  <view class="add-container">
    <view class="tips">
      <uni-icons type="info" size="20" color="#07c160"></uni-icons>
      <text class="tips-text">提交的数据会进行数字签名并写入区块链，写入后不可篡改。</text>
    </view>
    
    <view class="form-box">
      <uni-forms ref="formRef" :modelValue="formData" :rules="rules" label-width="80">
        <uni-forms-item name="operationType" label="操作类型" required>
          <uni-data-select v-model="formData.operationType" :localdata="typeOptions"></uni-data-select>
        </uni-forms-item>
        
        <uni-forms-item name="location" label="操作地点" required>
          <view class="location-row">
            <uni-easyinput type="text" v-model="formData.location" placeholder="请填写或获取当前位置" />
            <uni-icons type="location-filled" size="28" color="#07c160" @click="getLocation"></uni-icons>
          </view>
        </uni-forms-item>
        
        <uni-forms-item name="operationDetail" label="操作详情" required>
          <uni-easyinput type="textarea" v-model="formData.operationDetail" placeholder="请描述本次操作内容" :maxlength="200"></uni-easyinput>
        </uni-forms-item>

        <template v-if="formData.operationType === 'PROCESS'">
          <uni-forms-item name="temperature" label="处理温度">
            <uni-easyinput type="digit" v-model="formData.temperature" placeholder="单位: ℃" />
          </uni-forms-item>
        </template>
        
        <template v-if="formData.operationType === 'STORAGE'">
          <uni-forms-item name="temperature" label="仓储温度">
            <uni-easyinput type="digit" v-model="formData.temperature" placeholder="单位: ℃" />
          </uni-forms-item>
          <uni-forms-item name="humidity" label="仓储湿度">
            <uni-easyinput type="digit" v-model="formData.humidity" placeholder="单位: %" />
          </uni-forms-item>
        </template>
      </uni-forms>
    </view>
    
    <view class="btn-group">
      <button class="submit-btn" type="primary" @click="handleSubmit" :loading="isSubmitting">签名并上链</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request'

const productId = ref('')
const formRef = ref(null)
const isSubmitting = ref(false)

const formData = ref({
  operationType: '',
  location: '',
  operationDetail: '',
  temperature: '',
  humidity: ''
})

const typeOptions = [
  { value: 'PRODUCE', text: '生产/种植' },
  { value: 'PROCESS', text: '初加工' },
  { value: 'PACKAGE', text: '包装' },
  { value: 'STORAGE', text: '入库/仓储' },
  { value: 'TRANSPORT', text: '出库/运输' },
  { value: 'SALE', text: '上架销售' }
]

const rules = {
  operationType: { rules: [{ required: true, errorMessage: '请选择操作类型' }] },
  location: { rules: [{ required: true, errorMessage: '请填写操作地点' }] },
  operationDetail: { rules: [{ required: true, errorMessage: '请填写操作详情' }] }
}

onLoad((options) => {
  if (options.productId) {
    productId.value = options.productId
  }
})

const getLocation = () => {
  uni.showLoading({ title: '定位中...' })
  setTimeout(() => {
    formData.value.location = '上海市浦东新区张江高科技园区'
    uni.hideLoading()
  }, 1000)
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    if (!productId.value) {
      return uni.showToast({ title: '无法获取关联产品ID', icon: 'none' })
    }

    const userInfo = uni.getStorageSync('userInfo')
    if (!userInfo?.userId) {
      return uni.showToast({ title: '请先登录', icon: 'none' })
    }

    isSubmitting.value = true

    const environmentPayload = {}
    if (formData.value.temperature) {
      environmentPayload.temperature = parseFloat(formData.value.temperature)
    }
    if (formData.value.humidity) {
      environmentPayload.humidity = parseFloat(formData.value.humidity)
    }

    const payload = {
      productId: productId.value,
      operatorId: userInfo.userId,
      operationType: formData.value.operationType,
      operationDetail: formData.value.operationDetail,
      location: formData.value.location,
      environmentData: Object.keys(environmentPayload).length ? JSON.stringify(environmentPayload) : ''
    }

    const res = await request.post('/api/trace/add', payload)

    if (res.success) {
      uni.showToast({ title: '上链成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (e) {
    console.error(e)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style lang="scss">
.add-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 30rpx;
}
.tips {
  display: flex;
  background-color: #e8f5e9;
  padding: 20rpx;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
  align-items: flex-start;
  
  .tips-text {
    font-size: 26rpx;
    color: #4caf50;
    margin-left: 10rpx;
    line-height: 1.5;
    flex: 1;
  }
}

.form-box {
  background-color: #fff;
  padding: 30rpx 20rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
  margin-bottom: 60rpx;
}

.location-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.submit-btn {
  background-color: #07c160;
  border-radius: 44rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 34rpx;
  box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.3);
}
</style>
