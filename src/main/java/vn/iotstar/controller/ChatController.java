package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.dto.ChatMessageRequest;
import vn.iotstar.dto.ConversationDTO;
import vn.iotstar.dto.MessageDTO;
import vn.iotstar.entity.Conversation;
import vn.iotstar.entity.Message;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.ChatService;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // WebSocket message handler
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Conversation conversation;
        Message.SenderType senderType;
        
        // If conversationId is provided, get existing conversation
        if (request.getConversationId() != null && !request.getConversationId().isEmpty()) {
            conversation = chatService.getConversationById(request.getConversationId());
            
            // Determine sender type based on who is the owner of the store
            boolean isVendor = conversation.getStore().getOwner().getId().equals(user.getId());
            senderType = isVendor ? Message.SenderType.VENDOR : Message.SenderType.USER;
        } else {
            // Create new conversation - must have storeId
            if (request.getStoreId() == null || request.getStoreId().isEmpty()) {
                throw new RuntimeException("StoreId is required for new conversation");
            }
            
            conversation = chatService.getOrCreateConversation(user.getId(), request.getStoreId());
            
            // For new conversation from user/chat, sender is always USER
            boolean isVendor = conversation.getStore().getOwner().getId().equals(user.getId());
            senderType = isVendor ? Message.SenderType.VENDOR : Message.SenderType.USER;
        }
        
        // Save message
        Message message = chatService.sendMessage(
                user.getId(), 
                conversation.getId(), 
                request.getContent(), 
                request.getImageUrl(), 
                senderType
        );
        
        // Convert to DTO
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(message.getId());
        messageDTO.setConversationId(message.getConversation().getId());
        messageDTO.setSenderId(message.getSender().getId());
        messageDTO.setSenderName(message.getSender().getFullName());
        messageDTO.setSenderAvatar(message.getSender().getAvatar());
        messageDTO.setContent(message.getContent());
        messageDTO.setSenderType(message.getSenderType().toString());
        messageDTO.setIsRead(message.getIsRead());
        messageDTO.setImageUrl(message.getImageUrl());
        messageDTO.setCreatedAt(message.getCreatedAt());
        
        // Send to conversation topic
        messagingTemplate.convertAndSend("/topic/conversation." + conversation.getId(), messageDTO);
        
        // Notify the other party
        boolean isVendor = senderType == Message.SenderType.VENDOR;
        String recipientId = isVendor ? conversation.getUser().getId() : conversation.getStore().getOwner().getId();
        messagingTemplate.convertAndSendToUser(recipientId, "/queue/notifications", messageDTO);
    }
    
    // User chat page
    @GetMapping("/user/chat")
    public String userChatPage(Authentication authentication, Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<ConversationDTO> conversations = chatService.getUserConversations(user.getId());
        model.addAttribute("conversations", conversations);
        model.addAttribute("currentUser", user);
        
        return "user/chat";
    }
    
    // Vendor chat page
    @GetMapping("/vendor/chat")
    public String vendorChatPage(Authentication authentication, Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<ConversationDTO> conversations = chatService.getVendorConversations(user.getId());
        model.addAttribute("conversations", conversations);
        model.addAttribute("currentUser", user);
        
        return "vendor/chat";
    }
    
    // Get or create conversation
    @PostMapping("/api/chat/conversation")
    @ResponseBody
    public ConversationDTO getOrCreateConversation(@RequestParam String storeId, Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Conversation conversation = chatService.getOrCreateConversation(user.getId(), storeId);
            return chatService.getConversationDTO(conversation.getId(), user.getId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    // Get conversation messages
    @GetMapping("/api/chat/messages/{conversationId}")
    @ResponseBody
    public List<MessageDTO> getMessages(@PathVariable String conversationId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get conversation and determine reader type
        Conversation conversation = chatService.getConversationById(conversationId);
        boolean isVendor = conversation.getStore().getOwner().getId().equals(user.getId());
        Message.SenderType readerType = isVendor ? Message.SenderType.VENDOR : Message.SenderType.USER;
        
        // Mark messages as read
        chatService.markMessagesAsRead(conversationId, readerType);
        
        return chatService.getConversationMessages(conversationId);
    }
    
    // Get user conversations
    @GetMapping("/api/chat/conversations")
    @ResponseBody
    public List<ConversationDTO> getConversations(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() == User.UserRole.VENDOR) {
            return chatService.getVendorConversations(user.getId());
        } else {
            return chatService.getUserConversations(user.getId());
        }
    }
}