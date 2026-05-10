package com.shipin.mappers;

import com.shipin.entity.po.ChatMessage;
import com.shipin.entity.vo.ChatSessionWithUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    // 发送消息
    int insertMessage(@Param("query") ChatMessage message);

    // 获取与指定用户的聊天记录
    List<ChatMessage> getMessages(@Param("userId") Integer userId, @Param("otherUserId") Integer otherUserId);

    // 获取用户的聊天会话列表
    List<ChatSessionWithUser> getSessions(@Param("userId") Integer userId);

    // 标记消息为已读
    int markAsRead(@Param("messageId") Integer messageId);

    // 标记与指定用户的所有消息为已读
    int markAllAsRead(@Param("userId") Integer userId, @Param("otherUserId") Integer otherUserId);

    // 获取未读消息数量
    int getUnreadCount(@Param("userId") Integer userId, @Param("otherUserId") Integer otherUserId);

    // 获取最后一条消息
    ChatMessage getLastMessage(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    Integer getAllUnreadCount(@Param("userId")Integer userId);

    ChatMessage getMessage(@Param("senderId") Integer senderId, @Param("receiverId") Integer receiverId, @Param("content") String content);
}