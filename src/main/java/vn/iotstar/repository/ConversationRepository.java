package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.Conversation;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    Optional<Conversation> findByUserIdAndStoreId(String userId, String storeId);
    List<Conversation> findByUserIdOrderByLastMessageTimeDesc(String userId);
    List<Conversation> findByStoreIdOrderByLastMessageTimeDesc(String storeId);
    List<Conversation> findByStore_Owner_IdOrderByLastMessageTimeDesc(String ownerId);
}
