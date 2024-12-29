package vip.xiaonuo.inspection.modular.inspection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检API配置类，用于读取application.properties中的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "inspection.api")
public class InspectionApiConfig {
    /** API密钥 */
    private String apiKey;
    
    /** API基础URL */
    private String baseUrl;
    
    /** 使用的模型名称 */
    private String model;
    
    /** 请求超时时间（秒） */
    private Integer timeout;
    
    /** 最大重试次数 */
    private Integer maxRetries;
} 