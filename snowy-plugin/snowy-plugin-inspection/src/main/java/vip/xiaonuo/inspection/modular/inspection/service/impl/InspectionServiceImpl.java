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

import vip.xiaonuo.inspection.modular.inspection.service.InspectionRuleService;
import vip.xiaonuo.inspection.modular.inspection.config.InspectionPromptTemplate;
import vip.xiaonuo.inspection.modular.inspection.client.Ark;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordPageParam;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import vip.xiaonuo.inspection.modular.voiceRecord.mapper.InsuVoiceRecordMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import vip.xiaonuo.common.page.CommonPageRequest;
import cn.hutool.core.util.StrUtil;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;

@Slf4j
@Service
public class InspectionServiceImpl extends ServiceImpl<InsuVoiceRecordMapper, InsuVoiceRecord> implements InspectionService {

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
            log.info("对话内容: {}", dialogContent);

            // 2. 调用质检API
            AuditResult auditResult = callInspectionApi(dialogContent);
            log.info("质检结果: {}", JSONUtil.toJsonStr(auditResult));

            // 暂时注释掉保存和更新状态的代码
            // saveInspectionResult(insuVoiceId, JSONUtil.toJsonStr(auditResult));
            // updateInspectionStatus(insuVoiceId);

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
        requestBody.put("model", inspectionApiConfig.getModel());
        List<Map<String, String>> messages = Arrays.asList(
                createMessage("system", fullPrompt),
                createMessage("user", dialogContent)
        );
        requestBody.put("messages", messages);

        // 创建 Ark 客户端
        Ark client = new Ark(
                inspectionApiConfig.getApiKey(),
                inspectionApiConfig.getBaseUrl(),
                inspectionApiConfig.getTimeout(),
                2  // max_retries
        );

        try {
            // 调用API并处理响应
            String response = client.chat.completions.create(
                    inspectionApiConfig.getModel(),
                    messages,
                    false  // stream
            );

            AuditResult result = InspectionUtil.parseApiResponse(response);
            InspectionUtil.validateInspectionResult(result);
            return result;
        } finally {
            // 确保关闭客户端
            client.shutdown();
        }
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }


    private void updateInspectionStatus(Integer insuVoiceId) {
        int updated = inspectionMapper.updateInspectionStatus(insuVoiceId, new Date());
        if (updated == 0) {
            throw new RuntimeException("更新质检状态失败");
        }
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