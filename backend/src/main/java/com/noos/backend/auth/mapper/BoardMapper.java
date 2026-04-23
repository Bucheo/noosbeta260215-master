package com.noos.backend.auth.mapper;

import com.noos.backend.auth.dto.BoardComment;
import com.noos.backend.auth.dto.BoardPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * BoardMapper.java
 * SQL은 resources/mappers/board/BoardMapper.xml 에 작성
 */
@Mapper
public interface BoardMapper {

    // 게시글 목록 조회 (카테고리·검색·정렬·페이징)
    List<BoardPost> findPosts(
        @Param("category")    String category,
        @Param("searchQuery") String searchQuery,
        @Param("sortBy")      String sortBy,
        @Param("offset")      int    offset,
        @Param("limit")       int    limit
    );

    // 전체 게시글 수 (페이지네이션용)
    int countPosts(
        @Param("category")    String category,
        @Param("searchQuery") String searchQuery
    );

    // 게시글 단건 조회
    BoardPost findPostById(@Param("id") Long id);

    // 게시글 등록
    void insertPost(BoardPost post);

    // 게시글 수정
    void updatePost(BoardPost post);

    // 게시글 삭제
    void deletePost(@Param("id") Long id);

    // 조회수 증가
    void incrementViews(@Param("id") Long id);

    // 좋아요 증가
    void incrementLikes(@Param("id") Long id);

    // 좋아요 감소 (최솟값 0)
    void decrementLikes(@Param("id") Long id);

    // 댓글 수 증가
    void incrementCommentCount(@Param("id") Long id);

    // 댓글 수 감소 (최솟값 0)
    void decrementCommentCount(@Param("id") Long id);

    // 댓글 목록 조회
    List<BoardComment> findCommentsByPostId(@Param("postId") Long postId);

    // 댓글 등록
    void insertComment(BoardComment comment);

    // 댓글 단건 조회
    BoardComment findCommentById(@Param("id") Long id);

    // 댓글 삭제
    void deleteComment(@Param("id") Long id);

    // 댓글 좋아요 증가
    void incrementCommentLikes(@Param("id") Long id);
}