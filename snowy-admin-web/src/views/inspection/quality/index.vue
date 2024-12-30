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
							<a-button
								type="primary"
								size="small"
								@click="handleInspect(record)"
								:disabled="record.isInspected === 1"
							>
								质检
							</a-button>
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
					<pre v-if="inspectionResult" class="result-content">{{ inspectionResult }}</pre>
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
		console.log('加载的数据：', data)
		return data
	})
}

const handleInspect = async (record) => {
	try {
		currentRecord.value = record
		const response = await qualityApi.submitInspection({
			insuVoiceId: record.insuVoiceId
		})
		if (response.code === 200) {
			message.success('质检成功')
			inspectionResult.value = JSON.stringify(response.data, null, 2)
			tableRef.value?.refresh()
		}
	} catch (error) {
		message.error('质检失败：' + error.message)
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

.result-content {
	margin-top: 16px;
	padding: 16px;
	background: #f5f5f5;
	border-radius: 4px;
	white-space: pre-wrap;
	font-family: Consolas, Monaco, monospace;
	font-size: 14px;
	line-height: 1.6;
}
</style>
