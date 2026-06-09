package com.flower.service;

import com.flower.config.AiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class AiAssistantService {

    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    private static final String SYSTEM_PROMPT = "你是「花卉销售系统」的AI导购助手，你需要:\n" +
            "1. 礼貌友好地回答用户关于花卉选购、花束搭配、送礼建议的问题\n" +
            "2. 系统提供以下商品类别：红玫瑰、白百合、粉郁金香、向日葵、康乃馨、尤加利叶、小雏菊等\n" +
            "3. 根据不同场合给出合适的搭配建议：情人节、生日、母亲节、毕业季、道歉等\n" +
            "4. 如果用户问价格，告诉用户可以在商城查看具体价格，或者进入DIY设计页面自己搭配计算\n" +
            "5. 如果用户问DIY花束功能，告诉用户可以在「DIY花束方案」页面自己设计花束，支持拖拽、旋转、自由摆放，保存后可以下单购买\n" +
            "6. 保持回答简洁明了，不要太长\n" +
            "7. 用户不说英语，全程用中文回答";

    public AiAssistantService(AiConfig aiConfig, ObjectMapper objectMapper) {
        this.aiConfig = aiConfig;
        this.objectMapper = objectMapper;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    public void streamChat(String userMessage, List<Map<String, String>> history, Consumer<String> onChunk, Runnable onComplete, Consumer<Throwable> onError) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            messages.addAll(history);
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> body = Map.of(
                    "model", aiConfig.getModel(),
                    "messages", messages,
                    "stream", true,
                    "temperature", 0.7
            );

            String json = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(aiConfig.getBaseUrl() + "/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            EventSource.Factory factory = EventSources.createFactory(client);
            factory.newEventSource(request, new EventSourceListener() {
                @Override
                public void onOpen(EventSource eventSource, Response response) {}

                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if ("[DONE]".equals(data)) {
                        onComplete.run();
                        eventSource.cancel();
                        return;
                    }
                    try {
                        JsonNode node = objectMapper.readTree(data);
                        JsonNode delta = node.path("choices").get(0).path("delta");
                        String content = delta.path("content").asText();
                        if (content != null && !content.isEmpty()) {
                            onChunk.accept(content);
                        }
                    } catch (Exception e) {
                        // ignore parse errors for incomplete chunks
                    }
                }

                @Override
                public void onClosed(EventSource eventSource) {
                    onComplete.run();
                }

                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    onError.accept(t);
                }
            });
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @FunctionalInterface
    public interface Consumer<T> {
        void accept(T t);
    }
}