-- ─────────────────────────────────────────────────────────────────────────────
-- board_schema.sql
-- 게시판 테이블 초기화 스크립트
-- 실행 방법: MySQL 접속 후 `source board_schema.sql` 또는 직접 붙여넣기
-- ─────────────────────────────────────────────────────────────────────────────

USE noos_db;

-- ─── 게시글 테이블 ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS board_posts (
    id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '게시글 고유 ID',
    category      VARCHAR(20)  NOT NULL DEFAULT 'FREE'  COMMENT '카테고리 (NOTICE/FREE/QNA/INFO)',
    title         VARCHAR(100) NOT NULL                  COMMENT '제목 (최대 100자)',
    content       TEXT         NOT NULL                  COMMENT '본문 (최대 5000자)',
    author        VARCHAR(50)  NOT NULL                  COMMENT '작성자 닉네임',
    author_id     BIGINT       NOT NULL                  COMMENT '작성자 FK (users.user_id)',
    views         INT          NOT NULL DEFAULT 0        COMMENT '조회수',
    likes         INT          NOT NULL DEFAULT 0        COMMENT '좋아요(추천) 수',
    comment_count INT          NOT NULL DEFAULT 0        COMMENT '댓글 수 (캐시)',
    pinned        TINYINT(1)   NOT NULL DEFAULT 0        COMMENT '상단 고정 여부 (1=고정)',
    created_at    DATETIME     NOT NULL                  COMMENT '작성 시각',
    updated_at    DATETIME         NULL DEFAULT NULL     COMMENT '수정 시각 (수정 전이면 NULL)',
    PRIMARY KEY (id),
    INDEX idx_category   (category),               -- 카테고리 필터 성능 향상
    INDEX idx_author_id  (author_id),              -- 특정 유저 게시글 조회
    INDEX idx_created_at (created_at DESC),        -- 최신순 정렬 성능 향상
    INDEX idx_pinned     (pinned DESC, created_at DESC), -- 고정 + 최신순 복합 인덱스
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시판 게시글';

-- ─── 댓글 테이블 ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS board_comments (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '댓글 고유 ID',
    post_id    BIGINT      NOT NULL                COMMENT '부모 게시글 FK (board_posts.id)',
    author_id  BIGINT      NOT NULL                COMMENT '작성자 FK (users.user_id)',
    author     VARCHAR(50) NOT NULL                COMMENT '작성자 닉네임',
    content    TEXT        NOT NULL                COMMENT '댓글 내용 (최대 1000자)',
    likes      INT         NOT NULL DEFAULT 0      COMMENT '댓글 좋아요 수',
    created_at DATETIME    NOT NULL                COMMENT '작성 시각',
    updated_at DATETIME        NULL DEFAULT NULL   COMMENT '수정 시각',
    PRIMARY KEY (id),
    INDEX idx_comment_post_id    (post_id),         -- 게시글별 댓글 조회
    INDEX idx_comment_author_id  (author_id),       -- 유저별 댓글 조회
    INDEX idx_comment_created_at (created_at ASC),  -- 등록순 정렬
    -- 게시글 삭제 시 댓글도 함께 삭제 (CASCADE)
    CONSTRAINT fk_comment_post   FOREIGN KEY (post_id)   REFERENCES board_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(user_id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시판 댓글';

-- ─── 게시글 좋아요 중복 방지 테이블 (선택사항) ────────────────────────────────
-- 같은 유저가 같은 게시글에 중복 추천하지 못하도록 막을 때 사용
CREATE TABLE IF NOT EXISTS board_post_likes (
    user_id BIGINT NOT NULL COMMENT '좋아요를 누른 유저 ID',
    post_id BIGINT NOT NULL COMMENT '좋아요 대상 게시글 ID',
    PRIMARY KEY (user_id, post_id),  -- 복합 PK로 중복 방지
    CONSTRAINT fk_postlike_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_postlike_post FOREIGN KEY (post_id) REFERENCES board_posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시글 좋아요 기록';

-- ─── 샘플 데이터 삽입 ────────────────────────────────────────────────────────
-- 테스트용 공지 게시글 (author_id=1 은 관리자 계정 가정)
INSERT IGNORE INTO board_posts
    (category, title, content, author, author_id, views, likes, comment_count, pinned, created_at)
VALUES
    ('NOTICE', '서비스 이용 규칙 안내',
     '서비스 이용 시 다음 규칙을 준수해 주세요.\n\n1. 타인을 비방하는 게시글은 삭제될 수 있습니다.\n2. 광고성 게시글은 허용되지 않습니다.\n3. 개인 정보를 공유하지 마세요.',
     '관리자', 1, 100, 10, 0, 1, NOW());