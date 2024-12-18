<template>
	<a-card :bordered="false">

		<div style="margin-bottom: 16px; display: flex; align-items: center;">
			<!-- 使用 flex 布局确保所有元素在同一行内 -->
			<a-form ref="formR" :model="formData" :rules="formRules" layout="inline" style="margin-right: 8px;">
				<a-form-item label="录音URL：" name="voiceUrl" style="margin-bottom: 0;">
					<a-input
						v-model:value="formData.voiceUrl"
						placeholder="请输入录音文件的 URL"
						style="width: 400px; margin-right: 8px;"
						allow-clear
					/>
				</a-form-item>
			</a-form>
			<!-- 按钮与输入框在同一行内，靠左对齐 -->
			<a-button type="primary" @click="onSubmit" :loading="submitLoading" style="margin-left: 8px;">
				<template #icon><plus-outlined /></template>
				插入
			</a-button>
		</div>

		<s-table
			ref="tableRef"
			:columns="columns"
			:data="loadData"
			:alert="options.alert.show"
			bordered
			:row-key="(record) => record.id"
			:tool-config="toolConfig"
			:row-selection="options.rowSelection"
		>
			<template #operator class="table-operator">
				<a-space>
					<xn-batch-button
						v-if="hasPerm('insuVoiceRecordBatchDelete')"
						buttonName="批量删除"
						icon="DeleteOutlined"
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchInsuVoiceRecord"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a-button @click="handleTranslate(record)" style="color: #1890ff;">翻译</a-button>
						<a-divider type="vertical"/>
						<Form ref="formRef" />
						<a-button @click="formRef.onOpen(record)" style="color: #1890ff">编辑</a-button>
						<a-divider type="vertical"/>
						<query-form ref="queryFormRef" />
						<a-button @click="queryFormRef.onOpen(record)" style="color: #1890ff;">查询</a-button>
						<a-divider type="vertical"/>
						<a-popconfirm title="确定要删除吗？" @confirm="deleteInsuVoiceRecord(record)">
							<a-button>删除</a-button>
						</a-popconfirm>
					</a-space>
				</template>
			</template>
		</s-table>
	</a-card>
</template>

<script setup name="voiceRecord">
import { ref } from 'vue'
import { cloneDeep } from 'lodash-es'

import Form from './form.vue'
import queryForm from './queryForm.vue';

import insuVoiceRecordApi from '@/api/inspection/insuVoiceRecordApi'
import translateApi from "@/api/inspection/translateApi"
import { message } from 'ant-design-vue'

const tableRef = ref()
const formRef = ref()
const queryFormRef = ref()
const formR = ref()
const toolConfig = { refresh: true, height: true, columnSetting: true, striped: false }
const columns = [
	{
		title: '录音ID',
		dataIndex: 'insuVoiceId',
		align: 'center',
	},
	{
		title: '录音URL',
		dataIndex: 'voiceUrl',
		ellipsis: true,
		align: 'center',
	},
	{
		title: '翻译状态',
		dataIndex: 'isTranslated',
		align: 'center',
	},
	{
		title: '质检状态',
		dataIndex: 'isInspected',
		align: 'center',
	},
	{
		title: '查询状态',
		dataIndex: 'isQueried',
		align: 'center',
	},
	{
		title: '上传时间',
		dataIndex: 'uploadTime',
		align: 'center',
	},
	{
		title: '翻译完成时间',
		dataIndex: 'translateTime',
		align: 'center',
	},
	{
		title: '质检完成时间',
		dataIndex: 'inspectionTime',
		align: 'center',
	},
	{
		title: '任务Id',
		dataIndex: 'taskId',
		align: 'center',
	}
]

columns.push({
	title: '操作',
	dataIndex: 'action',
	align: 'center',
	width: 400
})

