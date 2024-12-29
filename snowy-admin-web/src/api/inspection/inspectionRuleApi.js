import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => {
	return baseRequest(`/inspection/rule/${url}`, ...arg)
}

/**
 * 质检规则 API
 */
export default {
	// 获取规则分页列表
	page(params) {
		return request('page', params, 'get')
	},
	// 添加规则
	add(params) {
		return request('add', params, 'post')
	},
	// 编辑规则
	edit(params) {
		return request('edit', params, 'post')
	},
	// 删除规则
	delete(params) {
		return request('delete', params, 'post')
	},
	// 更新规则状态
	updateStatus(params) {
		return request('updateStatus', params, 'post')
	},
	// 获取所有启用的规则
	getEnabledRules() {
		return request('list', {}, 'get')
	},
	// 根据等级获取规则
	getRulesByLevel(level) {
		return request('listByLevel', { level }, 'get')
	}
}
