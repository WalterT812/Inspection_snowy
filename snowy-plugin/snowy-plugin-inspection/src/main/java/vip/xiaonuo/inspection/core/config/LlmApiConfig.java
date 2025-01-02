package vip.xiaonuo.inspection.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型 API 配置类
 *
 * @author tanghaoyu
 * @date 2024/1/8
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmApiConfig {
    /** API密钥 */
    private String apiKey;

    /** API基础URL */
    private String baseUrl = "https://ark.volces.com";

    /** API版本 */
    private String apiVersion = "v3";

    /** 使用的模型名称 */
    private String model;

    /** 请求超时时间（秒） */
    private Integer timeout = 60;

    /** 最大重试次数 */
    private Integer maxRetries = 2;

    /** 是否启用流式响应 */
    private Boolean stream = false;

    /** 采样温度，控制输出的随机性 [0-2] */
    private Double temperature = 0.7;

    /** 每个对话最大上下文长度 */
    private Integer maxContextLength = 4096;

    /** 返回的候选结果数 */
    private Integer n = 1;

    /** 惩罚性质的重复token */
    private Double presencePenalty = 0.0;

    /** 频率惩罚度 */
    private Double frequencyPenalty = 0.0;

    /** 返回的最大Token数 */
    private Integer maxTokens = 2048;

    /** 是否返回Token数 */
    private Boolean returnTokenCount = false;

    /** 是否返回搜索信息 */
    private Boolean returnSearchInfo = false;
}