<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getAttachmentList, uploadAttachment, deleteAttachment, getAttachmentDownloadUrl } from '@/api/business'
import type { Attachment } from '@/api/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'

const props = defineProps<{
  relatedId: string
  relatedType: 'award' | 'punishment' | 'statusChange' | 'leaveSchool'
  readonly?: boolean
}>()

const emit = defineEmits<{
  (e: 'change', attachments: Attachment[]): void
}>()

const userStore = useUserStore()
const loading = ref(false)
const attachments = ref<Attachment[]>([])

// 获取附件列表
const fetchAttachments = async () => {
  if (!props.relatedId) return
  
  loading.value = true
  try {
    const res = await getAttachmentList(props.relatedId, props.relatedType)
    attachments.value = res.data || []
    emit('change', attachments.value)
  } catch (error) {
    console.error('获取附件列表失败', error)
  } finally {
    loading.value = false
  }
}

// 上传前检查
const beforeUpload = (file: File) => {
  const isValidType = file.type.startsWith('image/') || 
    file.type === 'application/pdf' ||
    file.type.startsWith('application/msword') ||
    file.type.startsWith('application/vnd.openxmlformats')
  
  if (!isValidType) {
    ElMessage.error('只支持图片、PDF和Word文档')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  return true
}

// 自定义上传
const handleUpload = async (options: { file: File }) => {
  const formData = new FormData()
  formData.append('file', options.file)
  formData.append('relatedId', props.relatedId)
  formData.append('relatedType', props.relatedType)
  formData.append('uploadUserId', userStore.userId)

  try {
    const res = await uploadAttachment(formData)
    if (res.code === 200) {
      ElMessage.success('上传成功')
      fetchAttachments()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  }
}

// 删除附件
const handleDelete = async (attachment: Attachment) => {
  try {
    await ElMessageBox.confirm('确定要删除该附件吗？', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAttachment(attachment.attachmentId)
    ElMessage.success('删除成功')
    fetchAttachments()
  } catch {
    // 用户取消
  }
}

// 下载附件
const handleDownload = (attachment: Attachment) => {
  window.open(getAttachmentDownloadUrl(attachment.attachmentId), '_blank')
}

// 格式化文件大小
const formatFileSize = (size: number) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

// 获取文件图标
const getFileIcon = (fileType: string) => {
  if (fileType.startsWith('image/')) return 'Picture'
  if (fileType === 'application/pdf') return 'Document'
  return 'Document'
}

// 监听relatedId变化
watch(() => props.relatedId, (newVal) => {
  if (newVal) {
    fetchAttachments()
  }
}, { immediate: true })

onMounted(() => {
  if (props.relatedId) {
    fetchAttachments()
  }
})

// 暴露刷新方法
defineExpose({
  refresh: fetchAttachments
})
</script>

<template>
  <div class="attachment-upload">
    <el-upload
      v-if="!readonly"
      class="upload-area"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="handleUpload"
      accept="image/*,.pdf,.doc,.docx"
      drag
    >
      <el-icon class="el-icon--upload"><Upload /></el-icon>
      <div class="el-upload__text">
        拖拽文件到此处或 <em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持图片、PDF、Word文档，单个文件不超过10MB
        </div>
      </template>
    </el-upload>

    <div class="attachment-list" v-loading="loading">
      <el-empty v-if="attachments.length === 0 && !loading" description="暂无附件" :image-size="60" />
      
      <div v-else class="attachment-item" v-for="item in attachments" :key="item.attachmentId">
        <div class="file-info">
          <el-icon class="file-icon" :size="24">
            <component :is="getFileIcon(item.fileType)" />
          </el-icon>
          <div class="file-detail">
            <span class="file-name" :title="item.fileName">{{ item.fileName }}</span>
            <span class="file-meta">{{ formatFileSize(item.fileSize) }}</span>
          </div>
        </div>
        <div class="file-actions">
          <el-button type="primary" size="small" link @click="handleDownload(item)">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
          <el-button v-if="!readonly" type="danger" size="small" link @click="handleDelete(item)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.attachment-upload {
  .upload-area {
    margin-bottom: 16px;
    
    :deep(.el-upload-dragger) {
      padding: 20px;
    }
  }
  
  .attachment-list {
    min-height: 60px;
  }
  
  .attachment-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    margin-bottom: 8px;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    .file-info {
      display: flex;
      align-items: center;
      flex: 1;
      min-width: 0;
      
      .file-icon {
        color: #409eff;
        margin-right: 12px;
        flex-shrink: 0;
      }
      
      .file-detail {
        display: flex;
        flex-direction: column;
        min-width: 0;
        
        .file-name {
          font-size: 14px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .file-meta {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
    
    .file-actions {
      flex-shrink: 0;
      margin-left: 16px;
    }
  }
}
</style>
