package vip.xiaonuo.inspection.modular.inspection.service.impl;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.inspection.core.config.LlmApiConfig;
import vip.xiaonuo.inspection.core.prompt.config.InspectionPromptConfig;
import vip.xiaonuo.inspection.modular.inspection.dto.AuditResult;
import vip.xiaonuo.inspection.modular.inspection.entity.InsuVoiceInspection;
import vip.xiaonuo.inspection.modular.inspection.mapper.InsuVoiceInspectionMapper;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionParam;
import vip.xiaonuo.inspection.modular.inspection.service.InspectionService;
import vip.xiaonuo.inspection.core.util.InspectionUtil;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;
import vip.xiaonuo.inspection.modular.translate.service.InsuVoiceDialogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import java.util.*;

import vip.xiaonuo.inspection.modular.inspection.service.InspectionRuleService;
import vip.xiaonuo.inspection.core.prompt.template.InspectionPromptTemplate;
import vip.xiaonuo.inspection.core.client.Ark;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordPageParam;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import vip.xiaonuo.inspection.modular.voiceRecord.mapper.InsuVoiceRecordMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import vip.xiaonuo.common.page.CommonPageRequest;
import cn.hutool.core.util.StrUtil;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.inspection.core.util.LoggerUtil;

@Slf4j
@Service
public class InspectionServiceImpl extends ServiceImpl<InsuVoiceRecordMapper, InsuVoiceRecord> implements InspectionService {

    @Autowired
    private LlmApiConfig llmApiConfig;

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
            String dialogContent = getDialogContent(insuVoiceId);
            AuditResult auditResult = callInspectionApi(dialogContent);
            
            // 添加结果验证
            InspectionUtil.validateInspectionResult(auditResult);
            
            // 记录角色信息
            InspectionUtil.RoleInfo roleInfo = new InspectionUtil.RoleInfo(auditResult);
            log.info("角色判断结果 - 坐席ID: {}, 客户ID: {}", 
                roleInfo.getStaffRole(), roleInfo.getCustomerRole());
            
            // 保存结果
            saveInspectionResult(insuVoiceId, JSONUtil.toJsonStr(auditResult));
            
            return auditResult;
        } catch (Exception e) {
            log.error("质检任务执行失败", e);
            throw new CommonException("质检任务执行失败: " + e.getMessage());
        }
    }

    @Override
    public void saveInspectionResult(Integer insuVoiceId, String result) {
        log.info("开始保存质检结果，录音ID: {}", insuVoiceId);
        try {
            // 1. 保存质检结果到 INSU_VOICE_INSPECTION 表
            InsuVoiceInspection inspection = new InsuVoiceInspection();
            inspection.setInsuVoiceId(insuVoiceId);
            inspection.setInspectionResult(result);
            inspection.setInspectionTime(new Date());
            inspection.setInspectionStatus(1);
            inspectionMapper.insert(inspection);
            log.info("质检结果保存成功");

            // 2. 更新 INSU_VOICE_RECORD 表的质检状态和时间
            InsuVoiceRecord record = new InsuVoiceRecord();
            record.setInsuVoiceId(insuVoiceId);
            record.setIsInspected(1);
            record.setInspectionTime(new Date());
            
            UpdateWrapper<InsuVoiceRecord> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("INSU_VOICE_ID", insuVoiceId);
            
            if (!this.update(record, updateWrapper)) {
                throw new CommonException("更新质检状态失败");
            }
            log.info("质检状态更新成功");
        } catch (Exception e) {
            log.error("保存质检结果失败", e);
            throw new CommonException("保存质检结果失败: " + e.getMessage());
        }
    }

    private String getDialogContent(Integer insuVoiceId) {
        List<InsuVoiceDialog> dialogs = dialogService.list(
                new QueryWrapper<InsuVoiceDialog>()
                        .eq("INSU_VOICE_ID", insuVoiceId)
                        .orderByAsc("START_TIME")
        );
        return InspectionUtil.formatDialogContent(dialogs);
    }

    private AuditResult callInspectionApi(String dialogContent) {
        // 获取规则提示语
        String rulePrompt = inspectionRuleService.buildRulePrompt();

        // 构建完整的系统提示语
        String fullPrompt = String.format(
                InspectionPromptTemplate.PROMPT_TEMPLATE,
                promptConfig.getSystemRole(),
                rulePrompt,
                promptConfig.getJsonTemplate()
        );

        // 构建API请求参数
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", llmApiConfig.getModel());
        requestBody.put("temperature", llmApiConfig.getTemperature());
        requestBody.put("max_tokens", llmApiConfig.getMaxTokens());
        requestBody.put("presence_penalty", llmApiConfig.getPresencePenalty());
        requestBody.put("frequency_penalty", llmApiConfig.getFrequencyPenalty());
        requestBody.put("n", llmApiConfig.getN());
        requestBody.put("stream", llmApiConfig.getStream());

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", fullPrompt));
        messages.add(createMessage("user", dialogContent));
        requestBody.put("messages", messages);

        // 创建 Ark 客户端
        Ark client = new Ark(
                llmApiConfig.getApiKey(),
                String.format("%s/%s", llmApiConfig.getBaseUrl(), llmApiConfig.getApiVersion()),
                llmApiConfig.getTimeout(),
                llmApiConfig.getMaxRetries()
        );

        try {
            LoggerUtil.logRequest("LLM API", "Request Body", requestBody);
            
            // 调用API并处理响应
            String response = client.chat.completions.create(
                    llmApiConfig.getModel(),
                    messages,
                    llmApiConfig.getStream()
            );
            
            LoggerUtil.logRequest("LLM API", "Response", response);

            AuditResult result = InspectionUtil.parseApiResponse(response);
            InspectionUtil.validateInspectionResult(result);
            return result;
        } catch (Exception e) {
            LoggerUtil.handleException("调用大模型API失败", e);
            return null;
        } finally {
            client.shutdown();
        }
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    @Override
    public Page<InsuVoiceRecord> page(InsuVoiceRecordPageParam param) {
        QueryWrapper<InsuVoiceRecord> queryWrapper = new QueryWrapper<InsuVoiceRecord>().checkSqlInjection();
        
        // 只查询必要的字段
        queryWrapper.select("INSU_VOICE_ID", "IS_INSPECTED", "INSPECTION_TIME", "IS_TRANSLATED");
        
        // 检查是否有排序字段和排序顺序
        if (ObjectUtil.isAllNotEmpty(param.getSortField(), param.getSortOrder())) {
            CommonSortOrderEnum.validate(param.getSortOrder());
            queryWrapper.orderBy(true, param.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(param.getSortField()));
        } else {
            // 默认按照录音ID倒序排序
            queryWrapper.orderByDesc("INSU_VOICE_ID");
        }
        
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }
} 