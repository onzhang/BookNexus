<!--
  ============================================================
  Login.vue — 登录/注册页面
  @description 提供用户登录和注册功能，支持登录/注册模式切换。
               使用 Element Plus 表单验证，登录成功后根据角色自动跳转。
  @author 张俊文
  @date 2026-05-01
  ============================================================
-->
<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">BookNexus 图书管理系统</h1>
      <p class="login-subtitle">{{ isRegister ? '创建新账号' : '欢迎登录' }}</p>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @submit.prevent
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <!-- 注册模式下的邮箱输入 -->
        <el-form-item v-if="isRegister" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isRegister ? '注册' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>{{ isRegister ? '已有账号？' : '没有账号？' }}</span>
        <el-button link type="primary" @click="toggleMode">
          {{ isRegister ? '去登录' : '去注册' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

/** 表单引用 */
const formRef = ref<FormInstance>()
/** 提交按钮加载状态 */
const loading = ref(false)
/** 是否为注册模式 */
const isRegister = ref(false)

/** 登录/注册表单数据 */
const form = reactive({
  username: '',
  password: '',
  email: ''
})

/** 表单校验规则 */
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不少于6位', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

/** 切换登录/注册模式 */
function toggleMode() {
  isRegister.value = !isRegister.value
  formRef.value?.resetFields()
  form.email = ''
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    router.push(userStore.isAdmin ? '/admin/dashboard' : '/user/home')
  }
})

/**
 * 提交登录或注册
 * @description 先校验表单，再调用对应接口，成功后根据角色跳转
 */
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    if (isRegister.value) {
      const registerData: Record<string, string> = {
        username: form.username,
        password: form.password
      }
      if (form.email) {
        registerData.email = form.email
      }
      await userStore.register(registerData as any)
      ElMessage.success('注册成功')
    } else {
      await userStore.login({
        username: form.username,
        password: form.password
      })
      ElMessage.success('登录成功')
    }
    router.push(userStore.isAdmin ? '/admin/dashboard' : '/user/home')
  } catch {
    // Error already shown by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-title {
  text-align: center;
  font-size: 24px;
  color: var(--primary-color, #409EFF);
  margin-bottom: 8px;
}

.login-subtitle {
  text-align: center;
  color: var(--text-secondary, #909399);
  font-size: 14px;
  margin-bottom: 32px;
}

.login-form {
  .login-btn {
    width: 100%;
  }
}

.login-footer {
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
