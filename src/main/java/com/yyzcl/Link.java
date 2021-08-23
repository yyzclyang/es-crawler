package com.yyzcl;

import java.util.Objects;

public class Link {
    public long id;
    public String url;
    public int status;

    public Link(long id, String url, int status) {
        this.id = id;
        this.url = url;
        this.status = status;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Link)) {
            return false;
        }
        Link link = (Link) o;
        return url.equals(link.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
