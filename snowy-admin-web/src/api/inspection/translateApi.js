import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/inspection/translate/` + url, ...arg)

/**
 * 翻译并查询Api接口管理器
 *
 * @author tanghaoyu
 * @date  2024/12/30 18:12
 **/
export default {
	// 翻译并查询
	translate(data) {
		return request('submit', data)
	},

	// 获取对话内容
	getDialogs(data) {
		return request('dialogs', data, "get")
	}
}
