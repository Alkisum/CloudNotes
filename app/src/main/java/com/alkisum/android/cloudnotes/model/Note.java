package com.alkisum.android.cloudnotes.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Note {

    @Id(autoincrement = true)
    private Long id;

    private String title;

    private String content;

    private Long updatedTime;

    @Transient
    private boolean selected;

    @Generated(hash = 182423927)
    public Note(Long id, String title, String content, Long updatedTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.updatedTime = updatedTime;
    }

    @Generated(hash = 1272611929)
    public Note() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUpdatedTime() {
        return this.updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
