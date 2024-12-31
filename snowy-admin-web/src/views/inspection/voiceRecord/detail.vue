<template>
    <div v-show="show" class="detail-container">
        <a-card :bordered="false" class="main-card">
            <template #extra>
                <a-button type="link" @click="closeDetail" class="close-button">关闭</a-button>
            </template>
            <a-row :gutter="16" justify="center">

                <!-- 左侧翻译结果 -->
                <a-col :span="11">
                    <a-card title="对话内容" :bordered="false" class="content-card">
                        <template #extra>
                            <a-button
                                type="primary"
                                @click="handleInspect"
                                :disabled="currentRecord.isInspected === 1"
                            >
                                质检
                            </a-button>
                        </template>
                        <div class="translation-content">
                            <a-descriptions bordered class="info-section">
                                <a-descriptions-item label="录音ID">
                                    {{ currentRecord.insuVoiceId }}
                                </a-descriptions-item>
                                <a-descriptions-item label="翻译时间">
                                    {{ currentRecord.translateTime }}
                                </a-descriptions-item>
                            </a-descriptions>
                            <div class="scroll-container">
                                <div class="chat-container" v-if="dialogList.length">
                                    <div v-for="(dialog, index) in dialogList"
                                         :key="index"
                                         :ref="el => dialogRefs[index] = el"
                                         :class="['message', dialog.role === 1 ? 'right' : 'left', { 'highlighted': highlightedId === index + 1 }]"
                                         :id="`dialog-${index + 1}`">
                                        <div class="message-content">
                                            <div class="message-header">
                                                <div class="dialog-info">
                                                    <span class="dialog-number">{{ index + 1 }}</span>
                                                    <span class="role">{{ dialog.role === 1 ? '客服' : '客户' }}</span>
                                                </div>
                                            </div>
                                            <div class="message-text">{{ dialog.dialogText }}</div>
                                        </div>
                                        <div class="time-container">
                                            <span class="time" :class="{ 'customer-time': dialog.role === 0, 'agent-time': dialog.role === 1 }">
                                                {{ formatTime(dialog.startTime) }}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </a-card>
                </a-col>

                <!-- 右侧质检结果 -->
                <a-col :span="11">
                    <a-card title="质检结果" :bordered="false" class="content-card">
                        <template #extra>
                            <a-tag :color="currentRecord.isInspected ? 'success' : 'warning'">
                                {{ currentRecord.isInspected ? '已质检' : '未质检' }}
                            </a-tag>
                        </template>
                        <div class="inspection-content">
                            <div class="scroll-container">
                                <a-spin :spinning="loading">
                                    <template v-if="inspectionResult">
                                        <div class="result-section">
                                            <h3>质检总结</h3>
                                            <p>{{ inspectionResult.inspectionSummary }}</p>
                                            <p class="score">总分：{{ inspectionResult.overallScore }}</p>
                                        </div>
                                        <div class="result-section" v-if="inspectionResult.auditResults?.length">
                                            <h3>违规详情</h3>
                                            <div v-for="(audit, index) in inspectionResult.auditResults" :key="index">
                                                <div v-for="(violation, vIndex) in audit.violations" :key="vIndex">
                                                    <a-card class="violation-card">
                                                        <p><strong>规则：</strong>{{ violation.rule }}</p>
                                                        <p><strong>描述：</strong>{{ violation.message }}</p>
                                                        <p><strong>建议：</strong>{{ violation.suggestion }}</p>
                                                        <div class="evidence-list" v-if="violation.evidence?.length">
                                                            <h4>违规证据：</h4>
                                                            <div v-for="(item, eIndex) in violation.evidence"
                                                                 :key="eIndex"
                                                                 class="evidence-item"
                                                                 @click="scrollToDialog(item.dialogId)">
                                                                <span class="dialog-badge">{{ item.dialogId }}</span>
                                                                <span class="evidence-content">{{ item.content }}</span>
                                                            </div>
                                                        </div>
                                                    </a-card>
                                                </div>
                                            </div>
                                        </div>
                                    </template>
                                    <a-empty v-else description="暂无质检结果" />
                                </a-spin>
                            </div>
                        </div>
                    </a-card>
                </a-col>
            </a-row>
        </a-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import qualityApi from '@/api/inspection/qualityApi'
import translateApi from '@/api/inspection/translateApi'

const show = ref(false)
const currentRecord = ref({})
const dialogList = ref([])
const inspectionResult = ref(null)
const loading = ref(false)
const dialogRefs = ref([])
const highlightedId = ref(null)

const formatTime = (seconds) => {
    if (!seconds) return '00:00'
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = Math.floor(seconds % 60)
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const openDetail = async (record) => {
    currentRecord.value = record
    show.value = true
    await loadTranslationResult(record)
    if (record.isInspected) {
        await loadInspectionResult(record)
    }
}

const closeDetail = () => {
    show.value = false
    currentRecord.value = {}
    dialogList.value = []
    inspectionResult.value = null
}

const scrollToDialog = (dialogId) => {
    highlightedId.value = dialogId
    const element = dialogRefs.value[dialogId - 1]
    if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' })
        // 3秒后取消高亮
        setTimeout(() => {
            highlightedId.value = null
        }, 3000)
    }
}

const loadTranslationResult = async (record) => {
    try {
        // 从后端获取对话内容
        const response = await translateApi.getDialogs({ insuVoiceId: record.insuVoiceId })

            dialogList.value = response.map(dialog => ({
                ...dialog,
                dialogId: parseInt(dialog.dialogTextId)
            }))

    } catch (error) {
        message.error('获取对话内容失败')
    }
}

