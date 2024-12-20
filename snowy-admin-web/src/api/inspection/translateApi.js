import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/inspection/translate/` + url, ...arg)

/**
 * 亲求翻译Api接口管理器
 *
 * @author tanghaoyu
 * @date  2024/12/13 12:08
 **/
export default {
	// 翻译
	translateVoice(data) {
		return request('submit', data)
	},
}
