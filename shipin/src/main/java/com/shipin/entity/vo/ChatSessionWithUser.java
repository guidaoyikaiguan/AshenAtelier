package com.shipin.entity.vo;

public class ChatSessionWithUser {
    private Integer sessionId;
    private Integer otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String lastMessage;
    private Integer unreadCount;
    private String updateTime;

    // 构造方法、getter和setter
    public ChatSessionWithUser() {}

    public ChatSessionWithUser(Integer sessionId, Integer otherUserId, String otherUserName, String otherUserAvatar, String lastMessage, Integer unreadCount, String updateTime) {
        this.sessionId = sessionId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserAvatar = otherUserAvatar;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
        this.updateTime = updateTime;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Integer otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserAvatar() {
        return otherUserAvatar;
    }

    public void setOtherUserAvatar(String otherUserAvatar) {
        this.otherUserAvatar = otherUserAvatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
