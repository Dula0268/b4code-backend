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
public class BookingSseService {

    private final ConcurrentHashMap<Long, List<SseEmitter>> propertyEmitters = new ConcurrentHashMap<>();

    public SseEmitter addPropertyEmitter(Long propertyId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1 hour timeout

        propertyEmitters.computeIfAbsent(propertyId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.info("SSE emitter added for property bookings: {}", propertyId);
        
        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE connection established"));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE event for property bookings: {}", propertyId);
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

        log.info("Sending SSE event '{}' to {} emitter(s) for property bookings: {}", eventType, propEmitters.size(), propertyId);

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
