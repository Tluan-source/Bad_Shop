package vn.iotstar.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.iotstar.service.AiChatService;

@RestController
@RequestMapping(path = "/api/ai-chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class AiChatController {

    public record ChatResponse(String reply) {}

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping(path = "/ping")
    public ResponseEntity<ChatResponse> ping() {
        return ResponseEntity.ok(new ChatResponse("pong"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chat(@RequestBody(required = false) Map<String, Object> req) {
        try {
            String message = null;
            if (req != null) {
                Object raw = req.get("message");
                if (raw != null) message = raw.toString();
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.ok(new ChatResponse("Bạn hãy nhập câu hỏi để mình tư vấn."));
            }
            String reply = aiChatService.chat(message.trim());
            if (reply == null || reply.isBlank()) {
                reply = "Hiện chưa có phản hồi. Bạn có thể nói 'mua vợt tầm 700k' hoặc 'phụ kiện dưới 50k' để mình tư vấn.";
            }
            return ResponseEntity.ok(new ChatResponse(reply));
        } catch (Exception ex) {
            return ResponseEntity.ok(new ChatResponse("Hệ thống đang bận. Vui lòng thử lại sau."));
        }
    }
}


