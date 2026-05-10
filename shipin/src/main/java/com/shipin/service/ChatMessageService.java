package com.shipin.service;

import com.shipin.entity.po.ChatMessage;
import com.shipin.entity.vo.ChatSessionWithUser;

import java.util.List;

public interface ChatMessageService {
    List<ChatMessage> getChatMessage(Integer userId, Integer otherUserId);

    void sendMessage(Integer senderId, Integer receiverId, String content);

    void markAllAsRead(Integer userId, Integer otherUserId);

    Integer getUnreadCount(Integer userId, Integer otherUserId);

    Integer getAllUnreadCount(Integer userId);

    List<ChatSessionWithUser> getSessions(Integer userId);
}
