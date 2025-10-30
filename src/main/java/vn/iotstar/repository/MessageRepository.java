package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    List<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId);
    Long countByConversationIdAndIsReadFalseAndSenderType(String conversationId, Message.SenderType senderType);
}
