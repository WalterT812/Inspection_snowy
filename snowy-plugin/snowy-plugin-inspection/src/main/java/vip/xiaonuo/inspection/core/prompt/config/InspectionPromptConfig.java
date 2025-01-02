package vip.xiaonuo.inspection.core.prompt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import vip.xiaonuo.inspection.core.prompt.template.InspectionPromptTemplate;

/**
 * 质检提示语配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "inspection.prompt")
public class InspectionPromptConfig {
    /** 系统角色设定 */
    private String systemRole = InspectionPromptTemplate.SYSTEM_ROLE;
    
    /** JSON格式模板 */
    private String jsonTemplate = InspectionPromptTemplate.JSON_TEMPLATE;
} 