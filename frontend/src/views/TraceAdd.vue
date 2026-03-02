<template>
  <div class="trace-add-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>添加溯源记录</span>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="产品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择产品" style="width: 100%">
            <el-option
              v-for="product in products"
              :key="product.productId"
              :label="product.productName"
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
        
        <el-form-item label="操作者" prop="operatorId">
          <el-select v-model="form.operatorId" placeholder="请选择操作者" style="width: 100%">
            <el-option label="张三 - 生产者" value="user001" />
            <el-option label="李四 - 加工商" value="user002" />
            <el-option label="王五 - 物流商" value="user003" />
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
        
        <el-form-item label="环境数据" prop="environmentData">
          <el-input
            v-model="form.environmentData"
            type="textarea"
            :rows="3"
            placeholder='{"temperature": 25, "humidity": 60}'
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            添加记录
          </el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'TraceAdd',
  setup() {
    const store = useStore()
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const products = ref([])
    
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
      operatorId: [{ required: true, message: '请选择操作者', trigger: 'change' }],
      location: [{ required: true, message: '请输入操作地点', trigger: 'blur' }],
      operationDetail: [{ required: true, message: '请输入操作详情', trigger: 'blur' }]
    }
    
    const fetchProducts = async () => {
      // 模拟获取产品列表
      products.value = [
        { productId: 'AP202401010001', productName: '山东红富士苹果' },
        { productId: 'GR202401010002', productName: '五常大米' },
        { productId: 'TE202401010003', productName: '西湖龙井' }
      ]
    }
    
    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return
      
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
    
    onMounted(() => {
      fetchProducts()
    })
    
    return {
      form,
      formRef,
      rules,
      loading,
      products,
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
</style>
