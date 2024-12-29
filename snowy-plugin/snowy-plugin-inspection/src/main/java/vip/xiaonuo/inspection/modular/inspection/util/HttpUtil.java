package vip.xiaonuo.inspection.modular.inspection.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description HTTP请求工具类
 */
public class HttpUtil {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * 发送POST请求
     * @param url 请求URL
     * @param requestBody 请求体
     * @return 响应内容
     */
    public static String post(String url, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        logger.info("发送请求到: {}", url);
        logger.debug("请求体: {}", requestBody);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String.class
        );
        
        String responseBody = response.getBody();
        logger.info("收到响应: {}", responseBody);
        
        return responseBody;
    }
} 