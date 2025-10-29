package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.ConversationDTO;
import vn.iotstar.dto.MessageDTO;
import vn.iotstar.entity.Conversation;
import vn.iotstar.entity.Message;
import vn.iotstar.entity.Store;
import vn.iotstar.entity.User;
import vn.iotstar.repository.ConversationRepository;
import vn.iotstar.repository.MessageRepository;
import vn.iotstar.repository.StoreRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.ChatService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    
    @Override
    public Conversation getOrCreateConversation(String userId, String storeId) {
        // Check if conversation already exists
        return conversationRepository.findByUserIdAndStoreId(userId, storeId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Store store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new RuntimeException("Store not found"));
                    
                    // Validation: Prevent user from chatting with their own store
                    if (store.getOwner().getId().equals(userId)) {
                        throw new IllegalArgumentException("Bạn không thể nhắn tin đến cửa hàng của chính mình");
                    }
                    
                    Conversation conversation = new Conversation();
                    conversation.setUser(user);
                    conversation.setStore(store);
                    conversation.setLastMessageTime(LocalDateTime.now());
                    
                    return conversationRepository.save(conversation);
                });
    }
    
    @Override
    public Conversation getConversationById(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }
    
    @Override
    public Message sendMessage(String userId, String conversationId, String content, String imageUrl, Message.SenderType senderType) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setImageUrl(imageUrl);
        message.setSenderType(senderType);
        message.setIsRead(false);
        
        Message savedMessage = messageRepository.save(message);
        
        // Update conversation
        conversation.setLastMessage(content);
        conversation.setLastMessageTime(LocalDateTime.now());
        
        // Increment unread count for the receiver
        if (senderType == Message.SenderType.USER) {
            conversation.setUnreadCountVendor(conversation.getUnreadCountVendor() + 1);
        } else {
            conversation.setUnreadCountUser(conversation.getUnreadCountUser() + 1);
        }
        
        conversationRepository.save(conversation);
        
        return savedMessage;
    }
    
    @Override
    public List<MessageDTO> getConversationMessages(String conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        
        return messages.stream().map(this::convertToMessageDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<ConversationDTO> getUserConversations(String userId) {
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageTimeDesc(userId);
        
        return conversations.stream().map(conv -> {
            ConversationDTO dto = new ConversationDTO();
            dto.setId(conv.getId());
            dto.setUserId(conv.getUser().getId());
            dto.setUserName(conv.getUser().getFullName());
            dto.setUserAvatar(conv.getUser().getAvatar());
            dto.setStoreId(conv.getStore().getId());
            dto.setStoreName(conv.getStore().getName());
            dto.setStoreAvatar(conv.getStore().getFeaturedImages());
            dto.setLastMessage(conv.getLastMessage());
            dto.setLastMessageTime(conv.getLastMessageTime());
            dto.setUnreadCount(conv.getUnreadCountUser());
            dto.setCreatedAt(conv.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<ConversationDTO> getVendorConversations(String vendorId) {
        List<Conversation> conversations = conversationRepository.findByStore_Owner_IdOrderByLastMessageTimeDesc(vendorId);
        
        return conversations.stream().map(conv -> {
            ConversationDTO dto = new ConversationDTO();
            dto.setId(conv.getId());
            dto.setUserId(conv.getUser().getId());
            dto.setUserName(conv.getUser().getFullName());
            dto.setUserAvatar(conv.getUser().getAvatar());
            dto.setStoreId(conv.getStore().getId());
            dto.setStoreName(conv.getStore().getName());
            dto.setStoreAvatar(conv.getStore().getFeaturedImages());
            dto.setLastMessage(conv.getLastMessage());
            dto.setLastMessageTime(conv.getLastMessageTime());
            dto.setUnreadCount(conv.getUnreadCountVendor());
            dto.setCreatedAt(conv.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    public void markMessagesAsRead(String conversationId, Message.SenderType readerType) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        
        // Mark unread messages from the other party as read
        Message.SenderType senderTypeToMark = (readerType == Message.SenderType.USER) 
                ? Message.SenderType.VENDOR 
                : Message.SenderType.USER;
        
        messages.stream()
                .filter(msg -> !msg.getIsRead() && msg.getSenderType() == senderTypeToMark)
                .forEach(msg -> msg.setIsRead(true));
        
        messageRepository.saveAll(messages);
        
        // Reset unread count
        if (readerType == Message.SenderType.USER) {
            conversation.setUnreadCountUser(0);
        } else {
            conversation.setUnreadCountVendor(0);
        }
        
        conversationRepository.save(conversation);
    }
    
    @Override
    public ConversationDTO getConversationDTO(String conversationId, String currentUserId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conv.getId());
        dto.setUserId(conv.getUser().getId());
        dto.setUserName(conv.getUser().getFullName());
        dto.setUserAvatar(conv.getUser().getAvatar());
        dto.setStoreId(conv.getStore().getId());
        dto.setStoreName(conv.getStore().getName());
        dto.setStoreAvatar(conv.getStore().getFeaturedImages());
        dto.setLastMessage(conv.getLastMessage());
        dto.setLastMessageTime(conv.getLastMessageTime());
        
        // Set unread count based on who is viewing
        boolean isVendor = conv.getStore().getOwner().getId().equals(currentUserId);
        dto.setUnreadCount(isVendor ? conv.getUnreadCountVendor() : conv.getUnreadCountUser());
        dto.setCreatedAt(conv.getCreatedAt());
        
        return dto;
    }
    
    private MessageDTO convertToMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());
        dto.setSenderAvatar(message.getSender().getAvatar());
        dto.setContent(message.getContent());
        dto.setSenderType(message.getSenderType().toString());
        dto.setIsRead(message.getIsRead());
        dto.setImageUrl(message.getImageUrl());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}