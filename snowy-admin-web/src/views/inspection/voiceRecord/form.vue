<template>
	<xn-form-container
		:title="formData.id ? '编辑存储录音' : '增加存储录音'"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
			<a-form-item label="录音URL：" name="voiceUrl">
				<a-input v-model:value="formData.voiceUrl" placeholder="请输入录音文件的 URL" allow-clear />
			</a-form-item>
		</a-form>
		<template #footer>
			<a-button style="margin-right: 8px" @click="onClose">关闭</a-button>
			<a-button type="primary" @click="onSubmit" :loading="submitLoading">保存</a-button>
		</template>
	</xn-form-container>
</template>

<script setup name="insuVoiceRecordForm">
import { cloneDeep } from 'lodash-es'
import { required } from '@/utils/formRules'
import insuVoiceRecordApi from '@/api/inspection/insuVoiceRecordApi'
import {ref} from "vue";

// 抽屉状态
const open = ref(false)
const emit = defineEmits({ successful: null })
const formRef = ref()

// 表单数据
const formData = ref({
	isTranslated: 0, // 默认未翻译
	isInspected: 0, // 默认未质检
	isQueried: 0, // 默认未查询
	uploadTime: null, // 默认上传时间为空, 后端处理
	translateTime: null, // 翻译时间, 默认空
	inspectionTime: null, // 质检时间, 默认空
	taskId: null // 任务Id, 默认空
})
const submitLoading = ref(false)

// 打开抽屉
const onOpen = (record) => {
	console.log("编辑的记录：", record)  // 打印传入的 record
	open.value = true
	if (record) {
		let recordData = cloneDeep(record)
		formData.value = Object.assign({}, recordData)
	}
}
// 关闭抽屉
const onClose = () => {
	formRef.value.resetFields()
	formData.value = {}
	open.value = false
}


// 默认要校验的
const formRules = {
}

// 验证并提交数据
const onSubmit = () => {
	formRef.value
		.validate()
		.then(() => {
			submitLoading.value = true
			const formDataParam = cloneDeep(formData.value)
			insuVoiceRecordApi
				.insuVoiceRecordSubmitForm(formDataParam, formDataParam.id)
				.then(() => {
					onClose()
					emit('successful')
				})
				.finally(() => {
					submitLoading.value = false
				})
		})
		.catch(() => {
		})
}

// 抛出函数
defineExpose({
	onOpen
})

</script>
