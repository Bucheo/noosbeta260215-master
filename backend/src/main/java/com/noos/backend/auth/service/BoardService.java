package com.noos.backend.auth.service;

import com.noos.backend.auth.dto.BoardComment;
import com.noos.backend.auth.dto.BoardPost;
import com.noos.backend.auth.dto.BoardRequest;
import com.noos.backend.auth.mapper.BoardMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BoardService.java
 * 게시판 비즈니스 로직: 유효성 검증 + 권한 확인 + 트랜잭션 관리
 */
@Service
public class BoardService {

    private final BoardMapper boardMapper;

    public BoardService(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    // ── 게시글 목록 조회 ──────────────────────────────────────────────────────
    public Map<String, Object> getPosts(String category, String searchQuery,
                                        String sortBy, int page, int size) {
        String catParam    = (category == null    || category.equals("ALL")) ? null : category;
        String searchParam = (searchQuery == null || searchQuery.isBlank())  ? null : searchQuery.trim();
        int    offset      = (page - 1) * size;

        List<BoardPost> posts = boardMapper.findPosts(catParam, searchParam, sortBy, offset, size);
        int total             = boardMapper.countPosts(catParam, searchParam);
        int totalPages        = (int) Math.ceil((double) total / size);

        Map<String, Object> result = new HashMap<>();
        result.put("posts",      posts);
        result.put("total",      total);
        result.put("totalPages", totalPages);
        result.put("page",       page);
        return result;
    }

    // ── 게시글 상세 조회 + 조회수 증가 ───────────────────────────────────────
    @Transactional
    public BoardPost getPost(Long id) {
        boardMapper.incrementViews(id);
        return boardMapper.findPostById(id);
    }

    // ── 게시글 작성 ───────────────────────────────────────────────────────────
    @Transactional
    public BoardPost createPost(BoardRequest request, Long authorId, String authorName) {
        if (request.getTitle() == null || request.getTitle().isBlank())
            throw new IllegalArgumentException("제목을 입력해주세요.");
        if (request.getContent() == null || request.getContent().isBlank())
            throw new IllegalArgumentException("내용을 입력해주세요.");
        if (request.getTitle().length() > 100)
            throw new IllegalArgumentException("제목은 100자 이하여야 합니다.");
        if (request.getContent().length() > 5000)
            throw new IllegalArgumentException("내용은 5000자 이하여야 합니다.");

        BoardPost post = new BoardPost();
        post.setCategory(request.getCategory() != null ? request.getCategory() : "FREE");
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setPinned(request.isPinned());
        post.setAuthorId(authorId);
        post.setAuthor(authorName);
        post.setCreatedAt(LocalDateTime.now());

        boardMapper.insertPost(post);
        return post;
    }

    // ── 게시글 수정 ───────────────────────────────────────────────────────────
    @Transactional
    public BoardPost updatePost(Long id, BoardRequest request, Long requesterId, boolean isAdmin) {
        BoardPost existing = boardMapper.findPostById(id);
        if (existing == null)
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        if (!isAdmin && !existing.getAuthorId().equals(requesterId))
            throw new SecurityException("수정 권한이 없습니다.");

        existing.setCategory(request.getCategory() != null ? request.getCategory() : existing.getCategory());
        existing.setTitle(request.getTitle() != null ? request.getTitle().trim() : existing.getTitle());
        existing.setContent(request.getContent() != null ? request.getContent().trim() : existing.getContent());
        existing.setPinned(request.isPinned());
        existing.setUpdatedAt(LocalDateTime.now());

        boardMapper.updatePost(existing);
        return existing;
    }

    // ── 게시글 삭제 ───────────────────────────────────────────────────────────
    @Transactional
    public void deletePost(Long id, Long requesterId, boolean isAdmin) {
        BoardPost existing = boardMapper.findPostById(id);
        if (existing == null)
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        if (!isAdmin && !existing.getAuthorId().equals(requesterId))
            throw new SecurityException("삭제 권한이 없습니다.");

        boardMapper.deletePost(id);
    }

    // ── 게시글 좋아요 토글 ────────────────────────────────────────────────────
    @Transactional
    public void toggleLike(Long id, boolean like) {
        if (like) boardMapper.incrementLikes(id);
        else      boardMapper.decrementLikes(id);
    }

    // ── 댓글 목록 조회 ────────────────────────────────────────────────────────
    public List<BoardComment> getComments(Long postId) {
        return boardMapper.findCommentsByPostId(postId);
    }

    // ── 댓글 작성 ─────────────────────────────────────────────────────────────
    @Transactional
    public BoardComment addComment(Long postId, String content, Long authorId, String authorName) {
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        if (content.length() > 1000)
            throw new IllegalArgumentException("댓글은 1000자 이하여야 합니다.");

        BoardComment comment = new BoardComment();
        comment.setPostId(postId);
        comment.setContent(content.trim());
        comment.setAuthorId(authorId);
        comment.setAuthor(authorName);
        comment.setCreatedAt(LocalDateTime.now());

        boardMapper.insertComment(comment);         // 댓글 INSERT
        boardMapper.incrementCommentCount(postId);  // 게시글 댓글 수 +1
        return comment;
    }

    // ── 댓글 삭제 ─────────────────────────────────────────────────────────────
    @Transactional
    public void deleteComment(Long commentId, Long requesterId, boolean isAdmin) {
        BoardComment comment = boardMapper.findCommentById(commentId);
        if (comment == null)
            throw new IllegalArgumentException("존재하지 않는 댓글입니다.");
        if (!isAdmin && !comment.getAuthorId().equals(requesterId))
            throw new SecurityException("삭제 권한이 없습니다.");

        boardMapper.deleteComment(commentId);
        boardMapper.decrementCommentCount(comment.getPostId()); // 게시글 댓글 수 -1
    }

    // ── 댓글 좋아요 ───────────────────────────────────────────────────────────
    @Transactional
    public void likeComment(Long commentId) {
        boardMapper.incrementCommentLikes(commentId);
    }
}