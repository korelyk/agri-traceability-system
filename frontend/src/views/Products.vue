<template>
  <div class="products-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>产品列表</span>
          <el-button type="primary" @click="$router.push('/products/register')">
            <el-icon><Plus /></el-icon> 注册新产品
          </el-button>
        </div>
      </template>
      
      <el-table :data="products" style="width: 100%" v-loading="loading">
        <el-table-column prop="productId" label="产品ID" width="180" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="productCategory" label="类别" width="120" />
        <el-table-column prop="producerName" label="生产者" />
        <el-table-column prop="origin" label="产地" />
        <el-table-column prop="currentStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.currentStatus)">
              {{ getStatusText(scope.row.currentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="viewTrace(scope.row.productId)">
              溯源
            </el-button>
            <el-button link type="info" @click="viewQRCode(scope.row)">
              二维码
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 二维码弹窗 -->
    <el-dialog v-model="qrDialogVisible" title="产品溯源二维码" width="400px" center>
      <div class="qr-code-container">
        <img v-if="currentQRCode" :src="currentQRCode" alt="QR Code" style="width: 250px; height: 250px;" />
        <p class="qr-hint">扫描二维码查看溯源信息</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'Products',
  setup() {
    const router = useRouter()
    const products = ref([])
    const loading = ref(false)
    const qrDialogVisible = ref(false)
    const currentQRCode = ref('')
    
    const fetchProducts = async () => {
      loading.value = true
      // 模拟数据
      products.value = [
        {
          productId: 'AP202401010001',
          productName: '山东红富士苹果',
          productCategory: '水果',
          producerName: '山东果园有限公司',
          origin: '山东烟台',
          currentStatus: 'PRODUCE',
          qrCode: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=='
        },
        {
          productId: 'GR202401010002',
          productName: '五常大米',
          productCategory: '粮食',
          producerName: '五常农业合作社',
          origin: '黑龙江五常',
          currentStatus: 'SALE',
          qrCode: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=='
        },
        {
          productId: 'TE202401010003',
          productName: '西湖龙井',
          productCategory: '茶叶',
          producerName: '西湖茶叶公司',
          origin: '浙江杭州',
          currentStatus: 'PROCESS',
          qrCode: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=='
        }
      ]
      loading.value = false
    }
    
    const getStatusType = (status) => {
      const types = {
        'CREATED': 'info',
        'PRODUCE': 'success',
        'PROCESS': 'warning',
        'TRANSPORT': 'primary',
        'STORAGE': 'info',
        'SALE': 'danger'
      }
      return types[status] || 'info'
    }
    
    const getStatusText = (status) => {
      const texts = {
        'CREATED': '已创建',
        'PRODUCE': '生产中',
        'PROCESS': '加工中',
        'TRANSPORT': '运输中',
        'STORAGE': '仓储中',
        'SALE': '销售中'
      }
      return texts[status] || status
    }
    
    const viewTrace = (productId) => {
      router.push(`/trace/detail/${productId}`)
    }
    
    const viewQRCode = (product) => {
      currentQRCode.value = product.qrCode
      qrDialogVisible.value = true
    }
    
    onMounted(() => {
      fetchProducts()
    })
    
    return {
      products,
      loading,
      qrDialogVisible,
      currentQRCode,
      getStatusType,
      getStatusText,
      viewTrace,
      viewQRCode
    }
  }
}
</script>

<style scoped>
.products-page {
  padding: 20px;
}

.qr-code-container {
  text-align: center;
  padding: 20px;
}

.qr-hint {
  margin-top: 15px;
  color: #909399;
}
</style>
