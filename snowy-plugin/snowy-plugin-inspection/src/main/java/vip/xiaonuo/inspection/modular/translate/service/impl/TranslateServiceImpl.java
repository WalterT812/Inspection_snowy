package vip.xiaonuo.inspection.modular.translate.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;
import vip.xiaonuo.inspection.modular.translate.mapper.InsuVoiceDialogMapper;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;
import vip.xiaonuo.inspection.modular.translate.service.TranslateService;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.service.InsuVoiceRecordService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 语音转文字 Service 实现类
 * 提供语音任务的提交和查询功能
 * @Author tanghaoyu
 * @Date 2024/12/12
 */
@Service
public class TranslateServiceImpl extends ServiceImpl<InsuVoiceDialogMapper, InsuVoiceDialog> implements TranslateService {
    private final InsuVoiceRecordService insuVoiceRecordService;
    // 构造方法注入
    public TranslateServiceImpl(InsuVoiceRecordService insuVoiceRecordService) {
        this.insuVoiceRecordService = insuVoiceRecordService;
    }

    @Value("${translate.appid}")
    private String appId;

    @Value("${translate.token}")
    private String token;

    @Value("${translate.cluster}")
    private String cluster;

    @Value("${translate.service-url}")
    private String serviceUrl;

    @Value("${translate.uid}")
    private String uid;

