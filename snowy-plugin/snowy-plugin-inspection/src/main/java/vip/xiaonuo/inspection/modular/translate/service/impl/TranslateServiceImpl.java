package vip.xiaonuo.inspection.modular.translate.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.inspection.modular.translate.Util.LoggerUtil;
import vip.xiaonuo.inspection.modular.translate.config.ExternalApiConfig;
import vip.xiaonuo.inspection.modular.translate.dto.QueryResponse;
import vip.xiaonuo.inspection.modular.translate.dto.QueryTaskResponse;
import vip.xiaonuo.inspection.modular.translate.dto.SubmitTaskResponse;
import vip.xiaonuo.inspection.modular.translate.dto.Utterance;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceQueryResult;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;
import vip.xiaonuo.inspection.modular.translate.service.InsuVoiceDialogService;
import vip.xiaonuo.inspection.modular.translate.service.TranslateService;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.mapper.InsuVoiceRecordMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 语音转文字 Service 实现类
 * 提供语音任务的提交和查询功能
 *
 * @Author tanghaoyu
 * @Date 2024/12/12
 */
@Service
public class TranslateServiceImpl extends ServiceImpl<InsuVoiceRecordMapper, InsuVoiceRecord> implements TranslateService {

    @Autowired
    private TranslateDataServiceImpl translateDataService;
    @Autowired
    private TaskServiceImpl taskService;
    @Autowired
    private QueryResultProcessorImpl queryResultProcessor;
    @Autowired
    private InsuVoiceDialogService insuVoiceDialogService;
    @Autowired
    private ExternalApiConfig externalApiConfig;

