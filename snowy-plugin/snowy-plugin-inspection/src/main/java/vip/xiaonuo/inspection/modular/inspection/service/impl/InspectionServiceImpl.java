package vip.xiaonuo.inspection.modular.inspection.service.impl;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.inspection.modular.inspection.config.InspectionApiConfig;
import vip.xiaonuo.inspection.modular.inspection.config.InspectionPromptConfig;
import vip.xiaonuo.inspection.modular.inspection.dto.AuditResult;
import vip.xiaonuo.inspection.modular.inspection.entity.InsuVoiceInspection;
import vip.xiaonuo.inspection.modular.inspection.mapper.InsuVoiceInspectionMapper;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionParam;
import vip.xiaonuo.inspection.modular.inspection.service.InspectionService;
import vip.xiaonuo.inspection.modular.inspection.util.InspectionUtil;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;
import vip.xiaonuo.inspection.modular.translate.service.InsuVoiceDialogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.*;
import vip.xiaonuo.inspection.modular.inspection.util.HttpUtil;
import vip.xiaonuo.inspection.modular.inspection.service.InspectionRuleService;

@Slf4j
@Service
public class InspectionServiceImpl implements InspectionService {

    @Autowired
    private InspectionApiConfig inspectionApiConfig;

    @Autowired
    private InsuVoiceInspectionMapper inspectionMapper;

    @Autowired
    private InsuVoiceDialogService dialogService;

    @Autowired
    private InspectionPromptConfig promptConfig;

    @Autowired
    private InspectionRuleService inspectionRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditResult performInspection(InspectionParam param) {
        Integer insuVoiceId = param.getInsuVoiceId();
        log.info("开始质检任务，录音ID: {}", insuVoiceId);

        try {
            // 1. 获取对话内容
            String dialogContent = getDialogContent(insuVoiceId);
            
            // 2. 调用质检API
            AuditResult auditResult = callInspectionApi(dialogContent);
            
            // 3. 保存质检结果
            saveInspectionResult(insuVoiceId, JSONUtil.toJsonStr(auditResult));
            
            // 4. 更新质检状态
            updateInspectionStatus(insuVoiceId);
            
            return auditResult;
        } catch (Exception e) {
            log.error("质检任务执行失败", e);
            throw new RuntimeException("质检任务执行失败: " + e.getMessage());
        }
    }

    @Override
    public void saveInspectionResult(Integer insuVoiceId, String result) {
        InsuVoiceInspection inspection = new InsuVoiceInspection();
        inspection.setInsuVoiceId(insuVoiceId);
        inspection.setInspectionResult(result);
        inspection.setInspectionTime(new Date());
        inspection.setInspectionStatus(1);
        
        inspectionMapper.insert(inspection);
    }

    private String getDialogContent(Integer insuVoiceId) {
        // 从数据库获取对话内容并格式化
        List<InsuVoiceDialog> dialogs = dialogService.list(
            new QueryWrapper<InsuVoiceDialog>()
                .eq("INSU_VOICE_ID", insuVoiceId)
                .orderByAsc("START_TIME")
        );

        StringBuilder content = new StringBuilder();
        for (InsuVoiceDialog dialog : dialogs) {
            content.append(dialog.getRole()).append(": ")
                  .append(dialog.getDialogText())
                  .append("|");
        }
        return content.toString();
    }

    private AuditResult callInspectionApi(String dialogContent) {
        // 构建API请求参数
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", inspectionApiConfig.getModel());
        requestBody.put("messages", Arrays.asList(
            createMessage("system", buildSystemPrompt()),
            createMessage("user", dialogContent)
        ));

        // 调用API并处理响应
        String response = HttpUtil.post(inspectionApiConfig.getBaseUrl(), JSONUtil.toJsonStr(requestBody));
        return JSONUtil.toBean(response, AuditResult.class);
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * 构建完整的系统提示语
     */
    private String buildSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append(promptConfig.getSystemRole())
              .append("\n\n")
              .append("质检规则如下：\n")
              .append(inspectionRuleService.buildRulePrompt())
              .append("\n\n")
              .append("请按照以下JSON格式返回质检结果：\n")
              .append(promptConfig.getJsonTemplate());
        return prompt.toString();
    }

    private String getJsonTemplate() {
        return promptConfig.getJsonTemplate();
    }

    private String getRulePrompt() {
        return inspectionRuleService.buildRulePrompt();
    }

    private void updateInspectionStatus(Integer insuVoiceId) {
        int updated = inspectionMapper.updateInspectionStatus(insuVoiceId, new Date());
        if (updated == 0) {
            throw new RuntimeException("更新质检状态失败");
        }
    }
} 