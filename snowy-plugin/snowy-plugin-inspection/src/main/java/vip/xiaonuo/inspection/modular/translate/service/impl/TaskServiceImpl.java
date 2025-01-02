package vip.xiaonuo.inspection.modular.translate.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import vip.xiaonuo.inspection.core.util.HttpUtil;
import vip.xiaonuo.inspection.core.util.LoggerUtil;
import vip.xiaonuo.inspection.core.config.ExternalApiConfig;
import vip.xiaonuo.inspection.modular.translate.dto.QueryResponse;
import vip.xiaonuo.inspection.modular.translate.dto.SubmitTaskRequestBody;
import vip.xiaonuo.inspection.modular.translate.dto.SubmitTaskResponse;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;
import vip.xiaonuo.inspection.modular.translate.service.TaskService;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 负责提交任务并获取 taskId
 * @Author tang
 * @Date 2024/12/19
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private ExternalApiConfig externalApiConfig;

    /**
     * 提交语音转文字任务并返回任务 ID
     *
     * @param translateParam 请求参数
     * @return 任务 ID
     */
    @Override
    public SubmitTaskResponse submitTask(TranslateParam translateParam) {
        String url = externalApiConfig.getServiceUrl() + "/submit";
        HttpHeaders headers = HttpUtil.buildHeaders(translateParam.getToken());

        // 构建嵌套的请求体
        SubmitTaskRequestBody requestBody = SubmitTaskRequestBody.build(translateParam);

        try {
            String response = HttpUtil.postWithRestTemplate(url, headers, requestBody);
            return extractTaskIdFromResponse(response);
        } catch (Exception e) {
            logger.error("提交任务失败", e);
            throw new LoggerUtil.TranslateServiceException("提交任务失败", e);
        }
    }

    /**
     * 解析提交任务的响应并提取任务 ID
     *
     * @param response 响应字符串
     * @return 提交任务响应对象
     */
    private SubmitTaskResponse extractTaskIdFromResponse(String response) {
        try {
            SubmitTaskResponse submitTaskResponse = objectMapper.readValue(response, SubmitTaskResponse.class);
            if (submitTaskResponse.getResp() != null && submitTaskResponse.getResp().getCode() == 1000) {
                // 创建一个新的 SubmitTaskResponse 只包含 taskId
                SubmitTaskResponse simplifiedResponse = new SubmitTaskResponse();
                // 可以在 SubmitTaskResponse 中添加一个字段 taskId 或者创建一个新的响应类
                simplifiedResponse.setResp(new SubmitTaskResponse.Resp());
                simplifiedResponse.getResp().setId(submitTaskResponse.getResp().getId());
                simplifiedResponse.getResp().setCode(submitTaskResponse.getResp().getCode());
                simplifiedResponse.getResp().setMsg(submitTaskResponse.getResp().getMsg());
                return simplifiedResponse;
            } else {
                logger.error("提交任务失败，响应码: {}", submitTaskResponse.getResp() != null ? submitTaskResponse.getResp().getCode() : "null");
                throw new LoggerUtil.TranslateServiceException("提交任务失败，响应码: " + (submitTaskResponse.getResp() != null ? submitTaskResponse.getResp().getCode() : "null"));
            }
        } catch (Exception e) {
            logger.error("解析响应失败", e);
            throw new LoggerUtil.TranslateServiceException("解析响应失败", e);
        }
    }

    /**
     * 查询任务
     *
     * @param queryParams 查询参数
     * @return 响应字符串
     */
    @Override
    public String queryTask(Map<String, Object> queryParams) {
        String url = externalApiConfig.getServiceUrl() + "/query";
        String token = (String) queryParams.get("token");
        HttpHeaders headers = HttpUtil.buildHeaders(token);

        try {
            return HttpUtil.postWithRestTemplate(url, headers, queryParams);
        } catch (Exception e) {
            logger.error("查询任务失败", e);
            throw new LoggerUtil.TranslateServiceException("查询任务失败", e);
        }
    }

    /**
     * 构建查询任务的参数
     *
     * @param taskId 任务 ID
     * @return 查询参数
     */
    @Override
    public Map<String, Object> buildQueryParams(String taskId) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("appid", externalApiConfig.getAppid());
        queryParams.put("token", externalApiConfig.getToken());
        queryParams.put("cluster", externalApiConfig.getCluster());
        queryParams.put("id", taskId);
        return queryParams;
    }

    /**
     * 解析查询任务的响应
     *
     * @param response 响应字符串
     * @return 解析后的 QueryResponse 对象
     */
    @Override
    public QueryResponse parseQueryResponse(String response) {
        // 添加日志记录 responseStr
        logger.info("解析查询响应: {}", response);

        try {
            return objectMapper.readValue(response, QueryResponse.class);
        } catch (Exception e) {
            logger.error("解析查询响应失败", e);
            throw new LoggerUtil.TranslateServiceException("解析查询响应失败", e);
        }
    }
}
