package vip.xiaonuo.inspection.modular.translate.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.translate.DTO.SubmitTaskRequestBody;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceQueryResult;
import vip.xiaonuo.inspection.modular.translate.mapper.InsuVoiceQueryResultMapper;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;
import vip.xiaonuo.inspection.modular.translate.service.TranslateService;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.mapper.InsuVoiceRecordMapper;
import vip.xiaonuo.inspection.modular.voiceRecord.service.InsuVoiceRecordService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 语音转文字 Service 实现类
 * 提供语音任务的提交和查询功能
 * @Author tanghaoyu
 * @Date 2024/12/12
 */
@Service
public class TranslateServiceImpl extends ServiceImpl<InsuVoiceRecordMapper, InsuVoiceRecord> implements TranslateService {

    private static final Logger logger = LoggerFactory.getLogger(TranslateServiceImpl.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private InsuVoiceRecordMapper insuVoiceRecordMapper;
    @Autowired
    private InsuVoiceQueryResultMapper insuVoiceQueryResultMapper;
    private static InsuVoiceRecordService insuVoiceRecordService;
    public TranslateServiceImpl(InsuVoiceRecordService insuVoiceRecordService) {
        TranslateServiceImpl.insuVoiceRecordService = insuVoiceRecordService;
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
        Map<String,Object> requestBody = SubmitTaskRequestBody.build(translateParam, appId, token, cluster, uid).toMap();

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
     * @param insuVoiceId 任务 ID
     * @return 任务结果
     */
    @Override
    public Map<String, Object> queryTaskResult(Integer insuVoiceId) {
        // 先检查是否已保存过结果
        if (isResultAlreadySaved(insuVoiceId)) {
            logger.info("翻译结果已保存过，直接从数据库获取并返回");
            InsuVoiceQueryResult savedResult = getSavedResult(insuVoiceId);
            if (savedResult!= null) {
                return JSONUtil.parseObj(savedResult.getQueryResult());
            }
        }

        logger.info("开始根据 INSU_VOICE_ID 查询任务...");
        // 首先根据 INSU_VOICE_ID 查询对应的 TASK_ID
        String taskId = getTaskIdByInsuVoiceId(insuVoiceId);
        if (taskId == null) {
            logger.error("根据 INSU_VOICE_ID: {} 未找到对应的 TASK_ID", insuVoiceId);
        }

        HttpHeaders headers = buildHeaders();
        Map<String, Object> queryParams = buildQueryParams(taskId);

        try {
            logRequest("查询任务", serviceUrl + "/query", queryParams);
            String response = postRequest(serviceUrl + "/query", headers, queryParams);
            Map<String, Object> resultMap = parseQueryResponse(response);
            Map<String, Object> processedResult = processQueryResult(resultMap);
            String jsonResultMap  = JSONUtil.toJsonStr(processedResult);
            // 调用保存数据到 insu_voice_query_result 表的方法
            logger.info("开始保存");
            saveQueryResultToNewTable(insuVoiceId, taskId, jsonResultMap);

            return processedResult;
        } catch (Exception e) {
            logger.error("查询任务失败", e);
            throw new TranslateServiceException("查询任务失败", e);
        }
    }

    /**
     * 处理查询结果，提取utterances中的内容，并去掉words的内容，同时调整时间字段
     * @param resultMap 原始查询结果的Map对象
     * @return 处理后的Map对象，只包含utterances中的text字段，并且调整了时间字段
     */
    private Map<String, Object> processQueryResult(Map<String, Object> resultMap) {
        Map<String, Object> processedResult = new HashMap<>();
        List<Map<String, Object>> utterancesList = new ArrayList<>();
        Integer firstStartTime = null; // 用于记录第一个utterance的start_time

        // 提取utterances中的内容，移除words，并调整时间
        if (resultMap.containsKey("utterances")) {
            List<Map<String, Object>> utterances = (List<Map<String, Object>>) resultMap.get("utterances");
            for (Map<String, Object> utterance : utterances) {
                Map<String, Object> newUtterance = new HashMap<>(utterance);
                // 移除words字段
                newUtterance.remove("words");

                // 将start_time和end_time除以1000并保留整数位
                if (newUtterance.containsKey("start_time")) {
                    int startTime = (Integer) newUtterance.get("start_time");
                    newUtterance.put("start_time", startTime / 1000);
                }
                if (newUtterance.containsKey("end_time")) {
                    int endTime = (Integer) newUtterance.get("end_time");
                    newUtterance.put("end_time", endTime / 1000);
                }

                // 检查是否是第一个utterance，如果是，则记录其start_time
                if (firstStartTime == null && newUtterance.containsKey("start_time")) {
                    firstStartTime = (Integer) newUtterance.get("start_time");
                }

                // 如果firstStartTime不为空，则调整时间
                if (firstStartTime != null) {
                    adjustTimeFieldsBasedOnFirstStartTime(newUtterance, firstStartTime);
                }

                utterancesList.add(newUtterance);
            }
            processedResult.put("utterances", utterancesList);
        }

        return processedResult;
    }

    /**
     * 根据第一个utterance的start_time调整其他utterance的时间字段
     */
    private void adjustTimeFieldsBasedOnFirstStartTime(Map<String, Object> utterance, Integer firstStartTime) {
        if (utterance.containsKey("start_time")) {
            utterance.put("start_time", (Integer) utterance.get("start_time") - firstStartTime.intValue() + 1);
        }
        if (utterance.containsKey("end_time")) {
            utterance.put("end_time", (Integer) utterance.get("end_time") - firstStartTime.intValue() + 1);
        }
    }

    /**
     * 检查是否已经保存过翻译结果
     * @param insuVoiceId 录音 ID
     * @return true表示已保存过，false表示未保存过
     */
    private boolean isResultAlreadySaved(Integer insuVoiceId) {
        QueryWrapper<InsuVoiceQueryResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("insu_voice_id", insuVoiceId);
        InsuVoiceQueryResult result = insuVoiceQueryResultMapper.selectOne(queryWrapper);
        return result!= null;
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResult<HashMap<String, Object>> isSaveQueryResult(Integer insuVoiceId) {
        boolean isSaved = isResultAlreadySaved(insuVoiceId);
        if (isSaved) {
            // 如果已保存，从数据库中获取保存的结果并返回给前端
            InsuVoiceQueryResult savedResult = getSavedResult(insuVoiceId);
            if (savedResult!= null) {
                Map<String, Object> resultMap = JSONUtil.parseObj(savedResult.getQueryResult());
                return CommonResult.data(new HashMap<String, Object>() {{
                    put("data", resultMap);
                    put("msg", "翻译结果已保存，直接返回");
                    put("code", 200);
                }});
            }
        }
        return CommonResult.data(new HashMap<String, Object>() {{
            put("data", null);
            put("msg", "尚未保存翻译结果");
            put("code", 204);
        }});
    }

    /**
     * 根据录音ID获取已保存的结果
     * @param insuVoiceId 录音 ID
     * @return 已保存的 InsuVoiceQueryResult对象，如果不存在则返回null
     */
    private InsuVoiceQueryResult getSavedResult(Integer insuVoiceId) {
        QueryWrapper<InsuVoiceQueryResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("insu_voice_id", insuVoiceId);
        return insuVoiceQueryResultMapper.selectOne(queryWrapper);
    }

    /**
     * 通过INSU_VOICE_ID获取TASK_ID
     * @param insuVoiceId
     * @return TaskId
     */
    private String getTaskIdByInsuVoiceId(Integer insuVoiceId) {
        QueryWrapper<InsuVoiceRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("INSU_VOICE_ID", insuVoiceId);
        InsuVoiceRecord record = insuVoiceRecordMapper.selectOne(wrapper);
        if (record!= null) {
            return record.getTaskId();
        }
        return null;
    }

    /**
     * 将数据存入表中
     * @param insuVoiceId
     * @param taskId
     * @param queryResult
     */
    private void saveQueryResultToNewTable(Integer insuVoiceId, String taskId, String queryResult) {
        InsuVoiceQueryResult result = new InsuVoiceQueryResult();
        result.setInsuVoiceId(insuVoiceId);
        result.setTaskId(taskId);
        result.setQueryResult(queryResult);
        logger.info("保存结果: {}", result.getQueryResult());
        insuVoiceQueryResultMapper.insert(result);
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
            throw new TranslateServiceException("找不到对应的录音记录，INSU_VOICE_ID: " + insuVoiceId, null);
        }

//        logger.info(insuVoiceRecord.toString());
        // 返回完整的录音记录
        return insuVoiceRecord;
    }
}