<template>
	<div v-if="showDetail" class="admin-ui-main">
		<div class="detail-container">
			<a-card class="detail-card" :bordered="false">
				<a-row class="xn-row">
					<a-col :span="18">
						<h3>录音详情</h3>
					</a-col>
					<a-col :span="6">
						<div class="xn-fdr">
							<a-button type="primary" danger ghost @click="handleClose">关闭</a-button>
						</div>
					</a-col>
				</a-row>
			</a-card>

			<a-row :gutter="10" justify="center">
				<!-- 左侧翻译结果 -->
				<a-col :span="12">
					<a-card title="对话内容" :bordered="false" class="content-card">
						<template #extra>
							<a-switch
								v-model:checked="enableAudioPlay"
								checked-children="开启播放"
								un-checked-children="关闭播放"
								style="margin-right: 10px"
							/>
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
										<div class="message-content" @click="handleDialogClick(dialog)">
											<div class="message-header">
												<div class="dialog-info">
													<span class="role">{{ dialog.role === 1 ? '客服' : '客户' }}</span>
												</div>
												<span class="time"
													  :class="{ 'customer-time': dialog.role === 0, 'agent-time': dialog.role === 1 }">
                                                        {{ formatTime(dialog.startTime) }}
                                                    </span>
											</div>
											<div class="message-text">{{ dialog.dialogText }}</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</a-card>
				</a-col>

				<!-- 右侧质检结果 -->
				<a-col :span="12">
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
										</div>
										<div class="result-section" v-if="inspectionResult.auditResults?.length">
											<h3>违规详情</h3>
											<a-collapse v-model:activeKey="activeViolationKeys">
												<a-collapse-panel
													v-for="(violation, index) in inspectionResult.auditResults[0].violations"
													:key="index"
												>
													<template #header>
                                                            <span class="violation-header">
                                                                <span class="violation-index">违规{{ index + 1 }}</span>
                                                                <span class="violation-rule">{{ violation.rule }}</span>
                                                            </span>
													</template>
													<a-card class="violation-card">
														<p><strong>描述：</strong>{{ violation.message }}</p>
														<p><strong>建议：</strong>{{ violation.suggestion }}</p>
														<div class="evidence-list"
															 v-if="violation.evidence?.length">
															<h4>违规证据：</h4>
															<div v-for="(item, eIndex) in violation.evidence"
																 :key="eIndex"
																 class="evidence-item"
																 @click="scrollToDialog(item.dialogId)">
																	<span class="dialog-badge">对话{{
																			item.dialogId
																		}}</span>
																<span class="evidence-content">{{
																		item.content
																	}}</span>
															</div>
														</div>
													</a-card>
												</a-collapse-panel>
											</a-collapse>
										</div>
									</template>
									<a-empty v-else description="暂无质检结果"/>
								</a-spin>
							</div>
						</div>
					</a-card>
				</a-col>
			</a-row>
		</div>
	</div>
	<audio ref="audioPlayer" :src="currentRecord.voiceUrl" preload="auto"></audio>
</template>

<script setup name="voiceRecordDetail">
import {ref, onMounted, nextTick} from 'vue'
import {message} from 'ant-design-vue'
import qualityApi from '@/api/inspection/qualityApi'
import translateApi from '@/api/inspection/translateApi'
import {formatDateTime} from '@/utils/dateUtil'

const emit = defineEmits(['close'])

const showDetail = ref(false)
const currentRecord = ref({})
const dialogList = ref([])
const inspectionResult = ref(null)
const loading = ref(false)
const dialogRefs = ref([])
const highlightedId = ref(null)
const audioPlayer = ref(null)
const enableAudioPlay = ref(false)
const activeViolationKeys = ref(['0'])

