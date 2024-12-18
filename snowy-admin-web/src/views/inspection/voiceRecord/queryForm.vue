<template>
	<xn-form-container
		:width="700"
		:title="'查询翻译结果'"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-form ref="queryFormRef" :model="queryFormData" :rules="formRules" layout="vertical">
			<a-form-item label="录音Id：" name="insuVoiceId">
				<pre>{{formData.insuVoiceId}}</pre>
			</a-form-item>
			<a-form-item label="任务Id：" name="taskId">
				<pre>{{formData.taskId}}</pre>
			</a-form-item>
			<a-form-item label="查询结果：" name="Result">
				<div class="data-display-container">
					<pre>{{ queryFormData.queryResult }}</pre>
				</div>
			</a-form-item>
		</a-form>
		<template #footer>
			<a-button style="margin-right: 8px" @click="onClose">关闭</a-button>
		</template>
	</xn-form-container>
</template>

<script setup name="insuVoiceRecordForm">
import { cloneDeep } from 'lodash-es';
import { required } from '@/utils/formRules';
import insuVoiceRecordApi from '@/api/inspection/insuVoiceRecordApi';
import QueryTranslateApi from "@/api/inspection/queryTranslateApi";
import {ref} from "vue";
import {message} from "ant-design-vue";


// 抽屉状态
const open = ref(false)
const emit = defineEmits({ successful: null })
const queryFormRef = ref()

// 表单数据
const formData = ref({
	insuVoiceId: 0, // 默认0
	isTranslated: 0, // 默认未翻译
	isInspected: 0, // 默认未质检
	isQueried: 0, // 默认未查询
	uploadTime: null, // 默认上传时间为空, 后端处理
	translateTime: null, // 翻译时间, 默认空
	inspectionTime: null, // 质检时间, 默认空
	taskId: null // 任务Id, 默认空
})
const submitLoading = ref(false)

const queryFormData = ref({
	queryResult: null,
})

const queryData = async (record) => {
	try {
		const response = await QueryTranslateApi.queryTaskResult({ taskId: record.taskId });
		const resultJson = JSON.stringify(response);
		queryFormData.value.queryResult = resultJson;
		// 打印结果到控制台，方便调试查看
		console.log(resultJson);
	} catch (error) {
		console.error('查询任务结果失败', error);
		message.error('查询任务结果失败，请稍后重试');
	}
}

// 打开抽屉
const onOpen = (record) => {
	console.log("编辑的记录：", record)  // 打印传入的 record
	open.value = true
	if (record) {
		let recordData = cloneDeep(record)
		formData.value = Object.assign({}, recordData)
		queryData(record);
	}
}

// 关闭抽屉
const onClose = () => {
	queryFormRef.value.resetFields()
	formData.value = {}
	open.value = false
}

// 默认要校验的
const formRules = {
	taskId: [
		{ required: true, message: '请输入任务Id' }
	]
}

// 抛出函数
defineExpose({
	onOpen
})

</script>

<style scoped>
.data-display-container {
	border: 1px solid #ccc;
	padding: 10px;
	margin-bottom: 10px;
	border-radius: 5px;
}

/* 以下是新增的样式，用于美化 <pre> 标签展示的内容 */
pre {
	white-space: pre-wrap; /* 自动换行，避免内容超出容器宽度 */
	font-family: Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace; /* 设置等宽字体，让代码格式更清晰 */
	font-size: 14px; /* 调整字体大小 */
	line-height: 1.6; /* 调整行间距 */
	color: #333; /* 调整字体颜色 */
}
</style>
