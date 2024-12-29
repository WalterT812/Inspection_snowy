package vip.xiaonuo.inspection.modular.inspection.dto;

import lombok.Data;
import java.util.Map;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检总结数据传输对象
 */
@Data
public class AuditSummary {
    /** 已审核语句总数 */
    private Integer totalStatementsAudited;
    
    /** 发现的违规总数 */
    private Integer totalViolationsFound;
    
    /** 各规则的违规统计 */
    private Map<String, Integer> violationsByRule;
} 