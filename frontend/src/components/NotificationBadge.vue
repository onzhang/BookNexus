<template>
  <div class="notification-badge-wrapper">
    <slot />
    <Transition name="badge-fade">
      <span
        v-if="displayCount > 0"
        :key="count"
        class="notification-badge"
        :class="{ 'badge-pulse': isPulsing }"
      >
        {{ displayText }}
      </span>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

interface Props {
  count?: number
  max?: number
}

const props = withDefaults(defineProps<Props>(), {
  count: 0,
  max: 99,
})

const isPulsing = ref(false)

const displayCount = computed(() => {
  if (props.count <= 0) return 0
  return Math.min(props.count, props.max)
})

const displayText = computed(() => {
  if (props.count > props.max) {
    return `${props.max}+`
  }
  return String(props.count)
})

watch(
  () => props.count,
  (newVal, oldVal) => {
    if (newVal > oldVal && newVal > 0) {
      isPulsing.value = true
      setTimeout(() => {
        isPulsing.value = false
      }, 600)
    }
  }
)
</script>

<style scoped lang="scss">
.notification-badge-wrapper {
  position: relative;
  display: inline-flex;
}

.notification-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--danger-color, #C0392B);
  color: var(--text-inverse, #FDFAF5);
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  border-radius: 9px;
  white-space: nowrap;
  box-shadow: 0 1px 4px rgba(192, 57, 43, 0.3);
}

.badge-pulse {
  animation: badge-pulse 0.6s ease-in-out;
}

@keyframes badge-pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}

.badge-fade-enter-active {
  animation: badge-pulse 0.6s ease-in-out;
}

.badge-fade-enter-from {
  opacity: 0;
  transform: scale(0.5);
}

.badge-fade-enter-to {
  opacity: 1;
  transform: scale(1);
}

.badge-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.badge-fade-leave-from {
  opacity: 1;
  transform: scale(1);
}

.badge-fade-leave-to {
  opacity: 0;
  transform: scale(0.5);
}
</style>
