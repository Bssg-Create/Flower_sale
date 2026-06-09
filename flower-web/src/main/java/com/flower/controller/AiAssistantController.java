package com.flower.controller;

import com.flower.base.ResponseResult;
import com.flower.service.AiAssistantService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(
            @RequestParam String message,
            @RequestParam(required = false) String historyJson) {
        SseEmitter emitter = new SseEmitter(60_000L);

        List<Map<String, String>> history = new ArrayList<>();

        aiAssistantService.streamChat(message, history,
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                () -> {
                    try {
                        emitter.send(SseEmitter.event().data("[DONE]"));
                    } catch (IOException ignore) {}
                    emitter.complete();
                },
                error -> {
                    try {
                        emitter.send(SseEmitter.event().data("[ERROR] " + error.getMessage()));
                    } catch (IOException ignore) {}
                    emitter.completeWithError(error);
                }
        );

        return emitter;
    }
}