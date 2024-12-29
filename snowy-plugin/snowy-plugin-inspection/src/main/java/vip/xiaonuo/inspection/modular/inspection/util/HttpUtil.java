package vip.xiaonuo.inspection.modular.inspection.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class HttpUtil {
    
    /**
     * 发送POST请求
     * @param url 请求URL
     * @param body 请求体
     * @param headers 请求头
     * @param timeout 超时时间(毫秒)
     * @return 响应内容
     */
    public static String post(String url, String body, Map<String, String> headers, int timeout) {
        try {
            HttpRequest request = HttpRequest.post(url)
                .body(body)
                .timeout(timeout);
            
            // 添加请求头
            if (headers != null) {
                headers.forEach(request::header);
            }
            
            // 发送请求
            HttpResponse response = request.execute();
            
            // 检查响应状态
            if (response.isOk()) {
                return response.body();
            } else {
                log.error("HTTP请求失败, 状态码: {}, 响应内容: {}", response.getStatus(), response.body());
                throw new RuntimeException("HTTP请求失败: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("HTTP请求异常", e);
            throw new RuntimeException("HTTP请求异常: " + e.getMessage());
        }
    }
} 