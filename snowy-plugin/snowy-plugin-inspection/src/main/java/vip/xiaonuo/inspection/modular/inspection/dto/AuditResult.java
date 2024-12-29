package vip.xiaonuo.inspection.modular.inspection.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AuditResult {
    private List<SpeakerAudit> auditResults;

    @Data
    public static class SpeakerAudit {
        private String speakerId;
        private List<Violation> violations;
        private AuditSummary auditSummary;
    }

    @Data
    public static class Violation {
        private String rule;
        private String message;
        private String describe;
    }

    @Data
    public static class AuditSummary {
        private Integer totalStatementsAudited;
        private Integer totalViolationsFound;
        private Map<String, Integer> violationsByRule;
    }
} 