const handleInspect = async () => {
    try {
        loading.value = true
        await qualityApi.submitInspection({
            insuVoiceId: currentRecord.value.insuVoiceId
        })
        message.success('质检成功')
        currentRecord.value.isInspected = 1
        await loadInspectionResult(currentRecord.value)
    } catch (error) {
        message.error('质检失败：' + error.message)
    } finally {
        loading.value = false
    }
}

const loadInspectionResult = async (record) => {
    try {
        loading.value = true
        const data = await qualityApi.getInspectionResult(record.insuVoiceId)
        inspectionResult.value = JSON.parse(data)
    } catch (error) {
        message.error('获取质检结果失败')
    } finally {
        loading.value = false
    }
}

defineExpose({
    openDetail,
    closeDetail
})
</script>

<style scoped>
/* 容器样式 */
.detail-container {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: #f0f2f5;
    z-index: 100;
    padding: 24px;
    overflow: hidden;
}

.main-card {
    height: 100%;
    max-width: 1600px;
    margin: 0 auto;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* 内容卡片样式 */
.content-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    height: calc(100vh - 150px);
    display: flex;
    flex-direction: column;
}

/* 翻译和质检内容样式 */
.translation-content, .inspection-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.info-section {
    flex-shrink: 0;
    margin-bottom: 16px;
}

/* 滚动容器样式 */
.scroll-container {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 12px;
    box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.02);
    border: 1px solid #d9d9d9;
    margin-bottom: 16px;
}

/* 滚动条样式 */
.scroll-container::-webkit-scrollbar {
    width: 6px;
}

.scroll-container::-webkit-scrollbar-thumb {
    background-color: #d9d9d9;
    border-radius: 3px;
}

.scroll-container::-webkit-scrollbar-track {
    background-color: transparent;
}

/* 调整卡片内容区域的内边距 */
:deep(.ant-card-body) {
    padding: 16px;
    height: calc(100% - 57px);
    display: flex;
    flex-direction: column;
}

.message.left .message-content {
    background: white;
    margin-right: auto;
    border-top-left-radius: 2px;
    border: 1px solid #e8e8e8;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.message.right .message-content {
    background: #e6f7ff;
    margin-left: auto;
    border-top-right-radius: 2px;
    border: 1px solid #91d5ff;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

:deep(.ant-descriptions) {
    background: #fafafa;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 16px;
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
}

:deep(.ant-descriptions-item-label) {
    font-weight: 500;
    color: #666;
}

:deep(.ant-descriptions-item-content) {
    color: #1890ff;
    font-weight: 500;
}

.chat-container {
    background: #f8f9fa;
    border-radius: 12px;
    box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.02);
}

.dialog-number,
.dialog-badge {
    display: inline-block;
    width: 24px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    background: #e6f7ff;
    border-radius: 50%;
    margin-right: 8px;
    font-size: 12px;
    color: #1890ff;
    font-weight: bold;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.message.right .dialog-number {
    background: #e6f7ff;
    color: #1890ff;
    border: 1px solid #91d5ff;
}

.message {
    display: flex;
    margin-bottom: 20px;
    animation: fadeIn 0.3s ease;
}

.message.left {
    justify-content: flex-start;
}

.message.right {
    justify-content: flex-end;
}

.message-content {
    max-width: 70%;
    padding: 12px;
    border-radius: 12px;
    position: relative;
}

.message.left .message-content {
    background: white;
    margin-right: auto;
    border-top-left-radius: 2px;
    border: 1px solid #e8e8e8;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.message.right .message-content {
    background: #e6f7ff;
    margin-left: auto;
    border-top-right-radius: 2px;
    border: 1px solid #91d5ff;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

/* 消息头部样式 */
.message-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 6px;
    font-size: 12px;
}

.role {
    font-weight: bold;
}

.message.left .role {
    color: #666;
}

.message.right .role {
    color: #1890ff;
}

/* 时间样式 */
.time {
    font-size: 12px;
    color: #999;
    display: block;
    margin-top: 4px;
}

.customer-time {
    text-align: right;
}

.agent-time {
    text-align: left;
}

/* 标号样式 */
.dialog-number,
.dialog-badge {
    display: inline-block;
    width: 24px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    background: #e6f7ff;
    border-radius: 50%;
    margin-right: 8px;
    font-size: 12px;
    color: #1890ff;
    font-weight: bold;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.message.right .dialog-number {
    background: #e6f7ff;
    color: #1890ff;
    border: 1px solid #91d5ff;
}

/* 高亮样式 */
.highlighted .message-content {
    border: 2px solid #1890ff;
}

/* 动画样式 */
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

/* 违规卡片样式 */
.violation-card {
    margin-bottom: 16px;
    border: 1px solid #d9d9d9;
    border-radius: 8px;
    padding: 12px;
    background: #f9f9f9;
}

/* 证据列表样式 */
.evidence-list {
    margin: 8px 0;
    padding: 8px;
    background: #f5f5f5;
    border-radius: 4px;
}

/* 证据项样式 */
.evidence-item {
    display: flex;
    align-items: flex-start;
    padding: 8px;
    margin-bottom: 8px;
    background: #fafafa;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;
}

.evidence-item:hover {
    background: #f0f7ff;
}

.evidence-content {
    flex: 1;
    line-height: 1.6;
}

/* 分数样式 */
.score {
    font-size: 18px;
    font-weight: bold;
    color: #52c41a;
}

/* 关闭按钮样式 */
.close-button {
    color: #1890ff;
    font-weight: bold;
    transition: color 0.3s;
}

.close-button:hover {
    color: #40a9ff;
}

/* 时间容器样式 */
.time-container {
    display: flex;
    justify-content: space-between;
    margin-top: 4px;
}
</style>
