package com.noos.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig.java
 * ──────────────────────────────────────────────────────────────────────────────
 * STOMP 기반 WebSocket 설정
 *
 * 동작 흐름:
 *   클라이언트 → /ws 엔드포인트로 WebSocket 연결
 *   클라이언트 → /app/chat.send 로 메시지 전송
 *   서버       → /topic/room.{roomId} 로 전체 브로드캐스트
 *
 * ※ 파일명 반드시 WebSocketConfig.java 로 저장할 것
 *   (Java는 파일명 = 클래스명 이어야 컴파일 가능)
 * ──────────────────────────────────────────────────────────────────────────────
 */
@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 설정
     * /topic  → 서버가 클라이언트에게 메시지를 보낼 때 사용하는 경로
     * /app    → 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로 접두사
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP WebSocket 엔드포인트 등록
     * /ws 경로로 연결하고 SockJS 폴백 활성화
     * (SockJS: WebSocket 미지원 브라우저에서도 동작하도록 대체 연결 제공)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // 개발 환경 CORS 전체 허용
            .withSockJS();
    }
}