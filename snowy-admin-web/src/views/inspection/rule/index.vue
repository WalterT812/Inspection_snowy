<template>
	<a-card :bordered="false">
		<div style="margin-bottom: 16px; display: flex; align-items: center;">
			<a-form ref="formR" :model="formData" :rules="formRules" layout="inline" style="margin-right: 8px;">
				<a-form-item label="规则编号：" name="ruleCode" style="margin-bottom: 0;">
					<a-input
						v-model:value="formData.ruleCode"
						placeholder="请输入规则编号"
						style="width: 200px; margin-right: 8px;"
						allow-clear
					/>
				</a-form-item>
				<a-form-item label="规则名称：" name="ruleName" style="margin-bottom: 0;">
					<a-input
						v-model:value="formData.ruleName"
						placeholder="请输入规则名称"
						style="width: 200px; margin-right: 8px;"
						allow-clear
					/>
				</a-form-item>
			</a-form>
			<a-button type="primary" @click="onSubmit" :loading="submitLoading" style="margin-left: 8px;">
				<template #icon><plus-outlined/></template>
				添加
			</a-button>
		</div>

		<s-table
			ref="tableRef"
			:loading="tableLoading"
			:columns="columns"
			:data="loadData"
			:alert="options.alert.show"
			bordered
			:row-key="(record) => record.id"
			:tool-config="toolConfig"
			:row-selection="options.rowSelection"
			:scroll="{ x: 1200, y: 500 }"
		>
			<template #operator class="table-operator">
				<a-space>
					<xn-batch-button
						v-if="hasPerm('inspectionRuleBatchDelete')"
						buttonName="批量删除"
						icon="DeleteOutlined"
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchRule"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a-button @click="handleEdit(record)" style="color: #1890ff">编辑</a-button>
						<a-divider type="vertical"/>
						<a-button @click="handleStatusChange(record)" :style="{ color: record.ruleStatus ? '#52c41a' : '#ff4d4f' }">
							{{ record.ruleStatus ? '禁用' : '启用' }}
						</a-button>
						<a-divider type="vertical"/>
						<a-popconfirm title="确定要删除吗？" @confirm="deleteRule(record)">
							<a-button>删除</a-button>
						</a-popconfirm>
					</a-space>
				</template>
				<template v-if="column.dataIndex === 'ruleStatus'">
					<a-tag :color="record.ruleStatus ? 'success' : 'warning'">
						{{ record.ruleStatus ? '已启用' : '已禁用' }}
					</a-tag>
				</template>
			</template>
		</s-table>

		<Form ref="formRef" @refreshTable="refreshTable"/>
	</a-card>
</template>

<script setup name="inspectionRule">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { cloneDeep } from 'lodash-es'
import Form from './form.vue'
import inspectionRuleApi from '@/api/inspection/inspectionRuleApi'

// 表格配置
const tableRef = ref()
const formRef = ref()
const formR = ref()
const toolConfig = { refresh: true, height: true, columnSetting: true, striped: false }
const refreshTable = () => {
	tableRef.value.refresh(true)
}

// 表格列配置
const columns = [
	{
		title: '规则编号',
		dataIndex: 'ruleCode',
		align: 'center',
		width: 80,
		ellipsis: true,
	},
	{
		title: '规则名称',
		dataIndex: 'ruleName',
		align: 'center',
		width: 80,
		ellipsis: true
	},
	{
		title: '规则描述',
		dataIndex: 'ruleDescription',
		align: 'left',
		width: 200,
		ellipsis: true
	},
	{
		title: '规则等级',
		dataIndex: 'ruleLevel',
		align: 'center',
		width: 50,
	},
	{
		title: '规则状态',
		dataIndex: 'ruleStatus',
		align: 'center',
		width: 50
	},
	{
		title: '操作',
		dataIndex: 'action',
		align: 'center',
		width: 180
	}
]

// 表格选择配置
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

// 表格loading状态
const tableLoading = ref(false)

// 表格数据加载
const loadData = (parameter) => {
	tableLoading.value = true
	return inspectionRuleApi.page(parameter)
		.then((data) => {
			return data
		})
		.finally(() => {
			tableLoading.value = false
		})
}

// 表单数据
const formData = ref({
	ruleCode: '',
	ruleName: '',
	ruleDescription: '',
	ruleLevel: '',
	ruleStatus: true
})

const submitLoading = ref(false)

// 表单验证规则
const formRules = {
	ruleCode: [{ required: true, message: '请输入规则编号' }],
	ruleName: [{ required: true, message: '请输入规则名称' }]
}

// 添加规则
const onSubmit = () => {
	formR.value
		.validate()
		.then(() => {
			submitLoading.value = true
			const formDataParam = cloneDeep(formData.value)
			inspectionRuleApi
				.add(formDataParam)
				.then(() => {
					message.success('添加成功')
					formData.value = {
						ruleCode: '',
						ruleName: '',
						ruleDescription: '',
						ruleLevel: '',
						ruleStatus: true
					}
					tableRef.value.refresh(true)
				})
				.catch((error) => {
					message.error(error.msg || '添加失败')
				})
				.finally(() => {
					submitLoading.value = false
				})
		})
}

// 编辑规则
const handleEdit = (record) => {
	formRef.value.onOpen(record)
}

// 更新规则状态
const handleStatusChange = (record) => {
	const newStatus = !record.ruleStatus
	inspectionRuleApi
		.updateStatus({ id: record.id, ruleStatus: newStatus })
		.then(() => {
			message.success('状态更新成功')
			refreshTable()
		})
		.catch(() => {
			message.error('状态更新失败')
		})
}

// 删除规则
const deleteRule = (record) => {
	inspectionRuleApi
		.delete([{ id: record.id }])
		.then(() => {
			message.success('删除成功')
			refreshTable()
		})
}

// 批量删除规则
const deleteBatchRule = (ids) => {
	inspectionRuleApi
		.delete(ids.map(id => ({ id })))
		.then(() => {
			message.success('批量删除成功')
			refreshTable()
		})
}
</script>
