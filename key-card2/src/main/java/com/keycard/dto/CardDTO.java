package com.keycard.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CardDTO {

    private Long id;
    private String title;
    private String content;
    private String source;
    private List<TagInfo> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CardDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<TagInfo> getTags() { return tags; }
    public void setTags(List<TagInfo> tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
