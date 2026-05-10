package com.shipin.service.impl;

import com.shipin.entity.po.ChatMessage;
import com.shipin.entity.query.ChatSession;
import com.shipin.entity.vo.ChatSessionWithUser;
import com.shipin.mappers.ChatMessageMapper;
import com.shipin.mappers.ChatSessionMapper;
import com.shipin.service.AiAgentService;
import com.shipin.service.ChatMessageService;
import com.shipin.websocket.ChatWebSocket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {
    @Resource
    private ChatMessageMapper chatMessageMapper;
    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private AiAgentService aiAgentService;

    @Override
    @Transactional
    public List<ChatMessage> getChatMessage(Integer userId,Integer otherUserId) {
        try {
            //标记所以消息为已读
            chatMessageMapper.markAllAsRead(userId, otherUserId);
            //获取聊天记录
            List<ChatMessage> chatMessages=chatMessageMapper.getMessages(userId, otherUserId);
            return chatMessages;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public void sendMessage(Integer senderId, Integer receiverId, String content) {
        // AI 助手路由：receiverId == 0 时发送给 AI
        if (receiverId != null && receiverId == 0) {
            // 1. 保存用户消息
            ChatMessage userMessage = new ChatMessage();
            userMessage.setSenderId(senderId);
            userMessage.setReceiverId(0);
            userMessage.setContent(content);
            userMessage.setSendTime(new Date());
            chatMessageMapper.insertMessage(userMessage);

            // 2. 调用 AI 获取回复（同步，等 DeepSeek 返回）
            String aiReply = aiAgentService.chatWithAi(senderId, content);

            // 3. 保存 AI 回复
            ChatMessage aiMessage = new ChatMessage();
            aiMessage.setSenderId(0);
            aiMessage.setReceiverId(senderId);
            aiMessage.setContent(aiReply);
            aiMessage.setSendTime(new Date());
            chatMessageMapper.insertMessage(aiMessage);

            // 4. 获取或新建会话，更新最后一条消息
            ChatSession session = chatSessionMapper.getSession(senderId, 0);
            if (session == null) {
                session = new ChatSession();
                session.setUserId1(senderId);
                session.setUserId2(0);
                chatSessionMapper.insertSession(session);
            }
            chatSessionMapper.updateLastMessage(session.getId(), aiMessage.getId());

            // 5. 通过 WebSocket 推送 AI 回复给用户
            ChatWebSocket.sendMessageToUser(senderId, aiMessage);
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setContent(content);
        chatMessageMapper.insertMessage(chatMessage);

        // 获取或新建会话
        ChatSession session = chatSessionMapper.getSession(senderId, receiverId);
        if (session == null) {
            session = new ChatSession();
            session.setUserId1(senderId);
            session.setUserId2(receiverId);
            chatSessionMapper.insertSession(session);
        }
        // 更新会话的最后一条消息
        chatSessionMapper.updateLastMessage(session.getId(), chatMessage.getId());
        // 通过WebSocket推送消息给接收方
        ChatWebSocket.sendMessageToUser(receiverId, chatMessage);
    }

    @Override
    public void markAllAsRead(Integer userId, Integer otherUserId) {
        chatMessageMapper.markAllAsRead(userId, otherUserId);
    }

    @Override
    public Integer getUnreadCount(Integer userId, Integer otherUserId) {
        Integer count=chatMessageMapper.getUnreadCount(userId, otherUserId);
        return count;
    }

    @Override
    public Integer getAllUnreadCount(Integer userId) {
        Integer count=chatMessageMapper.getAllUnreadCount(userId);
        return count;
    }

    @Override
    public List<ChatSessionWithUser> getSessions(Integer userId) {
        List<ChatSessionWithUser> sessions=chatMessageMapper.getSessions(userId);
        return sessions;
    }

}
