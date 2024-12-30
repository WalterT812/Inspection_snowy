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
				<template #icon>
					<plus-outlined/>
				</template>
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
				<template v-if="column.dataIndex === 'isTranslated'">
					<a-tag :color="record.isTranslated ? 'success' : 'warning'">
						{{ record.isTranslated ? '已翻译' : '未翻译' }}
					</a-tag>
				</template>
				<template v-if="column.dataIndex === 'isQueried'">
					<a-tag :color="record.isQueried ? 'success' : 'warning'">
						{{ record.isQueried ? '已查询' : '未查询' }}
					</a-tag>
				</template>
				<template v-if="column.dataIndex === 'isInspected'">
					<a-tag :color="record.isInspected ? 'success' : 'warning'">
						{{ record.isInspected ? '已质检' : '未质检' }}
					</a-tag>
				</template>
				<template v-if="['uploadTime', 'translateTime', 'inspectionTime'].includes(column.dataIndex)">
					<span>{{ record[column.dataIndex] ? formatDateTime(record[column.dataIndex]) : '-' }}</span>
				</template>
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a-button @click="handleTranslate(record)" style="color: #1890ff;">翻译</a-button>
						<a-divider type="vertical"/>
						<Form ref="formRef"/>
						<a-button @click="formRef.onOpen(record)" style="color: #1890ff">编辑</a-button>
						<a-divider type="vertical"/>
						<query-form ref="queryFormRef" @refreshTable="refreshTable"/>
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
import {ref, h} from 'vue'
import {cloneDeep} from 'lodash-es'
import dayjs from 'dayjs'

import Form from './form.vue'
import queryForm from './queryForm.vue';

import insuVoiceRecordApi from '@/api/inspection/insuVoiceRecordApi'
import translateApi from "@/api/inspection/translateApi"
import {message} from 'ant-design-vue'
import Clipboard from "clipboard";

const tableRef = ref()
const formRef = ref()
const queryFormRef = ref()
const formR = ref()
const toolConfig = {refresh: true, height: true, columnSetting: true, striped: false}
const refreshTable = () => {
	debugger
	tableRef.value.refresh(true);
};

const formatDateTime = (dateStr) => {
	if (!dateStr) return '-'
	return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
}

const columns = [
	{
		title: '录音ID',
		dataIndex: 'insuVoiceId',
		align: 'center',
	},
	{
		title: '录音URL',
		dataIndex: 'voiceUrl',
		align: 'center',
		customRender: ({record}) => {
			const url = record.voiceUrl;
			if (url && url.length > 20) {
				const start = url.slice(0, 10);
				const end = url.slice(-10);
				const shortUrl = `${start}...${end}`;
				return h('span', {
					onMouseenter: (e) => {
						const tooltipDiv = document.createElement('div');
						tooltipDiv.textContent = url;
						tooltipDiv.style.fontSize = '12px';
						tooltipDiv.style.position = 'absolute';
						tooltipDiv.style.backgroundColor = 'white';
						tooltipDiv.style.border = '1px black solid';
						tooltipDiv.style.padding = '3px';
						tooltipDiv.style.zIndex = 1000;
						document.body.appendChild(tooltipDiv);
						const rect = e.target.getBoundingClientRect();
						tooltipDiv.style.top = rect.bottom + 'px';
						tooltipDiv.style.left = rect.left + 'px';
					},
					onMouseleave: () => {
						const tooltips = document.querySelectorAll('div[style*="position: absolute;"]');
						tooltips.forEach((tooltip) => tooltip.remove());
					},
					onClick: () => {
						const clipboard = new Clipboard('.url-display-wrapper', {
							text: () => url
						});
						clipboard.on('success', () => {
							console.log('已复制到剪贴板');
							message.success("复制成功");
							clipboard.destroy();
						});
						clipboard.on('error', (err) => {
							console.error('复制到剪贴板失败', err);
							message.error("复制失败");
							clipboard.destroy();
						});
					},
					class: 'url-display-wrapper'
				}, shortUrl);
			}
			return url;
		}
	},
	{
		title: '翻译状态',
		dataIndex: 'isTranslated',
		align: 'center',
	},
	{
		title: '查询状态',
		dataIndex: 'isQueried',
		align: 'center',
	},
	{
		title: '质检状态',
		dataIndex: 'isInspected',
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
		return data;
	})
}

// 翻译操作
const handleTranslate = (record) => {
	// 更新翻译状态，防止多次点击
	record.isTranslated = 1
	// 调用翻译 API
	translateApi
		.translateVoice({insuVoiceId: record.insuVoiceId})
		.then((response) => {
			console.log(response);
			const {code, id, msg} = response;  // 获取返回的数据和消息
			debugger
			if (code === 204) {
				// 状态码为204
				message.info(msg);
			} else if (code === 200) {
				// 状态码为200
				message.success(msg);
			} else {
				// 其他非预期的状态码
				message.error('翻译请求出现未知状态码，请稍后重试');
			}
			refreshTable();
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
	voiceUrl: [{required: true, message: '请输入录音文件的 URL'}],
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

<style>
.url-display-wrapper {
	max-width: 200px; /* 设置最大宽度，避免过长的简短 URL 撑开表格单元格 */
	overflow: hidden; /* 超出部分隐藏 */
	text-overflow: ellipsis; /* 显示省略号表示有内容被隐藏 */
	white-space: nowrap; /* 禁止文本换行 */
	cursor: pointer; /* 鼠标指针变为手型，提示用户可以交互 */
}
</style>
