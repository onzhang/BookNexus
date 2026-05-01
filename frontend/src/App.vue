<!--
  ============================================================
  App.vue — 根组件
  @description 应用根容器，仅提供路由视图出口 <router-view />，所有页面
               通过 Vue Router 动态渲染。全局样式设定 #app 撑满视口。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
#app {
  width: 100%;
  height: 100vh;
}
</style>
