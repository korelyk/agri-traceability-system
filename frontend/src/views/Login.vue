<template>
  <div class="login-page">
    <div class="ambient ambient-left" />
    <div class="ambient ambient-right" />

    <div class="login-layout">
      <section class="brand-side">
        <div class="hero-eyebrow">
          <el-icon><Connection /></el-icon>
          可信追溯成品系统
        </div>
        <h1 class="brand-title">让农产品信息不只可查，更能被验证。</h1>
        <p class="brand-text">
          平台围绕农产品建档、流转、检测、公开查询和监管核验构建完整闭环。
          关键记录通过数字签名确认主体身份，再通过区块链进行链上存证，减少历史数据被篡改的风险。
        </p>

        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon">01</div>
            <div>
              <div class="feature-title">主体身份可信</div>
              <div class="feature-text">每个业务主体对应独立密钥对，关键业务记录签名后再进入链上校验。</div>
            </div>
          </div>

          <div class="feature-item">
            <div class="feature-icon">02</div>
            <div>
              <div class="feature-title">链上流转可核验</div>
              <div class="feature-text">记录与区块哈希关联，支持区块浏览、签名验签、数据库一致性复核。</div>
            </div>
          </div>

          <div class="feature-item">
            <div class="feature-icon">03</div>
            <div>
              <div class="feature-title">检测监管可展示</div>
              <div class="feature-text">支持检测结果、证书编号、文档哈希与公开溯源页联动展示。</div>
            </div>
          </div>
        </div>
      </section>

      <section class="auth-panel">
        <div class="auth-header">
          <div class="auth-brand">
            <div class="auth-logo">
              <el-icon :size="24"><Connection /></el-icon>
            </div>
            <div>
              <div class="auth-title">农产品可信溯源平台</div>
              <div class="auth-subtitle">登录后进入业务管理与监管视图</div>
            </div>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="auth-tabs">
          <el-tab-pane label="登录" name="login">
            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="auth-form">
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  size="large"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="submit-button"
                  :loading="loading"
                  @click="handleLogin"
                >
                  登录系统
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="auth-form">
              <el-form-item prop="username">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  show-password
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入密码"
                  :prefix-icon="Lock"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="realName">
                <el-input
                  v-model="registerForm.realName"
                  placeholder="请输入真实姓名"
                  :prefix-icon="UserFilled"
                  size="large"
                />
              </el-form-item>
              <el-form-item prop="userType">
                <el-select
                  v-model="registerForm.userType"
                  placeholder="请选择用户类型"
                  size="large"
                  style="width: 100%"
                >
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
                  placeholder="请输入单位名称"
                  :prefix-icon="OfficeBuilding"
                  size="large"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="submit-button"
                  :loading="loading"
                  @click="handleRegister"
                >
                  提交注册
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="auth-footer">
          <span>面向农产品可信流转的区块链溯源成品系统</span>
          <span>2026</span>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  Connection,
  Lock,
  OfficeBuilding,
  User,
  UserFilled
} from '@element-plus/icons-vue'

export default {
  name: 'Login',
  components: {
    Connection
  },
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
        return
      }
      callback()
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
      companyName: [{ required: true, message: '请输入单位名称', trigger: 'blur' }]
    }

    const handleLogin = async () => {
      const valid = await loginFormRef.value.validate().catch(() => false)
      if (!valid) {
        return
      }

      loading.value = true
      const result = await store.dispatch('login', loginForm)
      loading.value = false

      if (result.success) {
        ElMessage.success('登录成功')
        router.push('/dashboard')
        return
      }

      ElMessage.error(result.message)
    }

    const handleRegister = async () => {
      const valid = await registerFormRef.value.validate().catch(() => false)
      if (!valid) {
        return
      }

      loading.value = true
      const result = await store.dispatch('register', registerForm)
      loading.value = false

      if (result.success) {
        ElMessage.success('注册成功，请使用新账号登录')
        activeTab.value = 'login'
        return
      }

      ElMessage.error(result.message)
    }

    return {
      activeTab,
      handleLogin,
      handleRegister,
      loading,
      loginForm,
      loginFormRef,
      loginRules,
      registerForm,
      registerFormRef,
      registerRules,
      Lock,
      OfficeBuilding,
      User,
      UserFilled
    }
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  padding: 36px;
  overflow: hidden;
  background:
    linear-gradient(125deg, rgba(23, 59, 43, 0.96), rgba(27, 79, 54, 0.92)),
    linear-gradient(180deg, #183426, #11241b);
}

.ambient {
  position: absolute;
  border-radius: 50%;
  filter: blur(10px);
}

.ambient-left {
  width: 360px;
  height: 360px;
  top: -120px;
  left: -40px;
  background: rgba(106, 196, 141, 0.18);
}

.ambient-right {
  width: 420px;
  height: 420px;
  right: -140px;
  bottom: -180px;
  background: rgba(214, 171, 104, 0.16);
}

.login-layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(420px, 520px);
  gap: 28px;
  align-items: stretch;
  max-width: 1280px;
  min-height: calc(100vh - 72px);
  margin: 0 auto;
}

.brand-side {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 24px;
  padding: 36px 24px 36px 12px;
  color: #f4faf5;
}

.brand-title {
  max-width: 680px;
  margin: 0;
  font-size: clamp(42px, 5vw, 62px);
  line-height: 1.05;
  letter-spacing: -0.03em;
}

.brand-text {
  max-width: 680px;
  margin: 0;
  color: rgba(244, 250, 245, 0.78);
  font-size: 16px;
  line-height: 1.9;
}

.feature-list {
  display: grid;
  gap: 16px;
  max-width: 720px;
}

.feature-item {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 14px;
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(14px);
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.12);
  color: #f2d7a9;
  font-weight: 700;
}

.feature-title {
  font-size: 18px;
  font-weight: 700;
}

.feature-text {
  margin-top: 6px;
  color: rgba(244, 250, 245, 0.78);
  line-height: 1.8;
  font-size: 14px;
}

.auth-panel {
  align-self: center;
  padding: 28px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.28);
  box-shadow: 0 28px 50px rgba(10, 27, 20, 0.26);
  backdrop-filter: blur(16px);
}

.auth-header {
  margin-bottom: 16px;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.auth-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: linear-gradient(135deg, #5dbb83, #2f7d58);
  color: #fff;
}

.auth-title {
  font-size: 24px;
  font-weight: 700;
  color: #193026;
}

.auth-subtitle {
  margin-top: 4px;
  color: #6d7b72;
  font-size: 14px;
}

.auth-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.auth-tabs :deep(.el-tabs__item) {
  font-weight: 700;
}

.auth-form {
  margin-top: 18px;
}

.submit-button {
  width: 100%;
  height: 48px;
  margin-top: 8px;
}

.auth-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid rgba(25, 48, 38, 0.08);
  color: #7a877f;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .login-page {
    padding: 20px;
  }

  .login-layout {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .brand-side {
    padding: 10px 4px 0;
  }
}

@media (max-width: 640px) {
  .brand-side {
    gap: 18px;
  }

  .brand-title {
    font-size: 34px;
  }

  .auth-panel {
    padding: 20px;
  }

  .auth-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
