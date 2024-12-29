package vip.xiaonuo.inspection.modular.inspection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 质检提示语配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "inspection.prompt")
public class InspectionPromptConfig {
    /** 系统角色设定 */
    private String systemRole;
    
    /** JSON格式模板 */
    private String jsonTemplate;
    
    /** 质检规则提示 */
    private String rulePrompt;
} 