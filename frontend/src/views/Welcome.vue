<!--
  ============================================================
  Welcome.vue — 墨韵书斋 · 欢迎页
  @description BookNexus 图书管理系统的首屏落地页，包含导航栏、
               Hero 区、特色功能卡片、数据统计和页脚。
               使用 Intersection Observer 实现滚动动画。
  @author 张俊文
  @date 2026-05-07
  ============================================================
-->
<template>
  <div class="welcome-page">
    <!-- ===== 导航栏 ===== -->
    <header
      ref="headerRef"
      class="welcome-header"
      :class="{ 'welcome-header--scrolled': isScrolled }"
    >
      <div class="welcome-header__inner">
        <router-link to="/" class="welcome-header__logo">
          <span class="welcome-header__logo-icon">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="6" y="8" width="22" height="26" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
              <rect x="12" y="6" width="22" height="26" rx="2" stroke="currentColor" stroke-width="2" fill="none"/>
              <line x1="18" y1="12" x2="28" y2="12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="18" y1="17" x2="28" y2="17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="18" y1="22" x2="24" y2="22" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </span>
          <span class="welcome-header__logo-text">BookNexus</span>
        </router-link>
        <nav class="welcome-header__nav">
          <router-link to="/login" class="welcome-header__btn welcome-header__btn--login">
            登录
          </router-link>
          <router-link to="/login" class="welcome-header__btn welcome-header__btn--register">
            注册
          </router-link>
        </nav>
      </div>
    </header>

    <!-- ===== Hero 区 ===== -->
    <section class="hero">
      <!-- 纸张纹理叠加 -->
      <div class="hero__grain"></div>
      <!-- 装饰性墨线 -->
      <div class="hero__ink-line"></div>
      <!-- 浮动文字粒子 -->
      <div class="hero__particles" aria-hidden="true">
        <span class="hero__particle" style="--delay: 0s; --x: 10%; --dur: 18s;">书</span>
        <span class="hero__particle" style="--delay: 3s; --x: 25%; --dur: 22s;">墨</span>
        <span class="hero__particle" style="--delay: 6s; --x: 45%; --dur: 20s;">韵</span>
        <span class="hero__particle" style="--delay: 2s; --x: 60%; --dur: 24s;">卷</span>
        <span class="hero__particle" style="--delay: 8s; --x: 75%; --dur: 19s;">藏</span>
        <span class="hero__particle" style="--delay: 4s; --x: 88%; --dur: 21s;">阅</span>
      </div>

      <div class="hero__content">
        <h1 class="hero__title">
          <span class="hero__title-en">BookNexus</span>
          <span class="hero__title-cn">墨韵书斋</span>
        </h1>
        <p class="hero__subtitle">万卷藏书 · 一隅清心</p>
        <p class="hero__tagline">在字里行间，遇见另一个世界</p>
        <router-link to="/login" class="hero__cta">
          <span>开始探索</span>
          <svg class="hero__cta-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </router-link>
      </div>

      <!-- 底部装饰分隔线 -->
      <div class="hero__divider">
        <span class="hero__divider-line"></span>
        <span class="hero__divider-dot"></span>
        <span class="hero__divider-line"></span>
      </div>
    </section>

    <!-- ===== 特色功能区 ===== -->
    <section class="features">
      <h2 class="features__heading">
        <span class="features__heading-line"></span>
        <span class="features__heading-text">馆藏特色</span>
        <span class="features__heading-line"></span>
      </h2>
      <div class="features__grid">
        <div
          v-for="(feature, index) in features"
          :key="feature.title"
          class="feature-card"
          :style="{ '--stagger': `${index * 0.12}s` }"
          ref="featureCards"
        >
          <div class="feature-card__index">{{ String(index + 1).padStart(2, '0') }}</div>
          <div class="feature-card__icon">{{ feature.icon }}</div>
          <h3 class="feature-card__title">{{ feature.title }}</h3>
          <p class="feature-card__desc">{{ feature.desc }}</p>
          <div class="feature-card__corner"></div>
        </div>
      </div>
    </section>

    <!-- ===== 数据统计区 ===== -->
    <section class="stats">
      <div class="stats__inner">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="stat-item"
          ref="statItems"
        >
          <div class="stat-item__number">
            <span class="stat-item__count">{{ stat.displayValue }}</span>
            <span class="stat-item__suffix">{{ stat.suffix }}</span>
          </div>
          <p class="stat-item__label">{{ stat.label }}</p>
        </div>
      </div>
      <div class="stats__deco" aria-hidden="true">
        <svg viewBox="0 0 120 40" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M0 20 Q30 0 60 20 Q90 40 120 20" stroke="var(--border-light)" stroke-width="1" fill="none"/>
        </svg>
      </div>
    </section>

    <!-- ===== 页脚 ===== -->
    <footer class="welcome-footer">
      <div class="welcome-footer__inner">
        <div class="welcome-footer__bookmark" aria-hidden="true">
          <svg viewBox="0 0 24 36" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M2 0h20v36L12 28 2 36V0z" fill="var(--primary-light)" opacity="0.3"/>
          </svg>
        </div>
        <p class="welcome-footer__text">
          &copy; {{ currentYear }} BookNexus 墨韵书斋 — 张俊文 制作
        </p>
        <p class="welcome-footer__sub">以书会友，以文载道</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

