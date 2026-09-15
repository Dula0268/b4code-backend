package com.b4code.backend.service;

import com.b4code.backend.dao.InternalMessageRepository;
import com.b4code.backend.dao.PropertyRepository;
import com.b4code.backend.dao.UserRepository;
import com.b4code.backend.dto.InternalMessageDto;
import com.b4code.backend.exceptions.CustomException;
import com.b4code.backend.models.Property;
import com.b4code.backend.models.User;
import com.b4code.backend.models.messaging.InternalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.b4code.backend.dto.StaffConversationDto;
import com.b4code.backend.models.enums.UserRole;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.b4code.backend.repository.StaffQuickReplyRepository;
import com.b4code.backend.models.messaging.StaffQuickReply;
import com.b4code.backend.dto.StaffQuickReplyDto;

@Service
@RequiredArgsConstructor
public class InternalMessageService {

    private final InternalMessageRepository internalMessageRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StaffQuickReplyRepository staffQuickReplyRepository;

    public List<InternalMessageDto> getStaffOwnerMessages(String email) {
        User staff = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        if (staff.getPropertyId() == null) {
            throw new CustomException("Staff is not assigned to any property", HttpStatus.BAD_REQUEST);
        }

        Property property = propertyRepository.findById(staff.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));

        List<InternalMessage> messages = internalMessageRepository.findByPropertyIdOrderByCreatedAtAsc(property.getId());

