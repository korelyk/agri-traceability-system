<template>
  <div class="trace-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>溯源查询</span>
        </div>
      </template>
      
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="请输入产品ID或扫描二维码"
          size="large"
          class="search-input"
        >
          <template #append>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> 查询
            </el-button>
          </template>
        </el-input>
      </div>
      
      <div class="search-hint">
        <p>提示: 输入产品ID或扫描产品包装上的二维码进行溯源查询</p>
      </div>
    </el-card>
    
    <!-- 搜索结果 -->
    <el-card v-if="searchResult" class="result-card">
      <template #header>
        <div class="card-header">
          <span>溯源结果</span>
          <el-tag :type="searchResult.dataConsistent ? 'success' : 'danger'">
            {{ searchResult.dataConsistent ? '数据一致' : '数据异常' }}
          </el-tag>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品ID">
          {{ searchResult.product?.productId }}
        </el-descriptions-item>
        <el-descriptions-item label="产品名称">
          {{ searchResult.product?.productName }}
        </el-descriptions-item>
        <el-descriptions-item label="产品类别">
          {{ searchResult.product?.productCategory }}
        </el-descriptions-item>
        <el-descriptions-item label="生产者">
          {{ searchResult.product?.producerName }}
        </el-descriptions-item>
        <el-descriptions-item label="产地">
          {{ searchResult.product?.origin }}
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag>{{ searchResult.product?.currentStatus }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <h3 class="timeline-title">溯源历史</h3>
      <el-timeline>
        <el-timeline-item
          v-for="(record, index) in searchResult.traceHistory"
          :key="index"
          :type="index === 0 ? 'primary' : ''"
          :timestamp="formatTime(record.operationTime)"
        >
          <el-card>
            <h4>{{ record.operationTypeName }}</h4>
            <p>操作者: {{ record.operatorName }}</p>
            <p>地点: {{ record.location }}</p>
            <p>详情: {{ record.operationDetail }}</p>
            <el-tag v-if="record.verified" type="success" size="small">已验证</el-tag>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'

export default {
  name: 'Trace',
  setup() {
    const store = useStore()
    const searchQuery = ref('')
    const searchResult = ref(null)
    
    const handleSearch = async () => {
      if (!searchQuery.value.trim()) {
        ElMessage.warning('请输入产品ID')
        return
      }
      
      const result = await store.dispatch('traceProduct', searchQuery.value.trim())
      
      if (result.success) {
        searchResult.value = result.data
        ElMessage.success('查询成功')
      } else {
        ElMessage.error(result.message)
        searchResult.value = null
      }
    }
    
    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    }
    
    return {
      searchQuery,
      searchResult,
      handleSearch,
      formatTime
    }
  }
}
</script>

<style scoped>
.trace-page {
  padding: 20px;
}

.search-box {
  max-width: 600px;
  margin: 0 auto;
}

.search-input {
  width: 100%;
}

.search-hint {
  text-align: center;
  margin-top: 20px;
  color: #909399;
}

.result-card {
  margin-top: 20px;
}

.timeline-title {
  margin: 30px 0 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}
</style>