/** 当前年份 */
const currentYear = computed(() => new Date().getFullYear())

/** 导航栏滚动状态 */
const isScrolled = ref(false)
const headerRef = ref<HTMLElement | null>(null)

/** 特色功能数据 */
const features = [
  {
    icon: '\uD83D\uDCDA',
    title: '海量藏书',
    desc: '涵盖文学、历史、科技、艺术等多领域，馆藏丰富，满足你的每一分求知欲。'
  },
  {
    icon: '\uD83D\uDD0D',
    title: '智能检索',
    desc: '全文检索、分类筛选、作者查找，多种方式助你快速找到心仪之书。'
  },
  {
    icon: '\uD83D\uCD96',
    title: '轻松借阅',
    desc: '一键借阅、到期提醒、在线续借，让借阅流程如翻书般自然流畅。'
  },
  {
    icon: '\uD83D\uDCAC',
    title: '书友交流',
    desc: '留言建议、书评互动，与志同道合的书友分享阅读心得，碰撞思想火花。'
  }
]

/** 统计数据 */
const statsData = [
  { value: 12680, suffix: '', label: '馆藏图书（册）' },
  { value: 3560, suffix: '', label: '注册读者（人）' },
  { value: 28900, suffix: '', label: '累计借阅（次）' }
]

interface StatItem {
  value: number
  suffix: string
  label: string
  displayValue: string
}

const stats = ref<StatItem[]>(
  statsData.map(s => ({ ...s, displayValue: '0' }))
)

/** 滚动动画观察器 */
let scrollObserver: IntersectionObserver | null = null
let statsObserver: IntersectionObserver | null = null
let statsAnimated = false

/** 处理滚动事件 */
function handleScroll() {
  isScrolled.value = window.scrollY > 50
}

/** 数字递增动画 */
function animateNumber(el: HTMLElement, target: number, duration: number = 1800) {
  const start = performance.now()
  const format = (n: number) => n.toLocaleString('en-US')

  function tick(now: number) {
    const elapsed = now - start
    const progress = Math.min(elapsed / duration, 1)
    // easeOutExpo
    const eased = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress)
    const current = Math.floor(eased * target)
    el.textContent = format(current)
    if (progress < 1) {
      requestAnimationFrame(tick)
    } else {
      el.textContent = format(target)
    }
  }
  requestAnimationFrame(tick)
}

onMounted(() => {
  // 滚动监听
  window.addEventListener('scroll', handleScroll, { passive: true })

  // 特色卡片滚动动画
  scrollObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('feature-card--visible')
          scrollObserver?.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.15, rootMargin: '0px 0px -40px 0px' }
  )

  document.querySelectorAll('.feature-card').forEach(card => {
    scrollObserver?.observe(card)
  })

  // 统计数据滚动动画
  statsObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && !statsAnimated) {
          statsAnimated = true
          const counts = entry.target.querySelectorAll('.stat-item__count')
          statsData.forEach((s, i) => {
            if (counts[i]) {
              animateNumber(counts[i] as HTMLElement, s.value)
            }
          })
          statsObserver?.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.3 }
  )

  const statsInner = document.querySelector('.stats__inner')
  if (statsInner) {
    statsObserver.observe(statsInner)
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  scrollObserver?.disconnect()
  statsObserver?.disconnect()
})
</script>

<style scoped lang="scss">
/* ============================================================
   Welcome.vue — 墨韵书斋 · 欢迎页样式
   ============================================================ */

.welcome-page {
  min-height: 100vh;
  background: var(--bg-color);
  overflow-x: hidden;
}

/* ===== 导航栏 ===== */
.welcome-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: 0 var(--spacing-xl);
  transition: background var(--transition-normal), box-shadow var(--transition-normal), backdrop-filter var(--transition-normal);

  &--scrolled {
    background: rgba(253, 250, 245, 0.92);
    backdrop-filter: blur(12px);
    box-shadow: 0 1px 8px rgba(139, 111, 71, 0.08);
  }
}

.welcome-header__inner {
  max-width: 1200px;
  margin: 0 auto;
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome-header__logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--primary-dark);
  font-family: 'Playfair Display', serif;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.02em;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--accent-color);
  }
}

.welcome-header__logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 100%;
    height: 100%;
  }
}

