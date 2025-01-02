package vip.xiaonuo.inspection.core.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/**
 * @author tanghaoyu
 * @date 2024/12/31
 * @description Http请求类工具
 **/
@Slf4j
public class HttpUtil {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 使用 RestTemplate 发送 POST 请求
     */
    public static String postWithRestTemplate(String url, HttpHeaders headers, Object requestBody) throws Exception {
        try {
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // 使用 LoggerUtil 记录请求信息
            LoggerUtil.logRequest("HTTP POST", "URL", url);
            LoggerUtil.logRequest("HTTP POST", "Request Body", requestBodyJson);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();

            // 记录响应信息
            LoggerUtil.logRequest("HTTP POST", "Response", responseBody);
            return responseBody;
        } catch (Exception e) {
            LoggerUtil.handleException("HTTP请求失败", e);
            return null;
        }
    }

    /**
     * 使用 Hutool.HttpUtil 发送 POST 请求
     */
    public static String post(String url, String body, Map<String, String> headers, int timeout) {
        try {
            // 记录请求信息
            LoggerUtil.logRequest("HTTP POST", "URL", url);
            LoggerUtil.logRequest("HTTP POST", "Request Body", body);

            cn.hutool.http.HttpRequest request = HttpRequest.post(url)
                    .body(body)
                    .timeout(timeout);

            if (headers != null) {
                headers.forEach(request::header);
            }

            // 发送请求
            HttpResponse response = request.execute();

            // 检查响应状态
            if (response.isOk()) {
                String responseBody = response.body();
                LoggerUtil.logRequest("HTTP POST", "Response", responseBody);
                return responseBody;
            } else {
                LoggerUtil.handleException(
                        String.format("HTTP请求失败: 状态码=%d, 响应=%s",
                                response.getStatus(),
                                response.body()),
                        null
                );
                return null;
            }
        } catch (Exception e) {
            LoggerUtil.handleException("HTTP请求异常", e);
            return null;
        }
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
        headers.set("Authorization", "Bearer ;" + token);
        return headers;
    }
}
