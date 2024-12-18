import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/inspection/translate/` + url, ...arg)

/**
 * 查询翻译Api接口管理器
 *
 * @author tanghaoyu
 * @date  2024/12/13 12:08
 **/
export default {
	queryTaskResult(data){
		return request("queryTaskResult", data)
	}
}
