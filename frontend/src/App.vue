<template>
  <router-view />
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
#app {
  min-height: 100vh;
}

.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-operations {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.text-success {
  color: #67C23A;
}

.text-warning {
  color: #E6A23C;
}

.text-danger {
  color: #F56C6C;
}

.text-info {
  color: #909399;
}
</style>
