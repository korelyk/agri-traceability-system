<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon size="48" color="#67C23A"><Connection /></el-icon>
        <h1 class="title">农产品防伪溯源系统</h1>
        <p class="subtitle">基于区块链与数字签名技术</p>
      </div>
      
      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                :prefix-icon="Lock"
                size="large"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                :prefix-icon="Lock"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                :prefix-icon="Lock"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="realName">
              <el-input
                v-model="registerForm.realName"
                placeholder="真实姓名"
                :prefix-icon="UserFilled"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="userType">
              <el-select v-model="registerForm.userType" placeholder="用户类型" size="large" style="width: 100%">
                <el-option label="生产者" value="PRODUCER" />
                <el-option label="加工商" value="PROCESSOR" />
                <el-option label="物流商" value="LOGISTICS" />
                <el-option label="销售商" value="RETAILER" />
                <el-option label="检测机构" value="INSPECTOR" />
              </el-select>
            </el-form-item>
            <el-form-item prop="companyName">
              <el-input
                v-model="registerForm.companyName"
                placeholder="公司名称"
                :prefix-icon="OfficeBuilding"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <div class="login-footer">
      <p>© 2024 农产品防伪溯源系统 - 基于区块链与数字签名技术</p>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  setup() {
    const store = useStore()
    const router = useRouter()
    
    const activeTab = ref('login')
    const loading = ref(false)
    const loginFormRef = ref(null)
    const registerFormRef = ref(null)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      realName: '',
      userType: '',
      companyName: ''
    })
    
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    
    const loginRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }
    
    const registerRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ],
      realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
      userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
      companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
    }
    
    const handleLogin = async () => {
      const valid = await loginFormRef.value.validate().catch(() => false)
      if (!valid) return
      
      loading.value = true
      const result = await store.dispatch('login', loginForm)
      loading.value = false
      
      if (result.success) {
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } else {
        ElMessage.error(result.message)
      }
    }
    
    const handleRegister = async () => {
      const valid = await registerFormRef.value.validate().catch(() => false)
      if (!valid) return
      
      loading.value = true
      const result = await store.dispatch('register', registerForm)
      loading.value = false
      
      if (result.success) {
        ElMessage.success('注册成功，请登录')
        activeTab.value = 'login'
      } else {
        ElMessage.error(result.message)
      }
    }
    
    return {
      activeTab,
      loading,
      loginForm,
      registerForm,
      loginFormRef,
      registerFormRef,
      loginRules,
      registerRules,
      handleLogin,
      handleRegister
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  font-size: 24px;
  color: #303133;
  margin: 15px 0 5px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.login-tabs {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
}

.login-footer {
  position: fixed;
  bottom: 20px;
  color: #fff;
  font-size: 12px;
  opacity: 0.8;
}
</style>
