package vip.xiaonuo.inspection.modular.inspection.client;

import com.volcengine.ark.runtime.model.completion.chat.*;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.Getter;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class Ark {
    private final ArkService service;
    
    @Getter
    public final Chat chat;
    
    public Ark(String apiKey, String baseUrl, int timeout, int maxRetries) {
        this.service = ArkService.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .build();
        this.chat = new Chat(this);
    }
    
    public class Chat {
        public final Completions completions;
        
        private Chat(Ark ark) {
            this.completions = new Completions(ark);
        }
        
        public class Completions {
            private final Ark ark;
            
            private Completions(Ark ark) {
                this.ark = ark;
            }
            
            public String create(String model, List<Map<String, String>> messages, boolean stream) {
                List<ChatMessage> chatMessages = new ArrayList<>();
                
                // 转换消息格式
                for (Map<String, String> message : messages) {
                    chatMessages.add(ChatMessage.builder()
                        .role(ChatMessageRole.valueOf(message.get("role").toUpperCase()))
                        .content(message.get("content"))
                        .build());
                }
                
                // 构建请求
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(chatMessages)
                    .stream(stream)
                    .build();
                
                // 使用 StringBuilder 收集流式响应
                StringBuilder responseBuilder = new StringBuilder();
                
                // 发送流式请求
                ark.service.streamChatCompletion(request)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(
                        choice -> {
                            if (!choice.getChoices().isEmpty()) {
                                ChatMessage message = choice.getChoices().get(0).getMessage();
                                if (message != null && message.getContent() != null) {
                                    responseBuilder.append(message.getContent());
                                }
                            }
                        }
                    );
                
                return responseBuilder.toString();
            }
        }
    }
    
    public void shutdown() {
        if (service != null) {
            service.shutdownExecutor();
        }
    }
} 