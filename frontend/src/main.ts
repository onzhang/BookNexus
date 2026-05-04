/**
 * BookNexus 图书管理系统 — Vue 应用入口文件
 *
 * @description 负责创建 Vue 应用实例，注册全局插件（Pinia、Vue Router、Element Plus）、全局注册 Element Plus 图标，并挂载到 DOM
 * @author 张俊文
 * @date 2026-05-01
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/index.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
