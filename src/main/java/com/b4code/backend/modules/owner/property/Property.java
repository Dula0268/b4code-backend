package com.b4code.backend.modules.owner.property;

/**
 * Owner Property Module
 * ─────────────────────
 *
 * Frontend pages:
 *   - owner/(Property)/properties/page.tsx                 — property listing
 *   - owner/(Property)/properties/createNewProperty        — create new property
 *   - owner/(Property)/properties/editPropertyDetails      — edit property
 *   - owner/(Property)/properties/propertyDetails          — view property details
 *   - owner/(Property)/properties/propertyRoomInventry     — room inventory for a property
 *   - owner/(Property)/properties/Media                    — property media/photos
 *
 * API Endpoints (planned):
 *   GET    /api/owner/properties                    — list owner's properties
 *   GET    /api/owner/properties/{id}               — get property details
 *   POST   /api/owner/properties                    — create new property
 *   PUT    /api/owner/properties/{id}               — update property details
 *   DELETE /api/owner/properties/{id}               — delete property
 *   GET    /api/owner/properties/{id}/rooms         — get rooms for a property
 *   POST   /api/owner/properties/{id}/media         — upload property media
 *   GET    /api/owner/properties/{id}/media         — get property media
 *   DELETE /api/owner/properties/{id}/media/{mediaId} — delete media
 *
 * Implementation layers:
 *   - Controller: com.b4code.backend.modules.owner.controller.PropertyController
 *   - Service:    com.b4code.backend.modules.owner.service.PropertyService
 *   - DTO:        com.b4code.backend.modules.owner.dto.PropertyResponse
 *   - Entity:     com.b4code.backend.modules.owner.entity (OwnerProperty, PropertyMedia)
 *   - Repository: com.b4code.backend.modules.owner.repository (OwnerPropertyRepository)
 */
public class Property {

}
