package vip.xiaonuo.inspection.modular.inspection.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import vip.xiaonuo.common.exception.CommonException;
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
            .map(dialog -> String.format(
                "[%s] %s: %s",
                dialog.getDialogTextId(),
                dialog.getRole(),
                dialog.getDialogText()
            ))
            .collect(Collectors.joining("|"));
    }

    /**
     * 解析API响应
     * @param response API响应字符串
     * @return 质检结果对象
     */
    public static AuditResult parseApiResponse(String response) {
        try {
            log.info("API原始响应: {}", response);
            return objectMapper.readValue(response, AuditResult.class);
        } catch (Exception e) {
            log.error("解析API响应失败", e);
            throw new RuntimeException("解析API响应失败: " + e.getMessage());
        }
    }

    /**
     * 验证质检结果
     * @param result 质检结果
     */
    public static void validateInspectionResult(AuditResult result) {
        if (result == null || result.getAuditResults() == null || result.getAuditResults().isEmpty()) {
            throw new CommonException("无效的质检结果");
        }
        
        result.getAuditResults().forEach(speakerAudit -> {
            if (speakerAudit.getViolations() == null || speakerAudit.getAuditSummary() == null) {
                throw new CommonException("质检结果格式不完整");
            }
            
            // 验证角色信息
            if (speakerAudit.getRole() == null || 
                (speakerAudit.getRole() != 1 && speakerAudit.getRole() != 2)) {
                throw new CommonException("无效的角色信息");
            }
            
            // 验证违规证据
            speakerAudit.getViolations().forEach(violation -> {
                if (violation.getEvidence() != null) {
                    violation.getEvidence().forEach(evidence -> {
                        if (evidence.getRole() == null || 
                            (evidence.getRole() != 1 && evidence.getRole() != 2)) {
                            throw new CommonException("无效的证据角色信息");
                        }
                    });
                }
            });
        });
    }

    public static class RoleInfo {
        private Integer staffRole;  // 坐席角色ID（1或2）
        private Integer customerRole;  // 客户角色ID（1或2）
        
        public RoleInfo(AuditResult result) {
            if (!result.getAuditResults().isEmpty()) {
                this.staffRole = result.getAuditResults().get(0).getRole();
                this.customerRole = (staffRole == 1) ? 2 : 1;
            }
        }
        
        public Integer getStaffRole() {
            return staffRole;
        }
        
        public Integer getCustomerRole() {
            return customerRole;
        }
        
        public boolean isStaff(Integer role) {
            return role != null && role.equals(staffRole);
        }
        
        public boolean isCustomer(Integer role) {
            return role != null && role.equals(customerRole);
        }
        
        public String getRoleText(Integer role) {
            if (isStaff(role)) {
                return "坐席";
            } else if (isCustomer(role)) {
                return "客户";
            }
            return "未知角色";
        }
    }
} 