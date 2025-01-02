package vip.xiaonuo.inspection.modular.translate.service.impl;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.xiaonuo.inspection.core.client.Ark;
import vip.xiaonuo.inspection.core.config.LlmApiConfig;
import vip.xiaonuo.inspection.core.util.LoggerUtil;
import vip.xiaonuo.inspection.modular.translate.dto.Utterance;
import vip.xiaonuo.inspection.modular.translate.service.RoleIdentificationService;

import java.util.*;

@Service
public class RoleIdentificationServiceImpl implements RoleIdentificationService {

    @Autowired
    private LlmApiConfig llmApiConfig;

    @Override
    public String identifyStaffSpeaker(List<Utterance> utterances) {
        if (utterances == null || utterances.isEmpty()) {
            return null;
        }

        try {
            // 只取前3轮对话进行分析
            StringBuilder dialogContent = new StringBuilder();
            int count = 0;
            for (Utterance utterance : utterances) {
                if (count >= 3) break;
                dialogContent.append(String.format("Speaker %s: %s\n", 
                    utterance.getAdditions().getSpeaker(), utterance.getText()));
                count++;
            }

            String response = callLlmApi(dialogContent.toString());
            return extractSpeakerNumber(response);
        } catch (Exception e) {
            LoggerUtil.handleException("角色识别失败", e);
            return null;
        }
    }

    private String callLlmApi(String dialogContent) {
        Ark client = new Ark(
                llmApiConfig.getApiKey(),
                String.format("%s/%s", llmApiConfig.getBaseUrl(), llmApiConfig.getApiVersion()),
                llmApiConfig.getTimeout(),
                llmApiConfig.getMaxRetries());
        
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("system", 
                "你是保险对话分析专家。分析对话内容判断哪个说话者是坐席。" +
                "只需返回数字1或2，1表示Speaker 1是坐席，2表示Speaker 2是坐席。"));
            messages.add(createMessage("user", dialogContent));
            
            return client.chat.completions.create(
                    llmApiConfig.getModel(),
                    messages,
                    false
            );
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
    }

    private String extractSpeakerNumber(String response) {
        try {
            // 直接返回响应内容，因为我们只需要数字1或2
            String content = response.trim();
            // 验证返回值是否为1或2
            if (content.equals("1") || content.equals("2")) {
                return content;
            }
            LoggerUtil.handleException("无效的角色识别结果: " + content, null);
            return null;
        } catch (Exception e) {
            LoggerUtil.handleException("解析响应失败", e);
            return null;
        }
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
} 