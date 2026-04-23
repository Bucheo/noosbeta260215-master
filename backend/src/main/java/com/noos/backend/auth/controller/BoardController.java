package com.noos.backend.auth.controller;

import com.noos.backend.auth.dto.BoardComment;
import com.noos.backend.auth.dto.BoardPost;
import com.noos.backend.auth.dto.BoardRequest;
import com.noos.backend.auth.service.BoardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BoardController.java
 * 기본 경로: /api/auth/board
 *
 * GET    /api/auth/board                          - 게시글 목록
 * GET    /api/auth/board/{id}                     - 게시글 상세 + 조회수 증가
 * POST   /api/auth/board                          - 게시글 작성 (로그인 필요)
 * PUT    /api/auth/board/{id}                     - 게시글 수정 (본인/관리자)
 * DELETE /api/auth/board/{id}                     - 게시글 삭제 (본인/관리자)
 * POST   /api/auth/board/{id}/like                - 좋아요 토글
 * GET    /api/auth/board/{id}/comments            - 댓글 목록
 * POST   /api/auth/board/{id}/comments            - 댓글 작성
 * DELETE /api/auth/board/comments/{commentId}     - 댓글 삭제
 * POST   /api/auth/board/comments/{commentId}/like - 댓글 좋아요
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
    private Long getSessionUserId(HttpSession session) {
        Object id = session.getAttribute("userId");
        return id != null ? Long.parseLong(id.toString()) : null;
    }

    private String getSessionUserName(HttpSession session) {
        Object name = session.getAttribute("displayName");
        return name != null ? name.toString() : "익명";
    }

    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"));
    }

    // ── 게시글 목록 조회 ──────────────────────────────────────────────────────
    // GET /api/auth/board?category=ALL&search=&sort=latest&page=1&size=8
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
    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        BoardPost post = boardService.getPost(id);
        if (post == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(post);
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

    // ── 게시글 좋아요 ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id,
                                      @RequestParam(defaultValue = "true") boolean like,
                                      HttpSession session) {
        if (getSessionUserId(session) == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        boardService.toggleLike(id, like);
        return ResponseEntity.ok(like ? "추천하였습니다." : "추천을 취소하였습니다.");
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

    // ── 댓글 좋아요 ───────────────────────────────────────────────────────────
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId, HttpSession session) {
        if (getSessionUserId(session) == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        boardService.likeComment(commentId);
        return ResponseEntity.ok("댓글을 추천하였습니다.");
    }
}