        return messages.stream().map(msg -> mapToDto(msg)).collect(Collectors.toList());
    }

    public List<StaffQuickReplyDto> getStaffQuickReplies(String email) {
        User staff = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        if (staff.getPropertyId() == null) {
            throw new CustomException("Staff is not assigned to any property", HttpStatus.BAD_REQUEST);
        }

        List<StaffQuickReply> replies = staffQuickReplyRepository.findByPropertyIdAndIsActiveTrue(staff.getPropertyId());
        
        return replies.stream().map(reply -> StaffQuickReplyDto.builder()
                .id(reply.getId())
                .propertyId(reply.getProperty().getId())
                .name(reply.getName())
                .message(reply.getMessage())
                .isActive(reply.getIsActive())
                .createdAt(reply.getCreatedAt())
                .build()).collect(Collectors.toList());
    }

    public InternalMessageDto sendStaffMessageToOwner(String email, String content) {
        User staff = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        if (staff.getPropertyId() == null) {
            throw new CustomException("Staff is not assigned to any property", HttpStatus.BAD_REQUEST);
        }

        Property property = propertyRepository.findById(staff.getPropertyId())
                .orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND));

        if (property.getOwnerId() == null) {
            throw new CustomException("Property does not have an owner", HttpStatus.BAD_REQUEST);
        }

        InternalMessage message = InternalMessage.builder()
                .propertyId(property.getId())
                .senderId(staff.getId())
                .receiverId(property.getOwnerId())
                .content(content)
                .isRead(false)
                .build();

        InternalMessage savedMessage = internalMessageRepository.save(message);
        InternalMessageDto dto = mapToDto(savedMessage);
        messagingTemplate.convertAndSend("/topic/user/" + staff.getId() + "/internal-messages", dto);
        messagingTemplate.convertAndSend("/topic/user/" + property.getOwnerId() + "/internal-messages", dto);
        
        // Check for Auto-Reply Match
        List<StaffQuickReply> replies = staffQuickReplyRepository.findByPropertyIdAndIsActiveTrue(property.getId());
        for (StaffQuickReply reply : replies) {
            if (reply.getName() != null && reply.getName().equalsIgnoreCase(content.trim())) {
                InternalMessage autoReply = InternalMessage.builder()
                        .propertyId(property.getId())
                        .senderId(property.getOwnerId())
                        .receiverId(staff.getId())
                        .content(reply.getMessage())
                        .isRead(false)
                        .build();
                InternalMessage savedAutoReply = internalMessageRepository.save(autoReply);
                InternalMessageDto autoDto = mapToDto(savedAutoReply);
                messagingTemplate.convertAndSend("/topic/user/" + staff.getId() + "/internal-messages", autoDto);
                messagingTemplate.convertAndSend("/topic/user/" + property.getOwnerId() + "/internal-messages", autoDto);
                break;
            }
        }
        
        return dto;
    }

    public List<StaffConversationDto> getOwnerStaffConversations(String ownerEmail, Long propertyId) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        List<Property> properties = propertyId != null ? 
            List.of(propertyRepository.findById(propertyId).orElseThrow(() -> new CustomException("Property not found", HttpStatus.NOT_FOUND))) : 
            propertyRepository.findByOwnerId(owner.getId());

        List<StaffConversationDto> conversations = new ArrayList<>();
        
        for (Property property : properties) {
            List<User> staffMembers = userRepository.findByPropertyIdAndDeletedFalse(property.getId());
            List<InternalMessage> messages = internalMessageRepository.findByPropertyIdOrderByCreatedAtAsc(property.getId());
            
            for (User staff : staffMembers) {
                if (staff.getRole() != UserRole.STAFF) continue;
                
                InternalMessage latestMsg = null;
                int unreadCount = 0;
                
                for (InternalMessage msg : messages) {
                    boolean isBetween = (msg.getSenderId().equals(owner.getId()) && msg.getReceiverId().equals(staff.getId())) ||
                                        (msg.getSenderId().equals(staff.getId()) && msg.getReceiverId().equals(owner.getId()));
                    if (isBetween) {
                        latestMsg = msg; // since it's ordered by ASC, the last one will be the latest
                        if (!msg.isRead() && msg.getReceiverId().equals(owner.getId())) {
                            unreadCount++;
                        }
                    }
                }
                
                conversations.add(StaffConversationDto.builder()
                        .staffId(staff.getId())
                        .staffName(staff.getFullName())
                        .staffRole(staff.getStaffRole() != null ? staff.getStaffRole() : "Staff")
                        .propertyId(property.getId())
                        .propertyName(property.getName())
                        .latestMessageContent(latestMsg != null ? latestMsg.getContent() : null)
                        .latestMessageAt(latestMsg != null ? latestMsg.getCreatedAt() : null)
                        .unreadCount(unreadCount)
                        .build());
            }
        }
        
        // Sort by latest message date desc
        conversations.sort((a, b) -> {
            if (a.getLatestMessageAt() == null && b.getLatestMessageAt() == null) return 0;
            if (a.getLatestMessageAt() == null) return 1;
            if (b.getLatestMessageAt() == null) return -1;
            return b.getLatestMessageAt().compareTo(a.getLatestMessageAt());
        });
        
        return conversations;
    }

    public List<InternalMessageDto> getOwnerStaffMessages(String ownerEmail, Long propertyId, Long staffId) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));
        
        List<InternalMessage> allMessages = internalMessageRepository.findByPropertyIdOrderByCreatedAtAsc(propertyId);
        
        return allMessages.stream()
                .filter(msg -> (msg.getSenderId().equals(owner.getId()) && msg.getReceiverId().equals(staffId)) ||
                               (msg.getSenderId().equals(staffId) && msg.getReceiverId().equals(owner.getId())))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public InternalMessageDto sendOwnerMessageToStaff(String ownerEmail, Long propertyId, Long staffId, String content) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomException("Owner not found", HttpStatus.NOT_FOUND));

        InternalMessage message = InternalMessage.builder()
                .propertyId(propertyId)
                .senderId(owner.getId())
                .receiverId(staffId)
                .content(content)
                .isRead(false)
                .build();

        InternalMessage savedMessage = internalMessageRepository.save(message);
        InternalMessageDto dto = mapToDto(savedMessage);
        messagingTemplate.convertAndSend("/topic/user/" + owner.getId() + "/internal-messages", dto);
        messagingTemplate.convertAndSend("/topic/user/" + staffId + "/internal-messages", dto);
        return dto;
    }

    private InternalMessageDto mapToDto(InternalMessage msg) {
        User sender = userRepository.findById(msg.getSenderId()).orElse(null);
        String senderName = sender != null ? sender.getFullName() : "Unknown";

        return InternalMessageDto.builder()
                .id(msg.getId())
                .propertyId(msg.getPropertyId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .senderName(senderName)
                .content(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .isRead(msg.isRead())
                .build();
    }
}
