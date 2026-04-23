package com.noos.backend.chat.dto;

import java.time.LocalDateTime;

/**
 * ChatMessage.java
 * ──────────────────────────────────────────────────────────────────────────────
 * 채팅 메시지 데이터 전송 객체
 *
 * 메시지 타입:
 *   CHAT  - 일반 채팅 메시지
 *   JOIN  - 유저 입장 알림
 *   LEAVE - 유저 퇴장 알림
 * ──────────────────────────────────────────────────────────────────────────────
 */
public class ChatMessage {

    /** 메시지 타입 정의 */
    public enum MessageType {
        CHAT,   // 일반 채팅 메시지
        JOIN,   // 입장 알림
        LEAVE   // 퇴장 알림
    }

    private MessageType   type;      // 메시지 타입
    private String        roomId;    // 채팅방 ID (유저 ID 기준)
    private String        sender;    // 발신자 닉네임
    private String        senderId;  // 발신자 ID
    private String        content;   // 메시지 내용
    private String        role;      // 발신자 역할 (USER / ADMIN)
    private LocalDateTime timestamp; // 메시지 전송 시각

    public ChatMessage() {}

    // ── Getter / Setter ──────────────────────────────────────────────────────

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}