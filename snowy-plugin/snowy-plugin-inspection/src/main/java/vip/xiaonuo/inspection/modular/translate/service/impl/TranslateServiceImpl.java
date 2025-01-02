package vip.xiaonuo.inspection.modular.translate.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vip.xiaonuo.inspection.core.util.LoggerUtil;
import vip.xiaonuo.inspection.core.config.ExternalApiConfig;
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
import vip.xiaonuo.inspection.modular.translate.service.RoleIdentificationService;

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
    @Autowired
    private RoleIdentificationService roleIdentificationService;

    /**
     * 翻译并查询
     *
     * @param insuVoiceId
     * @return
     */
    @Override
    public QueryTaskResponse translate(Integer insuVoiceId) {
        InsuVoiceRecord record = translateDataService.getByInsuVoiceId(insuVoiceId);
        try {
            if (translateDataService.isResultAlreadySaved(insuVoiceId)) {
                LoggerUtil.logRequest("查询任务结果", "从数据库获取结果", insuVoiceId);
                InsuVoiceQueryResult savedResult = translateDataService.getSavedResult(insuVoiceId);
                if (savedResult != null) {
                    // 解析
                    Map<String, Object> resultMap = JSONUtil.parseObj(savedResult.getQueryResult());
                    QueryTaskResponse response = new QueryTaskResponse();
                    JSONArray utteranceArray = JSONUtil.parseArray(resultMap.get("utterances"));
                    List<Utterance> utterances = JSONUtil.toList(utteranceArray, Utterance.class);
                    response.setUtterances(utterances);
                    response.setCode(204);
                    response.setMsg("已查询");
                    return response;
                }
            }

            String voiceUrl = record.getVoiceUrl();
            if (voiceUrl == null) {
                LoggerUtil.handleException("未找到对应语音文件URL", null);
            }
            String taskId = record.getTaskId();
            if (taskId == null) {
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
                taskId = taskResponse.getResp().getId();

                // 保存 taskId 到数据库
                translateDataService.saveTaskId(insuVoiceId, taskId);
            }
            
            // 构建查询参数
            Map<String, Object> queryParams = taskService.buildQueryParams(taskId);
            // 查询任务
            String responseStr = taskService.queryTask(queryParams);
            QueryResponse response = taskService.parseQueryResponse(responseStr);

            if (response == null) {
                LoggerUtil.handleException("查询响应为空", null);
            }

            if (response != null && response.getResp().getCode() == 2000) {
                QueryTaskResponse queryTaskResponse = new QueryTaskResponse();
                queryTaskResponse.setMsg("翻译暂未完成");
                queryTaskResponse.setCode(205);
                return queryTaskResponse;
            }

            // 处理查询结果
            QueryTaskResponse processedResult = queryResultProcessor.processQueryResult(response);
            List<Utterance> utterances = processedResult.getUtterances();


            // 保存查询结果到数据库
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("utterances", utterances);
            String jsonResultMap = JSONUtil.toJsonStr(resultMap);
            translateDataService.saveQueryResult(insuVoiceId, taskId, jsonResultMap);

            // 更新翻译状态为已完成
            record.setIsTranslated(1);
            this.updateById(record);
            
            // 将 utterances 保存到数据库
            if (utterances != null && !utterances.isEmpty()) {
                List<InsuVoiceDialog> dialogRecords = new ArrayList<>();
                int dialogTextId = 1; // 从1开始

                for (Utterance utterance : utterances) {
                    InsuVoiceDialog DialogRecord = new InsuVoiceDialog();
                    DialogRecord.setInsuVoiceId(insuVoiceId);
                    DialogRecord.setDialogText(utterance.getText());
                    // 将 speaker 字段转换为 Integer
                    try {
                        DialogRecord.setRole(Integer.parseInt(utterance.getSpeaker()));
                    } catch (NumberFormatException e) {
                        LoggerUtil.handleException("无效的 speaker 值: " + utterance.getSpeaker(), e);
                        continue; // 跳过此条记录
                    }
                    DialogRecord.setStartTime(utterance.getStartTime());
                    DialogRecord.setEndTime(utterance.getEndTime());
                    DialogRecord.setDialogTextId(String.valueOf(dialogTextId++));
                    // createTime 和 updateTime 由数据库自动生成

                    dialogRecords.add(DialogRecord);
                }

                // 再保存新的对话记录
                insuVoiceDialogService.saveBatch(dialogRecords);
            }


            processedResult.setMsg("首次保存成功");
            processedResult.setCode(200);

            return processedResult;
        } catch (Exception e) {
            LoggerUtil.handleException("翻译失败", e);
            return null; // 根据需要可以返回其他默认值或抛出自定义异常
        }
    }
}


