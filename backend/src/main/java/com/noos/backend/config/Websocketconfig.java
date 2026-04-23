package com.noos.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig.java
 * ──────────────────────────────────────────────────────────────────────────────
 * STOMP(Simple Text Oriented Messaging Protocol) 기반 WebSocket 설정
 *
 * 동작 흐름:
 *   클라이언트 → /ws 엔드포인트로 WebSocket 연결
 *   클라이언트 → /app/chat.send 로 메시지 전송
 *   서버       → /topic/room.{roomId} 로 구독자 전체에게 브로드캐스트
 * ──────────────────────────────────────────────────────────────────────────────
 */
@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 설정
     * - /topic : 서버 → 클라이언트 브로드캐스트 경로 (구독 경로)
     * - /app   : 클라이언트 → 서버 전송 경로 접두사
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 경로 접두사 (서버가 메시지를 보낼 때 사용)
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 서버로 메시지를 보낼 때 사용할 경로 접두사
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP WebSocket 엔드포인트 등록
     * - /ws 경로로 WebSocket 연결 수립
     * - SockJS 폴백: WebSocket 미지원 브라우저에서도 동작
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("/ws")                        // WebSocket 연결 엔드포인트
            .setAllowedOrigins("http://localhost:3000") // CORS 허용 출처
            .withSockJS();                              // SockJS 폴백 활성화
    }
}