const formatTime = (milliseconds) => {
	if (!milliseconds) return '00:00'
	// 将毫秒转换为秒
	const totalSeconds = Math.floor(milliseconds / 1000)
	const minutes = Math.floor(totalSeconds / 60)
	const seconds = totalSeconds % 60
	return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

const openDetail = async (record) => {
	// 重置所有相关数据
	currentRecord.value = record
	dialogList.value = []
	inspectionResult.value = null
	highlightedId.value = null
	activeViolationKeys.value = ['0']

	showDetail.value = true
	await loadTranslationResult(record)
	if (record.isInspected) {
		await loadInspectionResult(record)
	}
}

const handleClose = () => {
	// 关闭时也清空数据
	showDetail.value = false
	currentRecord.value = {}
	dialogList.value = []
	inspectionResult.value = null
	highlightedId.value = null
	emit('close')
}

const scrollToDialog = (dialogId) => {
	// 先清除之前的高亮
	highlightedId.value = null

	// 设置新的高亮
	nextTick(() => {
		highlightedId.value = dialogId
		const element = dialogRefs.value[dialogId - 1]
		if (element) {
			element.scrollIntoView({behavior: 'smooth', block: 'center'})
			// 5秒后取消高亮
			setTimeout(() => {
				highlightedId.value = null
			}, 5000)
		}
	})
}

const loadTranslationResult = async (record) => {
	try {
		// 从后端获取对话内容
		const response = await translateApi.getDialogs({insuVoiceId: record.insuVoiceId})

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

// 直接使用毫秒级时间戳播放
const playDialogAudio = (startTime, endTime) => {
	const audio = audioPlayer.value
	if (!audio) return

	// 设置开始时间并播放
	audio.currentTime = startTime / 1000
	audio.play()

	// 监听播放进度
	const timeUpdateHandler = () => {
		const currentTimeMs = audio.currentTime * 1000
		if (currentTimeMs >= endTime) {
			audio.pause()
			audio.removeEventListener('timeupdate', timeUpdateHandler)
		}
	}

	audio.addEventListener('timeupdate', timeUpdateHandler)
}

// 点击对话处理函数
const handleDialogClick = (dialog) => {
	if (!enableAudioPlay.value) return

	if (dialog.startTime != null && dialog.endTime != null) {
		playDialogAudio(dialog.startTime, dialog.endTime)
	}
}

defineExpose({
	openDetail
})
</script>
<style scoped>
.detail-card {
	margin-bottom: 12px;
}

.xn-row {
	margin-bottom: -10px;
	margin-top: -10px;
}

.xn-fdr {
	display: flex;
	justify-content: flex-end;
}

/* 容器样式 */
.detail-container {
	position: relative;
	width: 100%;
	height: 100%;
	background: #fff;
	padding: 24px;
	overflow: hidden;
}

/* 内容卡片样式 */
.content-card {
	border-radius: 8px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
	height: calc(100vh - 218px);
	display: flex;
	flex-direction: column;
	overflow: hidden;
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
	overflow-y: scroll;
	padding: 16px 24px;
	background: #f8f9fa;
	border-radius: 12px;
	margin-bottom: 16px;
	border: 1px solid #e6e6e6;
	box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.02);
	margin-right: 0;
	box-sizing: border-box;
	width: calc(100% - 6px);
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
	align-items: center;
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
	margin-left: 8px;
}

.customer-time {
	margin-right: 4px;
}

.agent-time {
	margin-left: 4px;
}

/* 高亮样式 */
.highlighted .message-content {
	border: 2px solid #1890ff !important;
	background-color: #e6f7ff !important;
	transition: all 0.3s ease;
	box-shadow: 0 0 8px rgba(24, 144, 255, 0.3);
}

.highlighted.message.left .message-content {
	background-color: #e6f7ff !important;
}

.highlighted.message.right .message-content {
	background-color: #f0f9ff !important;
}

/* 动画样式 */
@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(10px);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

/* 违规卡片样式 */
.violation-card {
	margin-bottom: 16px;
	border: 1px solid #f0f0f0;
	padding: 0 8px;
	margin-right: 0;
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
	cursor: pointer;
	padding: 8px;
	margin: 8px 0;
	background: #f5f5f5;
	border-radius: 4px;
	transition: all 0.3s;
}

.evidence-item:hover {
	background: #e6f7ff;
}

.dialog-badge {
	background: #1890ff;
	color: white;
	padding: 2px 8px;
	border-radius: 4px;
	margin-right: 8px;
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

.message-content {
	cursor: pointer;
	transition: background-color 0.3s;
}

.message-content:hover {
	background-color: rgba(24, 144, 255, 0.1);
}

/* 违规标题样式 */
.violation-header {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	width: 100%;
}

.violation-index {
	font-size: 15px;
	font-weight: 600;
	color: #ff4d4f;
	flex-shrink: 0;
}

.violation-rule {
	font-size: 14px;
	color: #333;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	max-width: calc(100% - 80px);
}

.detail-wrapper {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	z-index: 1000;
}

.detail-mask {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: rgba(0, 0, 0, 0.45);
}

/* 结果区域样式 */
.result-section {
	padding-right: 0;
}

/* Collapse 面板样式调整 */
:deep(.ant-collapse-content-box) {
	padding: 16px 8px !important;
	overflow: visible !important;
}
</style>

