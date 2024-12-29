<template>
	<a-drawer
		:title="formData.id ? '编辑规则' : '添加规则'"
		:width="600"
		:visible="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
			<a-form-item label="规则编号：" name="ruleCode">
				<a-input v-model:value="formData.ruleCode" placeholder="请输入规则编号" allow-clear />
			</a-form-item>
			<a-form-item label="规则名称：" name="ruleName">
				<a-input v-model:value="formData.ruleName" placeholder="请输入规则名称" allow-clear />
			</a-form-item>
			<a-form-item label="规则描述：" name="ruleDescription">
				<a-textarea
					v-model:value="formData.ruleDescription"
					placeholder="请输入规则描述"
					:auto-size="{ minRows: 3, maxRows: 5 }"
					allow-clear
				/>
			</a-form-item>
			<a-form-item label="规则等级：" name="ruleLevel">
				<a-select v-model:value="formData.ruleLevel" placeholder="请选择规则等级" allow-clear>
					<a-select-option value="HIGH">高</a-select-option>
					<a-select-option value="MEDIUM">中</a-select-option>
					<a-select-option value="LOW">低</a-select-option>
				</a-select>
			</a-form-item>
			<a-form-item label="规则状态：" name="ruleStatus">
				<a-switch v-model:checked="formData.ruleStatus" />
			</a-form-item>
		</a-form>
		<template #footer>
			<a-space>
				<a-button type="primary" @click="onSubmit" :loading="submitLoading">确定</a-button>
				<a-button @click="onClose">取消</a-button>
			</a-space>
		</template>
	</a-drawer>
</template>

<script setup name="inspectionRuleForm">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { cloneDeep } from 'lodash-es'
import inspectionRuleApi from '@/api/inspection/inspectionRuleApi'

// 定义emit事件
const emit = defineEmits({ refreshTable: null })

// 抽屉是否打开
const open = ref(false)

// 表单数据
const formData = ref({})
const formRef = ref()
const submitLoading = ref(false)

// 表单验证规则
const formRules = {
	ruleCode: [{ required: true, message: '请输入规则编号' }],
	ruleName: [{ required: true, message: '请输入规则名称' }],
	ruleLevel: [{ required: true, message: '请选择规则等级' }]
}

// 打开抽屉
const onOpen = (record) => {
	open.value = true
	if (record) {
		let recordData = cloneDeep(record)
		formData.value = Object.assign({}, recordData)
	}
}

// 关闭抽屉
const onClose = () => {
	open.value = false
	formRef.value?.resetFields()
	formData.value = {}
}

// 提交表单
const onSubmit = () => {
	formRef.value
		.validate()
		.then(() => {
			submitLoading.value = true
			const formDataParam = cloneDeep(formData.value)
			if (formData.value.id) {
				inspectionRuleApi
					.edit(formDataParam)
					.then(() => {
						message.success('编辑成功')
						emit('refreshTable')
						onClose()
					})
					.finally(() => {
						submitLoading.value = false
					})
			} else {
				inspectionRuleApi
					.add(formDataParam)
					.then(() => {
						message.success('添加成功')
						emit('refreshTable')
						onClose()
					})
					.finally(() => {
						submitLoading.value = false
					})
			}
		})
}

// 暴露变量和方法
defineExpose({
	onOpen
})
</script>
