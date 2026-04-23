package com.noos.backend.board.controller;

import com.noos.backend.board.dto.BoardComment;
import com.noos.backend.board.dto.BoardRequest;
import com.noos.backend.board.service.BoardService;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // ← Map 사용을 위해 필수

/**
 * BoardController.java
 * 기본 경로: /api/auth/board
 *
 * GET    /api/auth/board                           - 게시글 목록
 * GET    /api/auth/board/{id}                      - 게시글 상세 + 좋아요 여부
 * POST   /api/auth/board                           - 게시글 작성
 * PUT    /api/auth/board/{id}                      - 게시글 수정
 * DELETE /api/auth/board/{id}                      - 게시글 삭제
 * POST   /api/auth/board/{id}/like                 - 게시글 좋아요 토글
 * GET    /api/auth/board/{id}/comments             - 댓글 목록
 * POST   /api/auth/board/{id}/comments             - 댓글 작성
 * DELETE /api/auth/board/comments/{commentId}      - 댓글 삭제
 * POST   /api/auth/board/comments/{commentId}/like - 댓글 좋아요 토글
 */
@RestController
@RequestMapping("/api/auth/board")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // ── 세션 헬퍼 ─────────────────────────────────────────────────────────────

    /** 세션에서 로그인 유저 ID 추출 (미로그인 시 null) */
    private Long getSessionUserId(HttpSession session) {
        Object id = session.getAttribute("userId");
        return id != null ? Long.parseLong(id.toString()) : null;
    }

    /** 세션에서 로그인 유저 닉네임 추출 */
    private String getSessionUserName(HttpSession session) {
        Object name = session.getAttribute("displayName");
        return name != null ? name.toString() : "익명";
    }

    /** 세션에서 관리자 여부 확인 */
    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"));
    }

    // ── 게시글 목록 조회 ──────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPosts(
        @RequestParam(defaultValue = "ALL")    String category,
        @RequestParam(defaultValue = "")       String search,
        @RequestParam(defaultValue = "latest") String sort,
        @RequestParam(defaultValue = "1")      int    page,
        @RequestParam(defaultValue = "8")      int    size
    ) {
        return ResponseEntity.ok(boardService.getPosts(category, search, sort, page, size));
    }

    // ── 게시글 상세 조회 ──────────────────────────────────────────────────────
    // 로그인 유저의 좋아요 여부도 함께 반환 → { post: {...}, liked: bool }
    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id, HttpSession session) {
        Long userId = getSessionUserId(session);
        Map<String, Object> result = boardService.getPostWithLike(id, userId);
        if (result.get("post") == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    // ── 게시글 작성 ───────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody BoardRequest request, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        try {
            return ResponseEntity.ok(boardService.createPost(request, userId, getSessionUserName(session)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── 게시글 수정 ───────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody BoardRequest request,
                                        HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        try {
            return ResponseEntity.ok(boardService.updatePost(id, request, userId, isAdmin(session)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ── 게시글 삭제 ───────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        try {
            boardService.deletePost(id, userId, isAdmin(session));
            return ResponseEntity.ok("삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ── 게시글 좋아요 토글 ────────────────────────────────────────────────────
    // 첫 클릭: liked=true, likes+1 / 재클릭: liked=false, likes-1
    // 응답: { liked: bool, likes: number, message: string }
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        Map<String, Object> result = boardService.toggleLike(id, userId);
        return ResponseEntity.ok(result);
    }

    // ── 댓글 목록 조회 ────────────────────────────────────────────────────────
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<BoardComment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getComments(id));
    }

    // ── 댓글 작성 ─────────────────────────────────────────────────────────────
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id,
                                        @RequestBody Map<String, String> body,
                                        HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        try {
            return ResponseEntity.ok(boardService.addComment(
                id, body.get("content"), userId, getSessionUserName(session)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── 댓글 삭제 ─────────────────────────────────────────────────────────────
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
        try {
            boardService.deleteComment(commentId, userId, isAdmin(session));
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ── 댓글 좋아요 토글 ─────────────────────────────────────────────────────
    // 첫 클릭: liked=true, likes+1 / 재클릭: liked=false, likes-1
    // 응답: { liked: bool, likes: number, message: string }
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        Map<String, Object> result = boardService.toggleCommentLike(commentId, userId);
        return ResponseEntity.ok(result);
    }
}