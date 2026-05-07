<!--
  ============================================================
  Login.vue — 登录/注册页面 · 墨韵书斋
  @description 提供用户登录和注册功能，支持登录/注册模式切换。
               适配墨韵书斋设计风格，使用焦茶棕主色系 + 宣纸质
               感背景 + 文艺衬线字体。登录成功后根据角色自动跳转。
  @author 张俊文
  @date 2026-05-07
  ============================================================
-->
<template>
  <div class="login-container">
    <!-- 背景装饰：水墨光斑 -->
    <div class="bg-orbs">
      <span class="orb orb-1" />
      <span class="orb orb-2" />
      <span class="orb orb-3" />
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 返回首页链接 -->
      <router-link to="/welcome" class="back-link">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </router-link>

      <!-- 标题区 -->
      <div class="card-header">
        <div class="logo-mark">
          <span>书</span>
        </div>
        <h1 class="login-title">BookNexus</h1>
        <p class="login-subtitle">{{ isRegister ? '创建新账号，开启阅读之旅' : '翻开书页，继续您的阅读' }}</p>
      </div>

      <!-- 表单 -->
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
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item v-if="isRegister" prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱（选填）"
            :prefix-icon="Message"
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
            {{ isRegister ? '注 册' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 切换模式 -->
      <div class="login-footer">
        <span>{{ isRegister ? '已有账号？' : '还没有账号？' }}</span>
        <el-button link type="primary" @click="toggleMode">
          {{ isRegister ? '立即登录' : '立即注册' }}
        </el-button>
      </div>
    </div>

    <!-- 底部署名 -->
    <p class="footer-signature">BookNexus · 墨韵书斋</p>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User, Lock, Message } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isRegister = ref(false)

const form = reactive({
  username: '',
  password: '',
  email: ''
})

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
      ElMessage.success('注册成功，欢迎加入墨韵书斋')
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
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--bg-hero);
  overflow: hidden;
}

/* ── 背景水墨光斑装饰 ── */
.bg-orbs {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);

    &-1 {
      width: 420px;
      height: 420px;
      top: -120px;
      right: -80px;
      background: radial-gradient(
        circle,
        rgba(196, 149, 106, 0.25) 0%,
        rgba(196, 149, 106, 0) 70%
      );
    }

    &-2 {
      width: 300px;
      height: 300px;
      bottom: -60px;
      left: -60px;
      background: radial-gradient(
        circle,
        rgba(139, 111, 71, 0.2) 0%,
        rgba(139, 111, 71, 0) 70%
      );
    }

    &-3 {
      width: 200px;
      height: 200px;
      top: 40%;
      left: 20%;
      background: radial-gradient(
        circle,
        rgba(212, 167, 106, 0.15) 0%,
        rgba(212, 167, 106, 0) 70%
      );
    }
  }
}

/* ── 登录卡片 ── */
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  max-width: 92vw;
  padding: 44px 40px 36px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg), 0 0 0 1px var(--border-light);
  // 微纸张纹理
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: inherit;
    pointer-events: none;
    background: repeating-linear-gradient(
      0deg,
      transparent,
      transparent 8px,
      rgba(139, 111, 71, 0.015) 8px,
      rgba(139, 111, 71, 0.015) 9px
    );
  }
}

.back-link {
  position: absolute;
  top: 20px;
  left: 24px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  transition: color var(--transition-fast);

  &:hover {
    color: var(--primary-color);
  }
}

/* ── 标题区 ── */
.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 14px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--primary-dark), var(--accent-color));
  box-shadow: 0 4px 14px rgba(139, 111, 71, 0.3);

  span {
    font-family: 'Noto Serif SC', serif;
    font-size: 26px;
    font-weight: 700;
    color: #fff;
    line-height: 1;
    margin-top: -2px;
  }
}

.login-title {
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  font-size: 26px;
  font-weight: 700;
  color: var(--primary-dark);
  letter-spacing: 2px;
  margin-bottom: 6px;
}

.login-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  font-family: 'Noto Serif SC', serif;
}

/* ── 表单 ── */
.login-form {
  :deep(.el-input__wrapper) {
    background: var(--bg-white);
    border-radius: var(--radius-sm);
    box-shadow: 0 0 0 1px var(--border-color) inset;
    transition: box-shadow var(--transition-fast);

    &:hover {
      box-shadow: 0 0 0 1px var(--border-color) inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px var(--primary-color) inset, 0 0 0 3px rgba(139, 111, 71, 0.1);
    }
  }

  :deep(.el-input__inner) {
    font-family: 'Noto Serif SC', 'Lora', serif;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 4px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--primary-dark), var(--primary-color));
  border: none;
  box-shadow: 0 4px 14px rgba(139, 111, 71, 0.25);
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast);

  &:hover:not(.is-loading) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(139, 111, 71, 0.35);
  }

  &:active:not(.is-loading) {
    transform: translateY(0);
  }
}

/* ── 底部切换 ── */
.login-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  font-family: 'Noto Serif SC', serif;

  :deep(.el-button.is-link) {
    color: var(--primary-color);

    &:hover {
      color: var(--primary-dark);
    }
  }
}

/* ── 底部署名 ── */
.footer-signature {
  position: absolute;
  bottom: 32px;
  font-size: 12px;
  color: var(--text-placeholder);
  font-family: 'Noto Serif SC', serif;
  letter-spacing: 2px;
  z-index: 1;
}
</style>
