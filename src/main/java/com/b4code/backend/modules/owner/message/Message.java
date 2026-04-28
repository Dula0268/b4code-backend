package com.b4code.backend.modules.owner.message;

/**
 * Owner Message Module
 * ────────────────────
 *
 * Frontend page: owner/(Entry & overview)/ownerDashboard/message/page.tsx
 *
 * API Endpoints (planned):
 *   GET    /api/owner/messages              — list conversations
 *   GET    /api/owner/messages/{id}         — get conversation detail
 *   POST   /api/owner/messages              — send a message
 *   PUT    /api/owner/messages/{id}/read    — mark as read
 *
 * Implementation layers:
 *   - Controller: com.b4code.backend.modules.owner.controller.MessageController
 *   - Service:    com.b4code.backend.modules.owner.service.MessageService
 *   - DTO:        com.b4code.backend.modules.owner.dto.MessageResponse
 *   - Entity:     com.b4code.backend.modules.owner.entity (Message-related)
 *   - Repository: com.b4code.backend.modules.owner.repository (Message-related)
 */
public class Message {

}
