<template>
	<a-card :bordered="false">
		<a-row :gutter="16">
			<!-- 左侧列表 -->
			<a-col :span="8">
				<s-table
					ref="tableRef"
					:columns="columns"
					:data="loadData"
					:alert="options.alert.show"
					bordered
					:row-key="(record) => record.insuVoiceId"
					:tool-config="toolConfig"
				>
					<template #bodyCell="{ column, record }">
						<template v-if="column.dataIndex === 'isInspected'">
							<a-tag :color="record.isInspected ? 'success' : 'warning'">
								{{ record.isInspected ? '已质检' : '未质检' }}
							</a-tag>
						</template>
						<template v-if="column.dataIndex === 'action'">
							<a-space>
								<a-button
									type="primary"
									size="small"
									@click="handleInspect(record)"
									:disabled="record.isInspected === 1"
								>
									质检
								</a-button>
								<a-button
									type="link"
									size="small"
									@click="handleView(record)"
									:disabled="record.isInspected === 0"
								>
									查看
								</a-button>
							</a-space>
						</template>
					</template>
				</s-table>
			</a-col>

			<!-- 右侧质检结果 -->
			<a-col :span="16">
				<div v-if="currentRecord" class="inspection-result">
					<a-descriptions bordered>
						<a-descriptions-item label="录音ID">{{ currentRecord.insuVoiceId }}</a-descriptions-item>
						<a-descriptions-item label="质检时间">{{ currentRecord.inspectionTime }}</a-descriptions-item>
						<a-descriptions-item label="质检状态">
							<a-tag :color="currentRecord.isInspected ? 'success' : 'warning'">
								{{ currentRecord.isInspected ? '已质检' : '未质检' }}
							</a-tag>
						</a-descriptions-item>
					</a-descriptions>

					<div class="result-wrapper">
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
													<div v-for="(item, eIndex) in violation.evidence" :key="eIndex" class="evidence-item">
														<span class="dialog-id">对话{{ item.dialogId }}</span>
														<span>{{ item.content }}</span>
													</div>
												</div>
											</a-card>
										</div>
										<div class="performance-section" v-if="audit.performanceAnalysis">
											<h4>表现分析：</h4>
											<p>{{ audit.performanceAnalysis.overallComments }}</p>
										</div>
									</div>
								</div>
							</template>
						</a-spin>
					</div>
				</div>
				<a-empty v-else description="请选择录音进行质检" />
			</a-col>
		</a-row>
	</a-card>
</template>

<script setup name="quality">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import qualityApi from '@/api/inspection/qualityApi'

const columns = [
	{
		title: '录音ID',
		dataIndex: 'insuVoiceId',
		align: 'center'
	},
	{
		title: '质检状态',
		dataIndex: 'isInspected',
		align: 'center'
	},
	{
		title: '质检时间',
		dataIndex: 'inspectionTime',
		align: 'center'
	},
	{
		title: '操作',
		dataIndex: 'action',
		width: 100,
		align: 'center'
	}
]

const currentRecord = ref(null)
const inspectionResult = ref(null)
const loading = ref(false)
const tableRef = ref()
const toolConfig = { refresh: true, height: true, columnSetting: true, striped: false }

const selectedRowKeys = ref([])
const options = {
	alert: {
		show: true,
		clear: () => {
			selectedRowKeys.value = []
		}
	},
	rowSelection: {
		onChange: (selectedRowKey) => {
			selectedRowKeys.value = selectedRowKey
		}
	}
}

const loadData = (parameter) => {
	return qualityApi.page(parameter).then((data) => {
		return data
	})
}

const handleInspect = async (record) => {
	try {
		currentRecord.value = record
		loading.value = true
		const data = await qualityApi.submitInspection({
			insuVoiceId: record.insuVoiceId
		})

		message.success('质检成功')

		// 更新当前记录状态
		currentRecord.value = {
			...record,
			isInspected: 1,
			inspectionTime: new Date().toLocaleString()
		}

		// 刷新表格
		await tableRef.value?.refresh()

		// 直接调用 handleView 显示结果
		await handleView(currentRecord.value)

	} catch (error) {
		message.error('质检失败：' + error.message)
	} finally {
		loading.value = false
	}
}

const handleView = async (record) => {
	try {
		currentRecord.value = record
		loading.value = true
		const data = await qualityApi.getInspectionResult(record.insuVoiceId)
		console.log('质检结果:', data)
		inspectionResult.value = JSON.parse(data)
		console.log('解析后的质检结果:', inspectionResult.value)
		currentRecord.value = {
			...record,
			isInspected: 1,
			inspectionTime: record.inspectionTime || new Date().toLocaleString()
		}
	} catch (error) {
		console.error('获取质检结果失败:', error)
		message.error('获取质检结果失败：' + error.message)
	} finally {
		loading.value = false
	}
}
</script>

<style scoped>
.inspection-result {
	padding: 16px;
	background: #fff;
	border: 1px solid #f0f0f0;
	border-radius: 2px;
}

.result-wrapper {
	margin-top: 16px;
	padding: 16px;
}

.result-section {
	margin-bottom: 24px;
}

.result-section h3 {
	margin-bottom: 16px;
	color: #1890ff;
}

.score {
	font-size: 18px;
	font-weight: bold;
	color: #52c41a;
}

.evidence-list {
	margin: 8px 0;
	padding: 8px;
	background: #f5f5f5;
	border-radius: 4px;
}

.evidence-item {
	margin: 4px 0;
	padding: 4px 8px;
}

.dialog-id {
	margin-right: 8px;
	padding: 2px 6px;
	background: #1890ff;
	color: white;
	border-radius: 4px;
}

.suggestion {
	margin-top: 8px;
	color: #52c41a;
}
</style>
