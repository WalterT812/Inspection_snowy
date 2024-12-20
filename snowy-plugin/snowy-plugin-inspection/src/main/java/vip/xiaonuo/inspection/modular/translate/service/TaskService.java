package vip.xiaonuo.inspection.modular.translate.service;

import vip.xiaonuo.inspection.modular.translate.dto.QueryResponse;
import vip.xiaonuo.inspection.modular.translate.dto.SubmitTaskResponse;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;

import java.util.Map;

/**
 * @author tanghaoyu
 * @date 2024/12/19
 * @description 负责提交任务并获取 taskId
 **/
public interface TaskService {
    /**
     * 提交语音转文字任务并返回任务响应
     *
     * @param translateParam 翻译参数
     * @return 提交任务响应
     */
    SubmitTaskResponse submitTask(TranslateParam translateParam);

    /**
     * 查询任务
     *
     * @param queryParams 查询参数
     * @return 响应字符串
     */
    String queryTask(Map<String, Object> queryParams);

    /**
     * 构建查询任务的参数
     *
     * @param taskId 任务 ID
     * @return 查询参数
     */
    Map<String, Object> buildQueryParams(String taskId);

    /**
     * 解析查询任务的响应
     *
     * @param response 响应字符串
     * @return 解析后的 QueryResponse 对象
     */
    QueryResponse parseQueryResponse(String response);
}
