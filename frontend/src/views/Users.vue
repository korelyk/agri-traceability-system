<template>
  <div class="users-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="showRegisterDialog = true">
            <el-icon><Plus /></el-icon> 添加用户
          </el-button>
        </div>
      </template>
      
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="userTypeName" label="用户类型" />
        <el-table-column prop="companyName" label="公司名称" />
        <el-table-column prop="blockchainAddress" label="区块链地址" width="200">
          <template #default="scope">
            <code>{{ scope.row.blockchainAddress?.substring(0, 20) }}...</code>
          </template>
        </el-table-column>
        <el-table-column prop="verified" label="认证状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.verified ? 'success' : 'warning'">
              {{ scope.row.verified ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewUser(scope.row)">
              查看
            </el-button>
            <el-button link :type="scope.row.verified ? 'danger' : 'success'" 
                       @click="toggleVerify(scope.row)">
              {{ scope.row.verified ? '取消认证' : '认证' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加用户对话框 -->
    <el-dialog v-model="showRegisterDialog" title="添加用户" width="500px">
      <el-form :model="newUser" :rules="userRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="newUser.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="newUser.password" type="password" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="newUser.realName" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="newUser.userType" style="width: 100%">
            <el-option label="生产者" value="PRODUCER" />
            <el-option label="加工商" value="PROCESSOR" />
            <el-option label="物流商" value="LOGISTICS" />
            <el-option label="销售商" value="RETAILER" />
          </el-select>
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="newUser.companyName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddUser" :loading="adding">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export default {
  name: 'Users',
  setup() {
    const store = useStore()
    const users = ref([])
    const loading = ref(false)
    const showRegisterDialog = ref(false)
    const adding = ref(false)
    const userFormRef = ref(null)
    
    const newUser = reactive({
      username: '',
      password: '',
      realName: '',
      userType: '',
      companyName: ''
    })
    
    const userRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
      userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
      companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
    }
    
    const fetchUsers = async () => {
      loading.value = true
      const result = await store.dispatch('fetchUsers')
      if (result.success) {
        users.value = result.data
      }
      loading.value = false
    }
    
    const handleAddUser = async () => {
      const valid = await userFormRef.value.validate().catch(() => false)
      if (!valid) return
      
      adding.value = true
      const result = await store.dispatch('register', newUser)
      adding.value = false
      
      if (result.success) {
        ElMessage.success('用户添加成功')
        showRegisterDialog.value = false
        fetchUsers()
      } else {
        ElMessage.error(result.message)
      }
    }
    
    const viewUser = (user) => {
      // 查看用户详情
    }
    
    const toggleVerify = (user) => {
      // 切换认证状态
    }
    
    onMounted(() => {
      fetchUsers()
    })
    
    return {
      users,
      loading,
      showRegisterDialog,
      adding,
      userFormRef,
      newUser,
      userRules,
      handleAddUser,
      viewUser,
      toggleVerify
    }
  }
}
</script>

<style scoped>
.users-page {
  padding: 20px;
}

code {
  font-size: 12px;
}
</style>
