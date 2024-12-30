package vip.xiaonuo.inspection.modular.inspection.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class AuditResult {
    @JsonProperty("audit_results")
    private List<SpeakerAudit> auditResults;
    
    @JsonProperty("overall_score")
    private Integer overallScore;
    
    @JsonProperty("inspection_summary")
    private String inspectionSummary;

    @Data
    public static class SpeakerAudit {
        @JsonProperty("speaker_id")
        private String speakerId;
        
        @JsonProperty("role")
        private Integer role;
        
        @JsonProperty("violations")
        private List<Violation> violations;
        
        @JsonProperty("performance_analysis")
        private PerformanceAnalysis performanceAnalysis;
        
        @JsonProperty("audit_summary")
        private AuditSummary auditSummary;
    }

    @Data
    public static class Violation {
        @JsonProperty("rule")
        private String rule;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("dialog_refs")
        private List<String> dialogRefs;
        
        @JsonProperty("evidence")
        private List<Evidence> evidence;
        
        @JsonProperty("suggestion")
        private String suggestion;
        
        @Data
        public static class Evidence {
            @JsonProperty("dialog_id")
            private String dialogId;
            
            @JsonProperty("role")
            private Integer role;
            
            @JsonProperty("content")
            private String content;
        }
    }

    @Data
    public static class PerformanceAnalysis {
        @JsonProperty("overall_comments")
        private String overallComments;
    }

    @Data
    public static class AuditSummary {
        @JsonProperty("total_statements")
        private Integer totalStatements;
        
        @JsonProperty("total_violations")
        private Integer totalViolations;
        
        @JsonProperty("violations_by_rule")
        private Map<String, Integer> violationsByRule;
    }
} 