const selectedRowKeys = ref([])
// 列表选择配置
const options = {
	alert: {
		show: true,
		clear: () => {
			selectedRowKeys.value = ref([])
		}
	},
	rowSelection: {
		onChange: (selectedRowKey, selectedRows) => {
			selectedRowKeys.value = selectedRowKey
		}
	}
}

// 表格数据加载
const loadData = (parameter) => {
	return insuVoiceRecordApi.insuVoiceRecordPage(parameter).then((data) => {
		console.log(data)
		// const formattedData = formatVoiceUrlData(data);
		return data;
	})
}

// // 数据格式化函数
// const formatVoiceUrlData = (data) => {
// 	if (data && data.records) {
// 		const formattedRecords = data.records.map(record => {
// 			const formattedRecord = {...record };
// 			if (formattedRecord.voiceUrl && formattedRecord.voiceUrl.length > 20) {
// 				const start = formattedRecord.voiceUrl.slice(0, 10);
// 				const end = formattedRecord.voiceUrl.slice(-10);
// 				formattedRecord.voiceUrl = `${start}...${end}`;
// 			}
// 			return formattedRecord;
// 		});
// 		return {...data, records: formattedRecords };
// 	}
// 	return data;
// };

// 翻译操作
const handleTranslate = (record) => {
	// 更新翻译状态，防止多次点击
	record.isTranslated = 1
	// 调用翻译 API
	translateApi
		.translateVoice({ insuVoiceId: record.insuVoiceId })
		.then((response) => {
			console.log(response);
			const { code, msg, data } = response;  // 获取返回的数据和消息
			if (code === 204) {
				// 状态码为204
				message.success(msg);  // 这里msg就是后端返回的自定义消息，比如"该语音文件已翻译"
			} else if (code === 200) {
				// 状态码为200
				message.success(msg);
			} else {
				// 其他非预期的状态码
				message.error('翻译请求出现未知状态码，请稍后重试');
			}
			tableRef.value.refresh(true); // 刷新表格
		})
		.catch((error) => {
			message.error(error.msg || '翻译失败，请稍后重试');
			// 这里catch中处理错误情况，也可以根据error的具体结构，比如error是否包含status等属性来进一步细分错误提示逻辑
		});
}

// 表单数据
const formData = ref({
	voiceUrl: '', // 添加 voiceUrl字段
	isTranslated: 0, // 默认未翻译
	isInspected: 0, // 默认未质检
	isQueried: 0, // 默认未查询
	uploadTime: null, // 默认上传时间为空, 后端处理
	translateTime: null, // 翻译时间, 默认空
	inspectionTime: null, // 质检时间, 默认空
	taskId: null // 任务Id, 默认空
})

// 查询数据
const queryFormData = ref({
	insuVoiceId: null, // 录音Id
	taskId: null, // 任务Id
	queryResult: '' // 查询结果
})

const submitLoading = ref(false)

// 默认要校验的
const formRules = {
	voiceUrl: [{ required: true, message: '请输入录音文件的 URL' }],
}

// 插入链接
const onSubmit = () => {
	formR.value
		.validate()
		.then(() => {
			submitLoading.value = true
			const formDataParam = cloneDeep(formData.value)
			insuVoiceRecordApi
				.insuVoiceRecordSubmitForm(formDataParam)
				.then(() => {
					formData.value.voiceUrl = ''
					tableRef.value.refresh(true)
				})
				.catch(() => {
					message.error('录音 URL 插入失败')
				})
				.finally(() => {
					submitLoading.value = false
				})
			})
		.catch(() => {
		})
}

// 删除
const deleteInsuVoiceRecord = (record) => {
	let params = [
		{
			id: record.id
		}
	]
	insuVoiceRecordApi.insuVoiceRecordDelete(params).then(() => {
		tableRef.value.refresh(true)
	})
}

// 批量删除
const deleteBatchInsuVoiceRecord = (params) => {
	insuVoiceRecordApi.insuVoiceRecordDelete(params).then(() => {
		tableRef.value.clearRefreshSelected()
	})
}
</script>