    /**
     * 通过 INSU_VOICE_ID 查询 VOICE_URL 并提交语音转文字任务
     *
     * @param insuVoiceId 录音 ID
     * @return 任务 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitTaskResponse submitTaskByInsuVoiceId(Integer insuVoiceId) {

        // 获取录音记录
        InsuVoiceRecord record = translateDataService.getByInsuVoiceId(insuVoiceId);

        // 是否已翻译
        if (record.getIsTranslated() == 1) {
            String taskId = record.getTaskId();
            LoggerUtil.logRequest("检查翻译状态", "获取已翻译任务", taskId);
            SubmitTaskResponse response = new SubmitTaskResponse();
            SubmitTaskResponse.Resp resp = new SubmitTaskResponse.Resp();
            resp.setId(taskId);
            resp.setCode(204); // 204 已翻译
            resp.setMsg("该语音文件已翻译");
            response.setResp(resp);
            return response;
        }

        String voiceUrl = record.getVoiceUrl(); // 获取 VOICE_URL
        if (voiceUrl == null) {
            LoggerUtil.handleException("未找到对应的语音文件URL", null);
        }

        // 创建翻译参数并填充配置属性
        TranslateParam translateParam = new TranslateParam();
        translateParam.setAppid(externalApiConfig.getAppid());
        translateParam.setToken(externalApiConfig.getToken());
        translateParam.setCluster(externalApiConfig.getCluster());
        translateParam.setServiceUrl(externalApiConfig.getServiceUrl());
        translateParam.setUid(externalApiConfig.getUid());
        translateParam.setFileFormat("MP3"); // 或从数据库/配置中获取
        translateParam.setVoiceUrl(voiceUrl);
        translateParam.setRealTimeReturn(true); // 根据需要选择是否需要实时返回

        // 提交任务并获取响应
        SubmitTaskResponse taskResponse = taskService.submitTask(translateParam);
        String taskId = taskResponse.getResp().getId();

        // 保存 taskId 到数据库
        translateDataService.saveTaskId(insuVoiceId, taskId);

        // 创建响应对象
        SubmitTaskResponse response = new SubmitTaskResponse();
        SubmitTaskResponse.Resp resp = new SubmitTaskResponse.Resp();
        resp.setId(taskId);
        resp.setCode(200); // 200 成功
        resp.setMsg("翻译并保存成功");
        response.setResp(resp);
        return response;
    }

    /**
     * 查询任务结果
     *
     * @param insuVoiceId 任务 ID
     * @return 任务结果
     */
    @Override
    public QueryTaskResponse queryTaskResult(Integer insuVoiceId) {
        try {
            // 先检查是否已保存过结果
            if (translateDataService.isResultAlreadySaved(insuVoiceId)) {
                LoggerUtil.logRequest("查询任务结果", "从数据库获取结果", insuVoiceId);
                InsuVoiceQueryResult savedResult = translateDataService.getSavedResult(insuVoiceId);
                if (savedResult != null) {
                    // 解析 JSON 字符串为 Map
                    Map<String, Object> resultMap = JSONUtil.parseObj(savedResult.getQueryResult());
                    // 创建 QueryTaskResponse 对象
                    QueryTaskResponse response = new QueryTaskResponse();
                    JSONArray utteranceArray = JSONUtil.parseArray(resultMap.get("utterances"));
                    List<Utterance> utterances = JSONUtil.toList(utteranceArray, Utterance.class);
                    response.setUtterances(utterances);
                    response.setCode(204);
                    response.setMsg("已查询");
                    return response;
                }
            }

            LoggerUtil.logRequest("查询任务结果", "调用翻译服务查询", insuVoiceId);
            // 根据 INSU_VOICE_ID 获取 TASK_ID
            String taskId = translateDataService.getTaskIdByInsuVoiceId(insuVoiceId);
            if (taskId == null) {
                LoggerUtil.handleException("根据 INSU_VOICE_ID: " + insuVoiceId + " 未找到对应的 TASK_ID", null);
            }

            // 构建查询参数
            Map<String, Object> queryParams = taskService.buildQueryParams(taskId);
            // 查询任务
            String responseStr = taskService.queryTask(queryParams);
            QueryResponse response = taskService.parseQueryResponse(responseStr);

            if (response == null) {
                LoggerUtil.handleException("查询响应为空", null);
            }

            if (response != null && response.getResp().getCode() == 2000){
                QueryTaskResponse queryTaskResponse = new QueryTaskResponse();
                queryTaskResponse.setMsg("翻译暂未完成");
                queryTaskResponse.setCode(205);
                return queryTaskResponse;
            }
            // 处理查询结果
            QueryTaskResponse processedResult = queryResultProcessor.processQueryResult(response);

            // 保存查询结果到数据库
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("utterances", processedResult.getUtterances());
            String jsonResultMap = JSONUtil.toJsonStr(resultMap);
            translateDataService.saveQueryResult(insuVoiceId, taskId, jsonResultMap);

            // 将 utterances 保存到数据库
            List<Utterance> utterances = processedResult.getUtterances();
            if (utterances != null && !utterances.isEmpty()) {
                List<InsuVoiceDialog> dialogRecords = new ArrayList<>();
                int dialogTextId = 1; // 从1开始

                for (Utterance utterance : utterances) {
                    InsuVoiceDialog record = new InsuVoiceDialog();
                    record.setInsuVoiceId(insuVoiceId);
                    record.setDialogText(utterance.getText());
                    // 将 speaker 字段转换为 Integer
                    try {
                        record.setRole(Integer.parseInt(utterance.getSpeaker()));
                    } catch (NumberFormatException e) {
                        LoggerUtil.handleException("无效的 speaker 值: " + utterance.getSpeaker(), e);
                        continue; // 跳过此条记录
                    }
                    record.setStartTime(utterance.getStartTime());
                    record.setEndTime(utterance.getEndTime());
                    record.setDialogTextId(String.valueOf(dialogTextId++));
                    // createTime 和 updateTime 由数据库自动生成

                    dialogRecords.add(record);
                }

                // 批量保存
                insuVoiceDialogService.saveBatch(dialogRecords);
            }

            // 更新 insu_voice_record 的 IS_QUERY 字段为 1
            boolean updateResult = translateDataService.updateIsQueryByInsuVoiceId(insuVoiceId);
            if (!updateResult) {
                LoggerUtil.handleException("更新 IS_QUERIED 字段失败，insuVoiceId: " + insuVoiceId, null);
            }

            processedResult.setMsg("首次保存成功");
            processedResult.setCode(200);

            return processedResult;
        } catch (Exception e) {
            LoggerUtil.handleException("查询任务失败", e);
            return null; // 根据需要可以返回其他默认值或抛出自定义异常
        }
    }
}
