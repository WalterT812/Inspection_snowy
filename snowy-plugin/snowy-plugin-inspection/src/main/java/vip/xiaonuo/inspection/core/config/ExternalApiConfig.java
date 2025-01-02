package vip.xiaonuo.inspection.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author tanghaoyu
 * @date 2024/12/20
 * @description 外部api
 **/
@Data
@Component
@ConfigurationProperties(prefix = "translate")
public class ExternalApiConfig {
    private String appid;
    private String token;
    private String cluster;
    private String serviceUrl;
    private String uid;
}
