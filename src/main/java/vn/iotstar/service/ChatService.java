package vn.iotstar.service;

import vn.iotstar.dto.ChatMessageRequest;
import vn.iotstar.dto.ConversationDTO;
import vn.iotstar.dto.MessageDTO;
import vn.iotstar.entity.Conversation;
import vn.iotstar.entity.Message;

import java.util.List;

public interface ChatService {
    Conversation getOrCreateConversation(String userId, String storeId);
    Conversation getConversationById(String conversationId);
    Message sendMessage(String userId, String conversationId, String content, String imageUrl, Message.SenderType senderType);
    List<MessageDTO> getConversationMessages(String conversationId);
    List<ConversationDTO> getUserConversations(String userId);
    List<ConversationDTO> getVendorConversations(String vendorId);
    void markMessagesAsRead(String conversationId, Message.SenderType readerType);
    ConversationDTO getConversationDTO(String conversationId, String currentUserId);
}