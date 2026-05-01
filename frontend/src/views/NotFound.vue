<template>
  <div class="not-found">
    <div class="not-found-content">
      <h1>404</h1>
      <p>页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg-color, #f5f7fa);
}

.not-found-content {
  text-align: center;

  h1 {
    font-size: 96px;
    color: var(--primary-color, #409EFF);
    margin: 0 0 16px;
  }

  p {
    font-size: 18px;
    color: var(--text-regular, #606266);
    margin-bottom: 24px;
  }
}
</style>
