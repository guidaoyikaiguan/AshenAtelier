package com.shipin.entity.query;

import java.util.Date;

public class ChatSession {
    private Integer id;
    private Integer userId1;
    private Integer userId2;
    private Integer lastMessageId;
    private Date updateTime;

    // 构造方法、getter和setter
    public ChatSession() {}

    public ChatSession(Integer userId1, Integer userId2) {
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.updateTime = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId1() {
        return userId1;
    }

    public void setUserId1(Integer userId1) {
        this.userId1 = userId1;
    }

    public Integer getUserId2() {
        return userId2;
    }

    public void setUserId2(Integer userId2) {
        this.userId2 = userId2;
    }

    public Integer getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(Integer lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
