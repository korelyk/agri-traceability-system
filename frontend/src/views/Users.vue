<template>
  <div class="users-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button v-if="isAdmin" type="primary" @click="showRegisterDialog = true">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </div>
      </template>

      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column label="用户类型">
          <template #default="{ row }">
            {{ userTypeDisplay(row.userTypeName, row.userType) }}
          </template>
        </el-table-column>
        <el-table-column label="公司名称">
          <template #default="{ row }">
            {{ readableValue(row.companyName) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="blockchainAddress" label="区块链地址" width="220">
          <template #default="{ row }">
            <code>{{ shortAddress(row.blockchainAddress) }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="verified" label="认证状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.verified ? 'success' : 'warning'">
              {{ row.verified ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewUser(row)">
              查看
            </el-button>
            <el-button
              v-if="canEditUser(row)"
              link
              type="primary"
              @click="openEditDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canDeleteUser(row)"
              link
              type="danger"
              :loading="deletingUserId === row.userId"
              @click="handleDeleteUser(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showRegisterDialog" title="添加用户" width="520px">
      <el-form ref="userFormRef" :model="newUser" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="newUser.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="newUser.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="newUser.realName" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="newUser.userType" style="width: 100%">
            <el-option label="生产者" value="PRODUCER" />
            <el-option label="加工商" value="PROCESSOR" />
            <el-option label="物流商" value="LOGISTICS" />
            <el-option label="销售商" value="RETAILER" />
            <el-option label="检测机构" value="INSPECTOR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="newUser.companyName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="handleAddUser">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="用户详情" width="520px">
      <el-descriptions v-if="selectedUser" :column="1" border>
        <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ readableValue(selectedUser.realName) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">{{ userTypeDisplay(selectedUser.userTypeName, selectedUser.userType) }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ readableValue(selectedUser.companyName) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ readableValue(selectedUser.email) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ readableValue(selectedUser.phone) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">{{ selectedUser.verified ? '已认证' : '未认证' }}</el-descriptions-item>
        <el-descriptions-item label="启用状态">{{ selectedUser.active ? '已启用' : '已停用' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleLabel(selectedUser.role) }}</el-descriptions-item>
        <el-descriptions-item label="区块链地址">{{ selectedUser.blockchainAddress || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑用户" width="560px">
      <el-form ref="editFormRef" :model="editUser" :rules="editRules" label-width="100px">
        <el-form-item label="用户名">
          <el-input :model-value="editUser.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editUser.realName" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="editUser.userType" style="width: 100%">
            <el-option label="生产者" value="PRODUCER" />
            <el-option label="加工商" value="PROCESSOR" />
            <el-option label="物流商" value="LOGISTICS" />
            <el-option label="销售商" value="RETAILER" />
            <el-option label="检测机构" value="INSPECTOR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="公司名称">
          <el-input v-model="editUser.companyName" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editUser.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editUser.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editUser.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="业务操作员" value="OPERATOR" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证状态">
          <el-switch
            v-model="editUser.verified"
            inline-prompt
            active-text="已认证"
            inactive-text="未认证"
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch
            v-model="editUser.active"
            inline-prompt
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editing" @click="handleUpdateUser">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleLabel, userTypeDisplay } from '../utils/labels'

function readableValue(value) {
  return value && value.trim() && !/^\?+$/.test(value) ? value : ''
}

export default {
  name: 'Users',
  setup() {
    const store = useStore()
    const isAdmin = computed(() => store.getters.isAdmin)
    const users = ref([])
    const loading = ref(false)
    const showRegisterDialog = ref(false)
    const showDetailDialog = ref(false)
    const showEditDialog = ref(false)
    const adding = ref(false)
    const editing = ref(false)
    const deletingUserId = ref('')
    const userFormRef = ref(null)
    const editFormRef = ref(null)
    const selectedUser = ref(null)

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
      realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
      userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
      companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }]
    }

    const editUser = reactive({
      userId: '',
      username: '',
      realName: '',
      userType: '',
      companyName: '',
      email: '',
      phone: '',
      role: '',
      verified: false,
      active: true
    })

    const editRules = {
      realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
      userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
      role: [{ required: true, message: '请选择角色', trigger: 'change' }]
    }

    const resetForm = () => {
      newUser.username = ''
      newUser.password = ''
      newUser.realName = ''
      newUser.userType = ''
      newUser.companyName = ''
    }

    const fillEditForm = (user) => {
      editUser.userId = user.userId
      editUser.username = user.username
      editUser.realName = user.realName || ''
      editUser.userType = user.userType || ''
      editUser.companyName = user.companyName || ''
      editUser.email = user.email || ''
      editUser.phone = user.phone || ''
      editUser.role = user.role || 'OPERATOR'
      editUser.verified = !!user.verified
      editUser.active = user.active !== false
    }

    const fetchUsers = async () => {
      loading.value = true
      const result = await store.dispatch('fetchUsers')
      if (result.success) {
        users.value = result.data || []
      } else {
        ElMessage.error(result.message || '获取用户失败')
      }
      loading.value = false
    }

    const handleAddUser = async () => {
      const valid = await userFormRef.value.validate().catch(() => false)
      if (!valid) {
        return
      }

      adding.value = true
      const result = await store.dispatch('register', newUser)
      adding.value = false

      if (result.success) {
        ElMessage.success('用户添加成功')
        showRegisterDialog.value = false
        resetForm()
        fetchUsers()
      } else {
        ElMessage.error(result.message)
      }
    }

    const viewUser = (user) => {
      selectedUser.value = user
      showDetailDialog.value = true
    }

    const canEditUser = () => store.getters.isAdmin

    const openEditDialog = (user) => {
      fillEditForm(user)
      showEditDialog.value = true
    }

    const handleUpdateUser = async () => {
      const valid = await editFormRef.value.validate().catch(() => false)
      if (!valid) {
        return
      }

      editing.value = true
      const result = await store.dispatch('updateUser', {
        userId: editUser.userId,
        userData: {
          realName: editUser.realName,
          userType: editUser.userType,
          companyName: editUser.companyName,
          email: editUser.email,
          phone: editUser.phone,
          role: editUser.role,
          verified: editUser.verified,
          active: editUser.active
        }
      })
      editing.value = false

      if (result.success) {
        ElMessage.success(result.message || '用户更新成功')
        showEditDialog.value = false
        if (selectedUser.value?.userId === editUser.userId) {
          selectedUser.value = result.data
        }
        fetchUsers()
      } else {
        ElMessage.error(result.message)
      }
    }

    const canDeleteUser = (user) => {
      return store.getters.isAdmin && user?.userId && user.userId !== store.state.user?.userId
    }

    const handleDeleteUser = async (user) => {
      try {
        await ElMessageBox.confirm(
          `确认删除用户“${user.username}”吗？此操作不可撤销。`,
          '删除用户',
          {
            type: 'warning',
            confirmButtonText: '确认删除',
            cancelButtonText: '取消'
          }
        )
      } catch {
        return
      }

      deletingUserId.value = user.userId
      const result = await store.dispatch('deleteUser', user.userId)
      deletingUserId.value = ''

      if (result.success) {
        ElMessage.success(result.message || '用户删除成功')
        if (selectedUser.value?.userId === user.userId) {
          showDetailDialog.value = false
          selectedUser.value = null
        }
        fetchUsers()
      } else {
        ElMessage.error(result.message)
      }
    }

    const shortAddress = (value) => {
      if (!value) {
        return '-'
      }
      return value.length > 24 ? `${value.substring(0, 20)}...` : value
    }

    onMounted(fetchUsers)

    return {
      users,
      isAdmin,
      loading,
      showRegisterDialog,
      showDetailDialog,
      showEditDialog,
      adding,
      editing,
      deletingUserId,
      userFormRef,
      editFormRef,
      selectedUser,
      newUser,
      userRules,
      editUser,
      editRules,
      readableValue,
      roleLabel,
      shortAddress,
      userTypeDisplay,
      canEditUser,
      canDeleteUser,
      handleAddUser,
      handleDeleteUser,
      handleUpdateUser,
      openEditDialog,
      viewUser
    }
  }
}
</script>

<style scoped>
.users-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

code {
  font-size: 12px;
}
</style>
