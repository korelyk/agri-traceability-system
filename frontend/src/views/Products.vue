<template>
  <div class="products-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>产品列表</span>
          <el-button type="primary" @click="$router.push('/products/register')">
            <el-icon><Plus /></el-icon>
            注册新产品
          </el-button>
        </div>
      </template>

      <el-table :data="products" style="width: 100%" v-loading="loading">
        <el-table-column prop="productId" label="产品ID" width="220" />
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="productCategory" label="类别" width="120" />
        <el-table-column prop="producerName" label="生产者" />
        <el-table-column prop="origin" label="产地" />
        <el-table-column prop="currentStatus" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.currentStatus)">
              {{ getStatusText(scope.row.currentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
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

    <el-dialog v-model="qrDialogVisible" title="产品二维码" width="400px" center>
      <div class="qr-code-container">
        <qrcode-vue
          v-if="currentQRCodeUrl"
          :value="currentQRCodeUrl"
          :size="250"
          level="H"
          render-as="svg"
        />
        <p class="qr-hint">请扫描二维码查看公开溯源信息</p>
        <p class="qr-link">{{ currentQRCodeUrl }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import QrcodeVue from 'qrcode.vue'

const DEFAULT_PUBLIC_BASE_URL = process.env.VUE_APP_PUBLIC_BASE_URL || 'http://38.76.221.36:8088'

function buildPublicTraceUrl(productId) {
  const baseUrl = DEFAULT_PUBLIC_BASE_URL.replace(/\/+$/, '')
  return `${baseUrl}/public-trace/${productId}`
}

export default {
  name: 'Products',
  components: {
    Plus,
    QrcodeVue
  },
  setup() {
    const router = useRouter()
    const store = useStore()
    const products = ref([])
    const loading = ref(false)
    const qrDialogVisible = ref(false)
    const currentQRCodeUrl = ref('')

    const fetchProducts = async () => {
      loading.value = true
      const result = await store.dispatch('fetchProducts')
      loading.value = false

      if (result.success) {
        products.value = result.data || []
      } else {
        ElMessage.error(result.message || '获取产品失败')
      }
    }

    const getStatusType = (status) => {
      const types = {
        CREATED: 'info',
        PRODUCE: 'success',
        PROCESS: 'warning',
        TRANSPORT: 'primary',
        STORAGE: 'info',
        SALE: 'danger'
      }
      return types[status] || 'info'
    }

    const getStatusText = (status) => {
      const texts = {
        CREATED: '已创建',
        PRODUCE: '生产中',
        PROCESS: '加工中',
        TRANSPORT: '运输中',
        STORAGE: '仓储中',
        SALE: '销售中'
      }
      return texts[status] || status
    }

    const viewTrace = (productId) => {
      router.push(`/trace/detail/${productId}`)
    }

    const viewQRCode = (product) => {
      currentQRCodeUrl.value = buildPublicTraceUrl(product.productId)
      qrDialogVisible.value = true
    }

    onMounted(fetchProducts)

    return {
      products,
      loading,
      qrDialogVisible,
      currentQRCodeUrl,
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.qr-code-container {
  text-align: center;
  padding: 20px;
}

.qr-hint {
  margin-top: 15px;
  color: #909399;
}

.qr-link {
  margin-top: 12px;
  color: #606266;
  font-size: 12px;
  word-break: break-all;
}
</style>
