package com.shipin.mappers;

import com.shipin.entity.query.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatSessionMapper {
    // 创建会话
    int insertSession(ChatSession session);

    // 获取会话
    ChatSession getSession(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // 更新会话的最后一条消息
    int updateLastMessage(@Param("sessionId") Integer sessionId, @Param("lastMessageId") Integer lastMessageId);
}