.welcome-header__nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.welcome-header__btn {
  padding: 8px 24px;
  border-radius: var(--radius-md);
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.05em;
  transition: all var(--transition-fast);
  text-decoration: none;

  &--login {
    color: var(--primary-color);
    border: 1.5px solid var(--border-color);
    background: transparent;

    &:hover {
      border-color: var(--primary-color);
      background: var(--accent-bg);
    }
  }

  &--register {
    color: var(--text-inverse);
    background: var(--primary-color);
    border: 1.5px solid var(--primary-color);

    &:hover {
      background: var(--primary-dark);
      border-color: var(--primary-dark);
      box-shadow: 0 4px 16px rgba(139, 111, 71, 0.25);
    }
  }
}

/* ===== Hero 区 ===== */
.hero {
  position: relative;
  min-height: 88vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg-hero);
  overflow: hidden;
  padding: calc(var(--header-height) + var(--spacing-2xl)) var(--spacing-lg) var(--spacing-2xl);
}

.hero__grain {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 512 512' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.75' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
  opacity: 0.8;
  mix-blend-mode: multiply;
}

.hero__ink-line {
  position: absolute;
  top: 12%;
  right: 8%;
  width: 200px;
  height: 200px;
  opacity: 0.06;
  background: radial-gradient(ellipse at center, var(--primary-dark) 0%, transparent 70%);
  border-radius: 50%;
  filter: blur(40px);
  animation: inkFloat 8s ease-in-out infinite;
}

@keyframes inkFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-20px, 15px) scale(1.1); }
}

/* 浮动粒子 */
.hero__particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.hero__particle {
  position: absolute;
  bottom: -60px;
  left: var(--x);
  font-family: 'Noto Serif SC', serif;
  font-size: 28px;
  font-weight: 300;
  color: var(--primary-light);
  opacity: 0;
  animation: particleRise var(--dur) var(--delay) ease-in-out infinite;
}

@keyframes particleRise {
  0% {
    opacity: 0;
    transform: translateY(0) rotate(0deg);
  }
  15% {
    opacity: 0.18;
  }
  50% {
    opacity: 0.12;
  }
  85% {
    opacity: 0.06;
  }
  100% {
    opacity: 0;
    transform: translateY(-90vh) rotate(15deg);
  }
}

.hero__content {
  position: relative;
  z-index: 2;
  text-align: center;
  max-width: 720px;
}

.hero__title {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
  animation: heroFadeIn 1s ease-out both;
}

.hero__title-en {
  font-family: 'Playfair Display', serif;
  font-size: clamp(36px, 6vw, 64px);
  font-weight: 800;
  color: var(--primary-dark);
  letter-spacing: 0.04em;
  line-height: 1.1;
}

.hero__title-cn {
  font-family: 'Noto Serif SC', serif;
  font-size: clamp(20px, 3vw, 32px);
  font-weight: 300;
  color: var(--text-secondary);
  letter-spacing: 0.3em;
}

.hero__subtitle {
  font-family: 'Noto Serif SC', serif;
  font-size: clamp(16px, 2.2vw, 22px);
  font-weight: 400;
  color: var(--primary-light);
  letter-spacing: 0.15em;
  margin-bottom: var(--spacing-sm);
  animation: heroFadeIn 1s 0.2s ease-out both;
}

.hero__tagline {
  font-family: 'Lora', serif;
  font-size: clamp(14px, 1.8vw, 17px);
  font-style: italic;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-xl);
  animation: heroFadeIn 1s 0.4s ease-out both;
}

@keyframes heroFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero__cta {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 14px 40px;
  background: var(--primary-color);
  color: var(--text-inverse);
  font-family: 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 0.1em;
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all var(--transition-normal);
  animation: heroFadeIn 1s 0.6s ease-out both;
  box-shadow: 0 4px 20px rgba(139, 111, 71, 0.2);

  &:hover {
    background: var(--primary-dark);
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(139, 111, 71, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

.hero__cta-arrow {
  width: 20px;
  height: 20px;
  transition: transform var(--transition-fast);
}

.hero__cta:hover .hero__cta-arrow {
  transform: translateX(4px);
}

/* 底部分隔线 */
.hero__divider {
  position: absolute;
  bottom: var(--spacing-xl);
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  animation: heroFadeIn 1s 0.8s ease-out both;
}

.hero__divider-line {
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-color), transparent);
}

.hero__divider-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent-color);
  opacity: 0.6;
}

/* ===== 特色功能区 ===== */
.features {
  padding: var(--spacing-2xl) var(--spacing-lg) calc(var(--spacing-2xl) + 20px);
  max-width: 1200px;
  margin: 0 auto;
}

.features__heading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-2xl);
}

.features__heading-line {
  flex: 0 1 80px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-color));

  &:last-child {
    background: linear-gradient(90deg, var(--border-color), transparent);
  }
}

