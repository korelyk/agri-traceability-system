<template>
  <div class="register-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>注册新产品</span>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        
        <el-form-item label="产品类别" prop="productCategory">
          <el-select v-model="form.productCategory" placeholder="请选择产品类别" style="width: 100%">
            <el-option label="水果" value="水果" />
            <el-option label="蔬菜" value="蔬菜" />
            <el-option label="粮食" value="粮食" />
            <el-option label="茶叶" value="茶叶" />
            <el-option label="肉类" value="肉类" />
            <el-option label="水产" value="水产" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="生产者" prop="producerId">
          <el-select v-model="form.producerId" placeholder="请选择生产者" style="width: 100%">
            <el-option label="山东果园有限公司" value="prod001" />
            <el-option label="五常农业合作社" value="prod002" />
            <el-option label="西湖茶叶公司" value="prod003" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="产地" prop="origin">
          <el-input v-model="form.origin" placeholder="请输入产地" />
        </el-form-item>
        
        <el-form-item label="产品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入产品描述"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            注册产品
          </el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'ProductRegister',
  setup() {
    const store = useStore()
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    
    const form = reactive({
      productName: '',
      productCategory: '',
      producerId: '',
      origin: '',
      description: ''
    })
    
    const rules = {
      productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
      productCategory: [{ required: true, message: '请选择产品类别', trigger: 'change' }],
      producerId: [{ required: true, message: '请选择生产者', trigger: 'change' }],
      origin: [{ required: true, message: '请输入产地', trigger: 'blur' }]
    }
    
    const handleSubmit = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return
      
      loading.value = true
      const result = await store.dispatch('registerProduct', form)
      loading.value = false
      
      if (result.success) {
        ElMessage.success('产品注册成功')
        router.push('/products')
      } else {
        ElMessage.error(result.message)
      }
    }
    
    return {
      form,
      formRef,
      rules,
      loading,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.register-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
</style>
