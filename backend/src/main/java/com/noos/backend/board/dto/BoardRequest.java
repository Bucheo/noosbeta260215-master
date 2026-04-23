package com.noos.backend.board.dto;

/** 게시글 작성/수정 요청 바디 DTO */
public class BoardRequest {
    private String  category;
    private String  title;
    private String  content;
    private boolean pinned;

    public BoardRequest() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
}