.features__heading-text {
  font-family: 'Noto Serif SC', serif;
  font-size: 24px;
  font-weight: 600;
  color: var(--primary-dark);
  letter-spacing: 0.15em;
  white-space: nowrap;
}

.features__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
}

/* 特色卡片 — 图书馆索引卡风格 */
.feature-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl) var(--spacing-lg) var(--spacing-lg);
  text-align: center;
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s var(--stagger) ease-out,
              transform 0.6s var(--stagger) ease-out,
              box-shadow var(--transition-normal),
              border-color var(--transition-normal);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, var(--primary-light), var(--accent-color), var(--primary-light));
    opacity: 0;
    transition: opacity var(--transition-normal);
  }

  &--visible {
    opacity: 1;
    transform: translateY(0);
  }

  &:hover {
    box-shadow: var(--shadow-lg);
    border-color: var(--accent-color);
    transform: translateY(-4px);

    &::before {
      opacity: 1;
    }
  }
}

.feature-card__index {
  position: absolute;
  top: var(--spacing-sm);
  right: var(--spacing-md);
  font-family: 'Playfair Display', serif;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-placeholder);
  letter-spacing: 0.05em;
}

.feature-card__icon {
  font-size: 36px;
  margin-bottom: var(--spacing-md);
  display: block;
  filter: grayscale(0.15);
}

.feature-card__title {
  font-family: 'Noto Serif SC', serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--primary-dark);
  margin-bottom: var(--spacing-sm);
  letter-spacing: 0.08em;
}

.feature-card__desc {
  font-family: 'Lora', 'Noto Serif SC', serif;
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--text-regular);
}

.feature-card__corner {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, transparent 50%, var(--border-light) 50%);
  border-radius: 0 0 var(--radius-md) 0;
  opacity: 0.5;
}

/* ===== 数据统计区 ===== */
.stats {
  position: relative;
  padding: var(--spacing-2xl) var(--spacing-lg);
  background: linear-gradient(180deg, var(--bg-color) 0%, var(--bg-card) 50%, var(--bg-color) 100%);
  text-align: center;
}

.stats__inner {
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-xl);
}

.stat-item {
  padding: var(--spacing-lg);
}

.stat-item__number {
  font-family: 'Playfair Display', serif;
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 700;
  color: var(--primary-dark);
  line-height: 1.2;
  margin-bottom: var(--spacing-sm);
}

.stat-item__suffix {
  font-size: 0.7em;
  color: var(--accent-color);
}

.stat-item__label {
  font-family: 'Noto Serif SC', serif;
  font-size: 14px;
  font-weight: 400;
  color: var(--text-secondary);
  letter-spacing: 0.08em;
}

.stats__deco {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: center;
  opacity: 0.5;

  svg {
    width: 120px;
    height: 40px;
  }
}

/* ===== 页脚 ===== */
.welcome-footer {
  padding: var(--spacing-xl) var(--spacing-lg);
  text-align: center;
  border-top: 1px solid var(--border-light);
  background: var(--bg-card);
}

.welcome-footer__inner {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
}

.welcome-footer__bookmark {
  display: flex;
  justify-content: center;
  margin-bottom: var(--spacing-md);

  svg {
    width: 18px;
    height: 28px;
  }
}

.welcome-footer__text {
  font-family: 'Noto Serif SC', serif;
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 0.05em;
  margin-bottom: var(--spacing-xs);
}

.welcome-footer__sub {
  font-family: 'Lora', serif;
  font-size: 12px;
  font-style: italic;
  color: var(--text-placeholder);
  letter-spacing: 0.1em;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .features__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-header {
    padding: 0 var(--spacing-md);
  }

  .welcome-header__logo-text {
    font-size: 18px;
  }

  .welcome-header__btn {
    padding: 6px 16px;
    font-size: 13px;
  }

  .hero {
    min-height: 80vh;
    padding: calc(var(--header-height) + var(--spacing-xl)) var(--spacing-md) var(--spacing-xl);
  }

  .hero__ink-line {
    width: 120px;
    height: 120px;
    top: 8%;
    right: 5%;
  }

  .hero__particle {
    font-size: 20px;
  }

  .features {
    padding: var(--spacing-xl) var(--spacing-md);
  }

  .features__heading-text {
    font-size: 20px;
  }

  .features__grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .stats__inner {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .stat-item {
    padding: var(--spacing-md);
  }
}

@media (max-width: 480px) {
  .welcome-header__nav {
    gap: var(--spacing-sm);
  }

  .welcome-header__btn--login {
    display: none;
  }

  .hero__title-en {
    font-size: 32px;
  }

  .hero__title-cn {
    font-size: 18px;
    letter-spacing: 0.2em;
  }

  .hero__cta {
    padding: 12px 32px;
    font-size: 14px;
  }
}
</style>
