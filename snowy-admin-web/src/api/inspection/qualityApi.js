import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/inspection/quality/` + url, ...arg)

/**
 * 质检Api接口管理器
 *
 * @author tanghaoyu
 * @date  2024/12/13 12:08
 **/
export default {
    // 获取质检分页
    page(data) {
        return request('page', data, 'get')
    },
    // 提交质检
    submitInspection(data) {
        return request('submit', data, 'post')
    }
}
