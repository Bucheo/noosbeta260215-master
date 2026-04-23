package com.noos.backend.chat.dto;

/**
 * ChatRoom.java
 * ──────────────────────────────────────────────────────────────────────────────
 * 채팅방 정보 DTO
 * 관리자 페이지에서 활성 채팅방 목록을 표시할 때 사용
 * ──────────────────────────────────────────────────────────────────────────────
 */
public class ChatRoom {

    private String roomId;       // 채팅방 ID (유저 ID 기준으로 생성)
    private String userName;     // 채팅 중인 유저 닉네임
    private String lastMessage;  // 마지막 메시지 내용 (미리보기)
    private int    unreadCount;  // 읽지 않은 메시지 수
    private String updatedAt;    // 마지막 메시지 시각

    public ChatRoom() {}

    public ChatRoom(String roomId, String userName) {
        this.roomId   = roomId;
        this.userName = userName;
    }

    // ── Getter / Setter ──────────────────────────────────────────────────────

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}