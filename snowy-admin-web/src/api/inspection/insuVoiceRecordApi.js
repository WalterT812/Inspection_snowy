import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/inspection/voiceRecord/` + url, ...arg)

/**
 * 存储录音Api接口管理器
 *
 * @author tanghaoyu
 * @date  2024/12/13 12:08
 **/
export default {
	// 获取存储录音分页
	insuVoiceRecordPage(data) {
		return request('page', data, 'get')
	},
	// 提交存储录音表单 edit为true时为编辑，默认为新增
	insuVoiceRecordSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除存储录音
	insuVoiceRecordDelete(data) {
		return request('delete', data)
	},
	// 获取存储录音详情
	insuVoiceRecordDetail(data) {
		return request('detail', data, 'get')
	}
}
