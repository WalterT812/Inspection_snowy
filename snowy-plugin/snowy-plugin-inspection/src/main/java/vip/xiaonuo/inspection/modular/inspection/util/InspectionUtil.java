package vip.xiaonuo.inspection.modular.inspection.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import vip.xiaonuo.inspection.modular.inspection.dto.AuditResult;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检工具类
 */
@Slf4j
public class InspectionUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 格式化对话内容
     * @param dialogs 对话记录列表
     * @return 格式化后的对话内容
     */
    public static String formatDialogContent(List<InsuVoiceDialog> dialogs) {
        return dialogs.stream()
            .map(dialog -> dialog.getRole() + ": " + dialog.getDialogText())
            .collect(Collectors.joining("|"));
    }

    /**
     * 解析API响应
     * @param response API响应字符串
     * @return 质检结果对象
     */
    public static AuditResult parseApiResponse(String response) {
        try {
            return objectMapper.readValue(response, AuditResult.class);
        } catch (Exception e) {
            log.error("解析API响应失败", e);
            throw new RuntimeException("解析API响应失败", e);
        }
    }

    /**
     * 验证质检结果
     * @param result 质检结果
     */
    public static void validateInspectionResult(AuditResult result) {
        if (result == null || result.getAuditResults() == null || result.getAuditResults().isEmpty()) {
            throw new RuntimeException("无效的质检结果");
        }
        
        // 检查每个话者的审核结果
        result.getAuditResults().forEach(speakerAudit -> {
            if (speakerAudit.getViolations() == null || speakerAudit.getAuditSummary() == null) {
                throw new RuntimeException("质检结果格式不完整");
            }
        });
    }
} 