<template>
  <div class="trace-add-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>添加溯源记录</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择产品" style="width: 100%" filterable>
            <el-option
              v-for="product in products"
              :key="product.productId"
              :label="`${product.productName} (${product.productId})`"
              :value="product.productId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="操作类型" prop="operationType">
          <el-select v-model="form.operationType" placeholder="请选择操作类型" style="width: 100%">
            <el-option label="生产" value="PRODUCE" />
            <el-option label="加工" value="PROCESS" />
            <el-option label="运输" value="TRANSPORT" />
            <el-option label="仓储" value="STORAGE" />
            <el-option label="销售" value="SALE" />
            <el-option label="检测" value="INSPECT" />
            <el-option label="包装" value="PACKAGE" />
          </el-select>
        </el-form-item>

        <el-form-item label="操作人" prop="operatorId">
          <el-select v-model="form.operatorId" placeholder="请选择操作人" style="width: 100%" filterable>
            <el-option
              v-for="user in operators"
              :key="user.userId"
              :label="operatorLabel(user)"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="操作地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入操作地点" />
        </el-form-item>

        <el-form-item label="操作详情" prop="operationDetail">
          <el-input
            v-model="form.operationDetail"
            type="textarea"
            :rows="4"
            placeholder="请输入操作详情"
          />
        </el-form-item>

        <el-form-item label="环境数据">
          <el-input
            v-model="form.environmentData"
            type="textarea"
            :rows="3"
            placeholder='例如：{"temperature":25,"humidity":60}'
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">添加记录</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

function readableValue(value) {
  return value && value.trim() && !/^\?+$/.test(value) ? value : ''
}

export default {
  name: 'TraceAdd',
  setup() {
    const store = useStore()
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const products = ref([])
    const operators = ref([])

    const form = reactive({
      productId: '',
      operationType: '',
      operatorId: '',
      location: '',
      operationDetail: '',
      environmentData: ''
    })

    const rules = {
      productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
      operationType: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
      operatorId: [{ required: true, message: '请选择操作人', trigger: 'change' }],
      location: [{ required: true, message: '请输入操作地点', trigger: 'blur' }],
      operationDetail: [{ required: true, message: '请输入操作详情', trigger: 'blur' }]
    }

    const operatorLabel = (user) => {
      const name = readableValue(user.realName) || user.username
      const type = readableValue(user.userTypeName) || user.userType || ''
      return type ? `${name} - ${type}` : name
    }

    const fetchPageData = async () => {
      const [productResult, userResult] = await Promise.all([
        store.dispatch('fetchProducts'),
        store.dispatch('fetchUsers')
      ])

      if (productResult.success) {
        products.value = productResult.data || []
      } else {
        ElMessage.error(productResult.message || '获取产品失败')
      }

      if (userResult.success) {
        operators.value = userResult.data || []
      } else {
        ElMessage.error(userResult.message || '获取操作人失败')
      }
    }

    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) {
        return
      }

      loading.value = true
      const result = await store.dispatch('addTraceRecord', form)
      loading.value = false

      if (result.success) {
        ElMessage.success('溯源记录添加成功')
        router.push('/trace')
      } else {
        ElMessage.error(result.message)
      }
    }

    onMounted(fetchPageData)

    return {
      form,
      formRef,
      rules,
      loading,
      products,
      operators,
      operatorLabel,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.trace-add-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
