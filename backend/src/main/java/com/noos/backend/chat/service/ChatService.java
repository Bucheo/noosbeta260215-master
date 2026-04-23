package com.noos.backend.chat.service;

import com.noos.backend.chat.dto.ChatMessage;
import com.noos.backend.chat.dto.ChatRoom;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatService.java
 * ──────────────────────────────────────────────────────────────────────────────
 * 실시간 채팅 비즈니스 로직
 *
 * 메모리 기반(ConcurrentHashMap)으로 채팅방과 메시지를 관리합니다.
 * 서버 재시작 시 데이터가 초기화됩니다.
 *
 * 실 서비스에서는 MySQL 또는 Redis로 교체를 권장합니다.
 * ──────────────────────────────────────────────────────────────────────────────
 */
@Service
public class ChatService {
    /**
     * 활성 채팅방 목록
     * key  = roomId (유저 displayName 기반)
     * value = ChatRoom 정보
     */
    private final Map<String, ChatRoom> activeRooms = new ConcurrentHashMap<>();

    /**
     * 채팅방별 메시지 히스토리
     * key  = roomId
     * value = 메시지 리스트 (최근 100개까지 보관)
     */
    private final Map<String, List<ChatMessage>> messageHistory = new ConcurrentHashMap<>();

    // ── 채팅방 생성 또는 조회 ─────────────────────────────────────────────────
    /**
     * roomId가 없으면 새로 생성, 있으면 기존 방 반환
     * @param roomId   채팅방 ID (유저 displayName)
     * @param userName 유저 닉네임
     */
    public ChatRoom getOrCreateRoom(String roomId, String userName) {
        return activeRooms.computeIfAbsent(roomId, id -> {
            ChatRoom room = new ChatRoom(id, userName);
            messageHistory.put(id, new ArrayList<>());
            return room;
        });
    }

    // ── 메시지 저장 ───────────────────────────────────────────────────────────
    /**
     * 메시지를 히스토리에 저장하고 채팅방의 마지막 메시지를 업데이트
     * 최근 100개 메시지만 보관 (메모리 절약)
     */
    public void saveMessage(ChatMessage message) {
        String roomId = message.getRoomId();

        // 채팅방이 없으면 자동 생성
        activeRooms.computeIfAbsent(roomId, id -> new ChatRoom(id, message.getSender()));
        messageHistory.computeIfAbsent(roomId, id -> new ArrayList<>());

        List<ChatMessage> history = messageHistory.get(roomId);

        // 메모리 절약: 최근 100개만 유지
        if (history.size() >= 100) {
            history.remove(0);
        }
        history.add(message);

        // 채팅방 마지막 메시지 업데이트 (관리자 목록 미리보기용)
        ChatRoom room = activeRooms.get(roomId);
        if (room != null) {
            room.setLastMessage(message.getContent());
            room.setUpdatedAt(message.getTimestamp() != null
                ? message.getTimestamp().toString() : "");
        }
    }

    // ── 채팅방 메시지 히스토리 조회 ───────────────────────────────────────────
    /** 특정 채팅방의 전체 메시지 히스토리 반환 */
    public List<ChatMessage> getHistory(String roomId) {
        return messageHistory.getOrDefault(roomId, new ArrayList<>());
    }

    // ── 전체 활성 채팅방 목록 조회 (관리자용) ────────────────────────────────
    /** 현재 활성화된 모든 채팅방 목록 반환 */
    public List<ChatRoom> getAllRooms() {
        return new ArrayList<>(activeRooms.values());
    }

    // ── 채팅방 삭제 (퇴장 시) ────────────────────────────────────────────────
    /** 유저 퇴장 시 채팅방 비활성화 (히스토리는 유지) */
    public void removeRoom(String roomId) {
        activeRooms.remove(roomId);
    }

    // ── 읽지 않은 메시지 수 초기화 ───────────────────────────────────────────
    /** 관리자가 채팅방 입장 시 unreadCount 초기화 */
    public void resetUnread(String roomId) {
        ChatRoom room = activeRooms.get(roomId);
        if (room != null) room.setUnreadCount(0);
    }

    // ── 읽지 않은 메시지 수 증가 ─────────────────────────────────────────────
    /** 유저가 메시지를 보낼 때마다 unreadCount 증가 */
    public void incrementUnread(String roomId) {
        ChatRoom room = activeRooms.get(roomId);
        if (room != null) room.setUnreadCount(room.getUnreadCount() + 1);
    }
}