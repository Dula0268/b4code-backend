package com.b4code.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class OrderSseService {

    private final ConcurrentHashMap<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(Long orderId) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 5 minutes timeout

        emitters.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.info("SSE emitter added for order: {}", orderId);
        
        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE connection established"));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE event for order: {}", orderId);
        }

        emitter.onCompletion(() -> removeEmitter(orderId, emitter));
        emitter.onTimeout(() -> removeEmitter(orderId, emitter));
        emitter.onError(e -> removeEmitter(orderId, emitter));

        return emitter;
    }

    public void sendEvent(Long orderId, String eventType, Object data) {
        List<SseEmitter> orderEmitters = emitters.get(orderId);
        if (orderEmitters == null || orderEmitters.isEmpty()) {
            log.debug("No SSE emitters found for order: {}", orderId);
            return;
        }

        log.info("Sending SSE event '{}' to {} emitter(s) for order: {}", eventType, orderEmitters.size(), orderId);

        for (SseEmitter emitter : orderEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(data));
            } catch (IOException e) {
                log.warn("Failed to send SSE event to emitter for order: {}", orderId);
                removeEmitter(orderId, emitter);
            }
        }
    }

    public void removeEmitter(Long orderId, SseEmitter emitter) {
        List<SseEmitter> orderEmitters = emitters.get(orderId);
        if (orderEmitters != null) {
            orderEmitters.remove(emitter);
            if (orderEmitters.isEmpty()) {
                emitters.remove(orderId);
            }
            log.debug("SSE emitter removed for order: {}", orderId);
        }
    }

    private final ConcurrentHashMap<Long, List<SseEmitter>> propertyEmitters = new ConcurrentHashMap<>();

    public SseEmitter addPropertyEmitter(Long propertyId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1 hour timeout

        propertyEmitters.computeIfAbsent(propertyId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.info("SSE emitter added for property: {}", propertyId);
        
        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE connection established"));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE event for property: {}", propertyId);
        }

        emitter.onCompletion(() -> removePropertyEmitter(propertyId, emitter));
        emitter.onTimeout(() -> removePropertyEmitter(propertyId, emitter));
        emitter.onError(e -> removePropertyEmitter(propertyId, emitter));

        return emitter;
    }

    public void sendPropertyEvent(Long propertyId, String eventType, Object data) {
        List<SseEmitter> propEmitters = propertyEmitters.get(propertyId);
        if (propEmitters == null || propEmitters.isEmpty()) {
            return;
        }

        log.info("Sending SSE event '{}' to {} emitter(s) for property: {}", eventType, propEmitters.size(), propertyId);

        for (SseEmitter emitter : propEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(data));
            } catch (IOException e) {
                removePropertyEmitter(propertyId, emitter);
            }
        }
    }

    private void removePropertyEmitter(Long propertyId, SseEmitter emitter) {
        List<SseEmitter> propEmitters = propertyEmitters.get(propertyId);
        if (propEmitters != null) {
            propEmitters.remove(emitter);
            if (propEmitters.isEmpty()) {
                propertyEmitters.remove(propertyId);
            }
        }
    }
}
