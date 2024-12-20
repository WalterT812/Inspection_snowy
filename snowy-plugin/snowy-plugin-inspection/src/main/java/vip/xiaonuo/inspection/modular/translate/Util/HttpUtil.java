package vip.xiaonuo.inspection.modular.translate.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

/**
 * @author tang
 * @date 2024/12/19
 * @description HTTP 请求工具类
 **/
public class HttpUtil {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送 POST 请求
     *
     * @param url         请求 URL
     * @param headers     请求头
     * @param requestBody 请求体对象
     * @return 响应体字符串
     * @throws Exception 发送请求失败时抛出异常
     */
    public static String postRequest(String url, HttpHeaders headers, Object requestBody) throws Exception {
        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        // 序列化请求体为 JSON 进行日志记录
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);
        logger.info("发送 POST 请求到 {}，请求体: {}", url, requestBodyJson);

        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // 记录响应体
        String responseBody = response.getBody();
        logger.info("收到响应: {}", responseBody);

        return responseBody;
    }

    /**
     * 构建请求头
     *
     * @param token Token
     * @return HttpHeaders 对象
     */
    public static HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 根据外部 API 的要求，设置授权头
        headers.set("Authorization", "Bearer ;" + token);
        return headers;
    }
}
