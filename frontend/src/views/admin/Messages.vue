<!--
  ============================================================
  Messages.vue — 管理端留言管理
  @description 管理员查看所有用户留言并进行回复的页面，
               包含分页列表、回复对话框功能。
  @author 张俊文
  @date 2026-05-06
  ============================================================
-->
<template>
  <div class="messages-page">
    <el-card shadow="hover">
      <template #header>
        <span class="card-title">留言管理</span>
      </template>

      <!-- 留言列表表格 -->
      <el-table :data="list" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="content" label="留言内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="reply" label="回复内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.reply">{{ row.reply }}</span>
            <el-tag v-else type="info" size="small">未回复</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="留言时间" width="180" />
        <el-table-column prop="replyAt" label="回复时间" width="180">
          <template #default="{ row }">
            {{ row.replyAt || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openReplyDialog(row)">
              {{ row.reply ? '修改回复' : '回复' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无留言" />

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchList"
        />
      </div>
    </el-card>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="回复留言"
      width="500px"
      @closed="resetForm"
    >
      <div class="reply-preview">
        <strong>用户留言：</strong>
        <p>{{ currentRow?.content }}</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="回复内容" prop="reply">
          <el-input v-model="form.reply" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'
import { AdminAPI } from '@/api/endpoints'
import type { Message, MessageReplyForm } from '@/types'
import type { FormInstance, FormRules } from 'element-plus'

/** 表格加载状态 */
const loading = ref(false)
/** 提交按钮加载状态 */
const submitting = ref(false)
/** 留言列表数据 */
const list = ref<Message[]>([])
/** 当前页码 */
const page = ref(1)
/** 每页大小 */
const size = ref(10)
/** 总记录数 */
const total = ref(0)

/** 对话框是否可见 */
const dialogVisible = ref(false)
/** 当前选中的留言行 */
const currentRow = ref<Message | null>(null)
/** 表单引用 */
const formRef = ref<FormInstance>()

/** 回复表单数据 */
const form = reactive<MessageReplyForm>({ reply: '' })

/** 表单校验规则 */
const rules: FormRules = {
  reply: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

/** 获取留言列表（分页） */
async function fetchList() {
  loading.value = true
  try {
    const res = await api.get(AdminAPI.MESSAGE_PAGE.path, {
      params: { page: page.value, size: size.value }
    })
    const data = res.data.data
    list.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/** 打开回复对话框 */
function openReplyDialog(row: Message) {
  currentRow.value = row
  form.reply = row.reply || ''
  dialogVisible.value = true
}

/** 关闭对话框时重置表单 */
function resetForm() {
  formRef.value?.resetFields()
  currentRow.value = null
}

/** 提交回复 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!currentRow.value) return

  submitting.value = true
  try {
    await api.put(AdminAPI.MESSAGE_REPLY(currentRow.value.id).path, form)
    ElMessage.success('回复成功')
    dialogVisible.value = false
    fetchList()
  } catch {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.messages-page {
  .card-title {
    font-size: 16px;
    font-weight: 600;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .reply-preview {
    background: var(--bg-color);
    padding: 12px;
    border-radius: var(--radius-sm);
    margin-bottom: 16px;

    p {
      margin: 8px 0 0;
      color: var(--text-regular);
      line-height: 1.6;
    }
  }
}
</style>
