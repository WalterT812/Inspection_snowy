package vip.xiaonuo.inspection.modular.inspection.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检结果数据传输对象
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditResult {
    /** 说话人ID */
    private String speakerId;
    
    /** 违规列表 */
    private List<ViolationRule> violations;
    
    /** 质检总结 */
    private AuditSummary auditSummary;
} 