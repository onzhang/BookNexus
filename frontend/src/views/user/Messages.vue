<!--
  ============================================================
  Messages.vue — 用户端留言建议
  @description 用户提交留言/建议并查看历史留言记录的页面，
               包含留言表单和我的留言列表。
  @author 张俊文
  @date 2026-05-06
  ============================================================
-->
<template>
  <div class="messages-page">
    <!-- 提交留言表单 -->
    <el-card shadow="hover" class="form-card">
      <template #header>
        <span class="card-title">提交留言 / 建议</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="留言内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入您的留言或建议..."
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交留言
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的留言列表 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <span class="card-title">我的留言记录</span>
      </template>

      <!-- 加载骨架屏 -->
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="records.length === 0" class="empty-wrap">
        <el-empty description="暂无留言记录" />
      </div>

      <template v-else>
        <el-table
          :data="records"
          stripe
          border
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="content" label="留言内容" min-width="240" show-overflow-tooltip />
          <el-table-column prop="reply" label="管理员回复" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.reply" class="reply-text">{{ row.reply }}</span>
              <el-tag v-else type="info" size="small">待回复</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="留言时间" width="180" />
          <el-table-column prop="replyAt" label="回复时间" width="180">
            <template #default="{ row }">
              {{ row.replyAt || '-' }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @change="fetchRecords"
          />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { UserAPI } from '@/api/endpoints'
import type { Message, PageResult } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

/** 提交按钮加载状态 */
const submitting = ref(false)
/** 表格加载状态 */
const loading = ref(false)
/** 留言记录列表 */
const records = ref<Message[]>([])
/** 当前页码 */
const page = ref(1)
/** 每页大小 */
const size = ref(10)
/** 总记录数 */
const total = ref(0)

/** 表单引用 */
const formRef = ref<FormInstance>()

/** 留言表单数据 */
const form = reactive({ content: '' })

/** 表单校验规则 */
const rules: FormRules = {
  content: [{ required: true, message: '请输入留言内容', trigger: 'blur' }]
}

/** 获取留言记录列表（分页） */
async function fetchRecords() {
  loading.value = true
  try {
    const res = await api.get<PageResult<Message>>(UserAPI.MESSAGE_MY_PAGE.path, {
      params: { page: page.value, size: size.value }
    })
    const data = res.data.data
    records.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 提交留言 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await api.post(UserAPI.MESSAGE_CREATE.path, { content: form.content })
    ElMessage.success('留言提交成功')
    form.content = ''
    formRef.value?.resetFields()
    page.value = 1
    fetchRecords()
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

/** 重置表单 */
function resetForm() {
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped lang="scss">
.messages-page {
  max-width: 1200px;
  margin: 0 auto;

  .form-card {
    margin-bottom: 20px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
  }

  .loading-wrap {
    padding: 40px;
  }

  .empty-wrap {
    padding: 60px 0;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .reply-text {
    color: #67c23a;
  }
}
</style>
