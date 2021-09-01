package com.yyzcl;

import java.time.Instant;

public class News {
    private long id;
    private String url;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public News() {
    }

    public News(String url, String title, String content) {
        this.url = url;
        this.title = title;
        this.content = content;
    }

    public News(News o) {
        this.id = o.id;
        this.url = o.url;
        this.title = o.title;
        this.content = o.content;
        this.createdAt = o.createdAt;
        this.updatedAt = o.updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
