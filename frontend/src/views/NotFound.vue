<!--
  ============================================================
  NotFound.vue — 404 页面
  @description 匹配所有未定义路由时展示的 404 页面，
               提供返回首页按钮引导用户回到正常页面。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
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

/** 跳转到首页（自动根据登录状态重定向） */
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
  background: var(--bg-color);
}

.not-found-content {
  text-align: center;

  h1 {
    font-size: 96px;
    color: var(--primary-color);
    margin: 0 0 16px;
  }

  p {
    font-size: 18px;
    color: var(--text-regular);
    margin-bottom: 24px;
  }
}
</style>
