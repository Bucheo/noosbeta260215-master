package com.noos.backend.chat.controller;

import com.noos.backend.chat.dto.ChatMessage;
import com.noos.backend.chat.dto.ChatRoom;
import com.noos.backend.chat.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ChatController.java
 * ──────────────────────────────────────────────────────────────────────────────
 * 실시간 채팅 컨트롤러
 *
 * ※ @Controller + @ResponseBody 조합 사용
 *   → @MessageMapping(WebSocket)과 @GetMapping(REST) 동시 처리 가능
 *   → @RestController 단독 사용 시 @MessageMapping이 동작하지 않음
 *
 * WebSocket (STOMP) 엔드포인트:
 *   /app/chat.send       → 메시지 전송
 *   /app/chat.join       → 채팅방 입장
 *   /app/chat.leave      → 채팅방 퇴장
 *   /topic/room.{roomId} → 구독자 전체 브로드캐스트
 *
 * REST 엔드포인트:
 *   GET /api/chat/rooms            → 활성 채팅방 목록 (관리자용)
 *   GET /api/chat/history/{roomId} → 채팅 히스토리 조회
 * ──────────────────────────────────────────────────────────────────────────────
 */
@Controller // ← @RestController 대신 @Controller 사용 (@MessageMapping 동작에 필수)
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송 유틸
    private final ChatService chatService;

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    // ════════════════════════════════════════════════════════════════════════
    // WebSocket STOMP 핸들러
    // ════════════════════════════════════════════════════════════════════════

    /**
     * 채팅 메시지 전송
     * 클라이언트: stompClient.send("/app/chat.send", {}, JSON.stringify(msg))
     * 서버: 저장 후 /topic/room.{roomId} 로 브로드캐스트
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message) {
        // 서버 타임스탬프 설정 (클라이언트 시간 신뢰 X)
        message.setTimestamp(LocalDateTime.now());
        message.setType(ChatMessage.MessageType.CHAT);

        // 메모리에 메시지 저장
        chatService.saveMessage(message);

        // 유저 메시지일 때만 읽지 않은 수 증가 (관리자 알림용)
        if (!"ADMIN".equals(message.getRole())) {
            chatService.incrementUnread(message.getRoomId());
        }

        // 해당 채팅방 구독자 전체에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room." + message.getRoomId(), message);
    }

    /**
     * 채팅방 입장 알림
     * 입장 메시지를 채팅방 전체에 브로드캐스트
     */
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload ChatMessage message) {
        message.setType(ChatMessage.MessageType.JOIN);
        message.setTimestamp(LocalDateTime.now());

        // 채팅방 생성 또는 기존 방 조회
        chatService.getOrCreateRoom(message.getRoomId(), message.getSender());

        messagingTemplate.convertAndSend("/topic/room." + message.getRoomId(), message);
    }

    /**
     * 채팅방 퇴장 알림
     * 퇴장 메시지 브로드캐스트 후 채팅방 비활성화
     */
    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload ChatMessage message) {
        message.setType(ChatMessage.MessageType.LEAVE);
        message.setTimestamp(LocalDateTime.now());

        chatService.removeRoom(message.getRoomId());

        messagingTemplate.convertAndSend("/topic/room." + message.getRoomId(), message);
    }

    // ════════════════════════════════════════════════════════════════════════
    // REST API 엔드포인트 (@ResponseBody 붙여야 JSON 반환됨)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * 활성 채팅방 목록 조회 (관리자 전용)
     * GET /api/chat/rooms
     */
    @ResponseBody
    @GetMapping("/api/chat/rooms")
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<List<ChatRoom>> getRooms(HttpSession session) {
        // 관리자만 전체 채팅방 목록 조회 가능
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(chatService.getAllRooms());
    }

    /**
     * 채팅방 메시지 히스토리 조회
     * GET /api/chat/history/{roomId}
     * - 관리자: 모든 방 조회 가능
     * - 일반 유저: 본인 채팅방만 조회 가능
     */
    @ResponseBody
    @GetMapping("/api/chat/history/{roomId}")
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public ResponseEntity<List<ChatMessage>> getHistory(
        @PathVariable String roomId,
        HttpSession session
    ) {
        String role        = (String) session.getAttribute("role");
        String displayName = (String) session.getAttribute("displayName");

        // 권한 확인: 관리자이거나 본인 채팅방만 허용
        if (!"ADMIN".equals(role) && !roomId.equals(displayName)) {
            return ResponseEntity.status(403).build();
        }

        // 관리자 입장 시 읽지 않은 수 초기화
        if ("ADMIN".equals(role)) {
            chatService.resetUnread(roomId);
        }

        return ResponseEntity.ok(chatService.getHistory(roomId));
    }
}