package vn.iotstar.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import vn.iotstar.dto.ChatMessageRequest;
import vn.iotstar.dto.ConversationDTO;
import vn.iotstar.dto.MessageDTO;
import vn.iotstar.entity.Conversation;
import vn.iotstar.entity.Message;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.User;
import vn.iotstar.repository.OrderRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.ChatService;

@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final OrderRepository orderRepository;
    
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
    public String userChatPage(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String orderId,
            Authentication authentication, 
            Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<ConversationDTO> conversations = chatService.getUserConversations(user.getId());
        model.addAttribute("conversations", conversations);
        model.addAttribute("currentUser", user);
        
        // Handle auto-message when coming from order page
        if (storeId != null && orderId != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null && order.getUser().getId().equals(user.getId())) {
                // Format order info message
                DecimalFormat df = new DecimalFormat("#,###");
                
                String productList = order.getOrderItems().stream()
                    .map(item -> "• " + item.getProduct().getName() + " (x" + item.getQuantity() + ")")
                    .collect(Collectors.joining("\n"));
                
                BigDecimal totalAmount = order.getAmountFromUser();
                if (order.getShippingFee() != null) {
                    totalAmount = totalAmount.add(order.getShippingFee());
                }
                
                String orderStatus = "";
                switch (order.getStatus()) {
                    case NOT_PROCESSED:
                        orderStatus = "Chờ xác nhận";
                        break;
                    case PROCESSING:
                        orderStatus = "Đã xác nhận";
                        break;
                    case DELIVERING:
                        orderStatus = "Đang giao";
                        break;
                    case AWAITING_CONFIRMATION:
                        orderStatus = "Đã giao - Chờ xác nhận";
                        break;
                    case DELIVERED:
                        orderStatus = "Đã giao";
                        break;
                    case CANCELLED:
                        orderStatus = "Đã hủy";
                        break;
                    case RETURNED:
                        orderStatus = "Trả hàng";
                        break;
                    default:
                        orderStatus = order.getStatus().toString();
                }
                
                String autoMessage = String.format(
                    "Xin chào shop, tôi muốn hỏi về đơn hàng này:%n%n" +
                    "Mã đơn hàng: #%s%n" +
                    "Trạng thái: %s%n" +
                    "Sản phẩm:%n%s%n%n" +
                    "Tổng tiền: %s đ%n%n" +
                    "Tôi muốn biết thêm thông tin về ",
                    order.getId(),
                    orderStatus,
                    productList,
                    df.format(totalAmount)
                );
                
                model.addAttribute("autoMessage", autoMessage);
                model.addAttribute("targetStoreId", storeId);
            }
        } else if (storeId != null) {
            model.addAttribute("targetStoreId", storeId);
        }
        
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
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Conversation conversation = chatService.getOrCreateConversation(user.getId(), storeId);
        return chatService.getConversationDTO(conversation.getId(), user.getId());
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
    
    // Exception handler for chat-related errors
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Đã xảy ra lỗi: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}