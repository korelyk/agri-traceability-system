<template>
  <view class="login-container">
    <view class="header">
      <text class="title">{{ isLogin ? '工作台登录' : '系统注册' }}</text>
      <text class="subtitle">农产品溯源系统移动端</text>
    </view>

    <view class="form-box">
      <uni-forms ref="formRef" :modelValue="formData" :rules="rules">
        <uni-forms-item name="username" label="用户名">
          <uni-easyinput type="text" v-model="formData.username" placeholder="请输入用户名" />
        </uni-forms-item>
        <uni-forms-item name="password" label="密码">
          <uni-easyinput type="password" v-model="formData.password" placeholder="请输入密码" />
        </uni-forms-item>

        <template v-if="!isLogin">
          <uni-forms-item name="realName" label="真实姓名">
            <uni-easyinput type="text" v-model="formData.realName" placeholder="请输入真实姓名" />
          </uni-forms-item>
          <uni-forms-item name="userType" label="用户类型">
            <uni-data-select v-model="formData.userType" :localdata="userTypeOptions"></uni-data-select>
          </uni-forms-item>
          <uni-forms-item name="companyName" label="公司名称">
            <uni-easyinput type="text" v-model="formData.companyName" placeholder="请输入公司/农场名称" />
          </uni-forms-item>
        </template>
      </uni-forms>

      <view class="btn-group">
        <button class="submit-btn" type="primary" @click="handleSubmit">{{ isLogin ? '登录' : '注册' }}</button>
      </view>

      <view class="switch-mode">
        <text class="link-text" @click="toggleMode">{{ isLogin ? '没有账号？立即注册' : '已有账号？去登录' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'

const isLogin = ref(true)
const formRef = ref(null)

const formData = ref({
  username: '',
  password: '',
  realName: '',
  userType: 'PRODUCER',
  companyName: ''
})

const userTypeOptions = [
  { value: 'PRODUCER', text: '生产者' },
  { value: 'PROCESSOR', text: '加工商' },
  { value: 'LOGISTICS', text: '物流商' },
  { value: 'RETAILER', text: '零售商' }
]

const rules = {
  username: { rules: [{ required: true, errorMessage: '用户名不能为空' }] },
  password: { rules: [{ required: true, errorMessage: '密码不能为空' }] }
}

const toggleMode = () => {
  isLogin.value = !isLogin.value
}

const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    uni.showLoading({ title: '请求中...' })

    if (isLogin.value) {
      const res = await request.post('/api/users/login', {
        username: formData.value.username,
        password: formData.value.password
      })
      if (res.success && res.data?.token) {
        uni.setStorageSync('token', res.data.token)
        uni.setStorageSync('userInfo', res.data.user)
        uni.showToast({ title: '登录成功' })
        setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 1000)
      }
    } else {
      const res = await request.post('/api/users/register', formData.value)
      if (res.success) {
        uni.showToast({ title: '注册成功，请登录' })
        isLogin.value = true
      }
    }
  } catch (err) {
    console.error(err)
  } finally {
    uni.hideLoading()
  }
}
</script>

<style lang="scss">
.login-container {
  min-height: 100vh;
  background-color: #fff;
  padding: 60rpx 40rpx;
}
.header {
  margin-top: 80rpx;
  margin-bottom: 80rpx;
  .title {
    font-size: 56rpx;
    font-weight: bold;
    color: #333;
    display: block;
    margin-bottom: 20rpx;
  }
  .subtitle {
    font-size: 30rpx;
    color: #888;
  }
}
.form-box {
  margin-top: 40rpx;
}
.submit-btn {
  margin-top: 60rpx;
  background-color: #07c160;
  border-radius: 44rpx;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 34rpx;
}
.switch-mode {
  margin-top: 40rpx;
  text-align: center;
  .link-text {
    font-size: 28rpx;
    color: #07c160;
  }
}
</style>
