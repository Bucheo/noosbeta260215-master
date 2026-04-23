package com.noos.backend.board.service;

import com.noos.backend.board.dto.BoardComment;
import com.noos.backend.board.dto.BoardPost;
import com.noos.backend.board.dto.BoardRequest;
import com.noos.backend.board.mapper.BoardMapper;

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

    // ── 게시글 상세 조회 + 좋아요 여부 포함 ──────────────────────────────────
    // 로그인 유저의 좋아요 여부를 함께 반환
    @Transactional
    public Map<String, Object> getPostWithLike(Long id, Long userId) {
        boardMapper.incrementViews(id);
        BoardPost post = boardMapper.findPostById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("post",    post);
        // userId가 있으면 좋아요 여부 확인, 없으면 false
        result.put("liked", userId != null && boardMapper.checkUserLiked(userId, id) > 0);
        return result;
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
    // board_post_likes 테이블로 유저별 중복 방지
    // - 좋아요 안 한 상태 → INSERT + likes +1 → liked: true 반환
    // - 이미 좋아요 한 상태 → DELETE + likes -1 → liked: false 반환
    @Transactional
    public Map<String, Object> toggleLike(Long postId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 현재 좋아요 상태 확인
        boolean alreadyLiked = boardMapper.checkUserLiked(userId, postId) > 0;

        if (alreadyLiked) {
            // 좋아요 취소
            boardMapper.deleteUserLike(userId, postId);
            boardMapper.decrementLikes(postId);
            result.put("liked", false);
            result.put("message", "추천을 취소하였습니다.");
        } else {
            // 좋아요 등록
            boardMapper.insertUserLike(userId, postId);
            boardMapper.incrementLikes(postId);
            result.put("liked", true);
            result.put("message", "추천하였습니다.");
        }

        // 변경 후 최신 likes 수 반환
        BoardPost updated = boardMapper.findPostById(postId);
        result.put("likes", updated != null ? updated.getLikes() : 0);
        return result;
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

        boardMapper.insertComment(comment);
        boardMapper.incrementCommentCount(postId);
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
        boardMapper.decrementCommentCount(comment.getPostId());
    }

    // ── 댓글 좋아요 토글 ─────────────────────────────────────────────────────
    // board_comment_likes 테이블로 유저별 중복 방지
    // - 첫 클릭:  INSERT + likes+1 → liked: true
    // - 재클릭:   DELETE + likes-1 → liked: false
    @Transactional
    public Map<String, Object> toggleCommentLike(Long commentId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 현재 좋아요 상태 확인
        boolean alreadyLiked = boardMapper.checkUserCommentLiked(userId, commentId) > 0;

        if (alreadyLiked) {
            // 좋아요 취소
            boardMapper.deleteUserCommentLike(userId, commentId);
            boardMapper.decrementCommentLikes(commentId);
            result.put("liked", false);
            result.put("message", "댓글 추천을 취소하였습니다.");
        } else {
            // 좋아요 등록
            boardMapper.insertUserCommentLike(userId, commentId);
            boardMapper.incrementCommentLikes(commentId);
            result.put("liked", true);
            result.put("message", "댓글을 추천하였습니다.");
        }

        // 변경 후 최신 likes 수 반환
        BoardComment updated = boardMapper.findCommentById(commentId);
        result.put("likes", updated != null ? updated.getLikes() : 0);
        return result;
    }
}