    private static final Logger logger = LoggerFactory.getLogger(TranslateServiceImpl.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 通过 INSU_VOICE_ID 查询 VOICE_URL 并提交语音转文字任务
     * @param insuVoiceId 录音 ID
     * @return 任务 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResult<HashMap<String, Object>> submitTaskByInsuVoiceId(Integer insuVoiceId) {

        InsuVoiceRecord record = getByInsuVoiceId(insuVoiceId);
        // 是否存在
        if (record == null) {
            throw new TranslateServiceException("未找到对应的语音文件", null);
        }
        // 是否翻译
        if (record.getIsTranslated() == 1) {
            String taskId = record.getTaskId();
            logger.info("该语音文件已翻译，任务ID: {}", taskId);
            return CommonResult.data(new HashMap<String, Object>() {{
                put("data", taskId);
                put("msg", "该语音文件已翻译");
                put("code", 204);
            }}); // 返回已翻译的 taskId 和提示信息
        }

        String voiceUrl = record.getVoiceUrl(); // 获取 VOICE_URL
        if (voiceUrl == null) {
            throw new TranslateServiceException("未找到对应的语音文件URL", null);
        }
        // 调用提交任务的方法
        TranslateParam translateParam = new TranslateParam();
        translateParam.setVoiceUrl(voiceUrl);
        translateParam.setRealTimeReturn(true); // 根据需要选择是否需要实时返回

        // 提交任务并获取 taskId
        String taskId = submitTask(translateParam);

        // 保存 taskId 到数据库
        saveTaskIdToDatabase(insuVoiceId, taskId, record);

        return CommonResult.data(new HashMap<String, Object>() {{
            put("data", taskId);
            put("msg", "翻译并保存成功");
            put("code", 200);
        }});
    }

    /**
     * 提交语音转文字任务并返回任务 ID
     * @param translateParam 请求参数
     * @return 任务 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String submitTask(TranslateParam translateParam) {
        logger.info("开始提交任务...");

        HttpHeaders headers = buildHeaders();
        Map<String, Object> requestBody = buildSubmitRequestBody(translateParam);

        try {
            logRequest("提交任务", serviceUrl + "/submit", requestBody);
            String response = postRequest(serviceUrl + "/submit", headers, requestBody);
            return extractTaskIdFromResponse(response);
        } catch (Exception e) {
            logger.error("提交任务失败", e);
            throw new TranslateServiceException("提交任务失败", e);
        }
    }

    /**
     * 保存 taskId 到数据库
     * @param insuVoiceId 录音 ID
     * @param record 任务 ID
     */
    private void saveTaskIdToDatabase(Integer insuVoiceId, String taskId,InsuVoiceRecord record) {

        record.setInsuVoiceId(insuVoiceId);
        record.setTaskId(taskId);
        record.setIsTranslated(1);
        record.setTranslateTime(new Date());

        // 更新或插入 taskId 和  IS_TRANSLATED 到数据库
        boolean updated = insuVoiceRecordService.updateById(record);
        if (!updated) {
            throw new TranslateServiceException("保存 taskId 和更新 IS_TRANSLATED 字段失败", null);
        }
        logger.info("保存 taskId [{}] 并更新 IS_TRANSLATED 为 1 到数据库成功, 已记录时间", taskId);
    }

    /**
     * 查询任务结果
     * @param taskId 任务 ID
     * @return 任务结果
     */
    @Override
    public Map<String, Object> queryTaskResult(String taskId) {
        logger.info("开始查询任务...");

        HttpHeaders headers = buildHeaders();
        Map<String, Object> queryParams = buildQueryParams(taskId);

        try {
            logRequest("查询任务", serviceUrl + "/query", queryParams);
            String response = postRequest(serviceUrl + "/query", headers, queryParams);
            return parseQueryResponse(response);
        } catch (Exception e) {
            logger.error("查询任务失败", e);
            throw new TranslateServiceException("查询任务失败", e);
        }
    }

    /**
     * 构建 HTTP 请求头
     * @return 请求头对象
     */
    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer ;" + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * 构建提交任务的请求体
     * @param translateParam 请求参数
     * @return 请求体
     */
    private Map<String, Object> buildSubmitRequestBody(TranslateParam translateParam) {
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> app = buildCommonFields();
        requestBody.put("app", app);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("uid", uid);
        requestBody.put("user", userInfo);

        Map<String, Object> audioInfo = new HashMap<>();
        audioInfo.put("format", translateParam.getFileFormat());
        audioInfo.put("url", translateParam.getVoiceUrl());
        audioInfo.put("channel", 2);
        requestBody.put("audio", audioInfo);

        Map<String, String> additions = new HashMap<>();
        additions.put("with_speaker_info", "True");
        requestBody.put("additions", additions);

        return requestBody;
    }

    /**
     * 构建查询任务的参数
     * @param taskId 任务 ID
     * @return 查询参数
     */
    private Map<String, Object> buildQueryParams(String taskId) {
        Map<String, Object> queryParams = buildCommonFields();
        queryParams.put("id", taskId);
        return queryParams;
    }

    /**
     * 构建通用字段
     * @return 通用字段
     */
    private Map<String, Object> buildCommonFields() {
        Map<String, Object> commonFields = new HashMap<>();
        commonFields.put("appid", appId);
        commonFields.put("token", token);
        commonFields.put("cluster", cluster);
        return commonFields;
    }

    /**
     * 发送 POST 请求
     * @param url 请求 URL
     * @param headers 请求头
     * @param body 请求体
     * @return 响应字符串
     */
    private String postRequest(String url, HttpHeaders headers, Map<String, Object> body) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            logger.info("发送请求至: {}", url);
            return restTemplate.postForObject(url, entity, String.class);
        } catch (Exception e) {
            logger.error("POST 请求失败", e);
            throw new TranslateServiceException("POST 请求失败", e);
        }
    }

    /**
     * 从提交任务的响应中提取任务 ID
     * @param response 响应字符串
     * @return 任务 ID
     */
    private String extractTaskIdFromResponse(String response) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            Map<String, Object> resp = (Map<String, Object>) responseMap.get("resp");
            if (resp == null || !resp.containsKey("id")) {
                throw new TranslateServiceException("响应格式错误，未包含任务 ID", null);
            }
            return (String) resp.get("id");
        } catch (Exception e) {
            logger.error("解析任务 ID 失败", e);
            throw new TranslateServiceException("解析任务 ID 失败", e);
        }
    }

    /**
     * 解析查询任务的响应
     * @param response 响应字符串
     * @return 解析后的结果
     */
    private Map<String, Object> parseQueryResponse(String response) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            return (Map<String, Object>) responseMap.get("resp");
        } catch (Exception e) {
            logger.error("解析查询响应失败", e);
            throw new TranslateServiceException("解析查询响应失败", e);
        }
    }

    /**
     * 统一日志输出格式
     * @param action 操作名称
     * @param url 请求 URL
     * @param body 请求体
     */
    private void logRequest(String action, String url, Map<String, Object> body) {
        logger.info("[{}] 请求 URL: {}，请求体: {}", action, url, body);
    }

    /**
     * 自定义异常类
     */
    public static class TranslateServiceException extends RuntimeException {
        public TranslateServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 根据 INSU_VOICE_ID 获取 VOICE_URL
     *
     * @param insuVoiceId 录音 ID
     * @return 录音文件的 URL
     */
    @Override
    public InsuVoiceRecord getByInsuVoiceId(Integer insuVoiceId) {
        // 查询对应的录音记录
        QueryWrapper<InsuVoiceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("INSU_VOICE_ID", insuVoiceId);

        InsuVoiceRecord insuVoiceRecord = insuVoiceRecordService.getOne(queryWrapper);

        if (insuVoiceRecord == null) {
            throw new CommonException("找不到对应的录音记录，INSU_VOICE_ID: " + insuVoiceId);
        }

//        logger.info(insuVoiceRecord.toString());
        // 返回完整的录音记录
        return insuVoiceRecord;
    }
}