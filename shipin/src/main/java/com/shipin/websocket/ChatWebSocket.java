package com.shipin.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipin.entity.po.ChatMessage;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/chat/{userId}")
@Component
public class ChatWebSocket {
    
    private static final Map<Integer, Session> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        sessions.put(userId, session);
        System.out.println("用户 " + userId + " 连接成功，当前在线用户数: " + sessions.size());
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("userId") Integer userId) {
        sessions.remove(userId);
        System.out.println("用户 " + userId + " 断开连接，当前在线用户数: " + sessions.size());
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket错误: " + error.getMessage());
        error.printStackTrace();
    }
    
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Integer userId) {
        System.out.println("收到用户 " + userId + " 的消息: " + message);
    }
    
    public static void sendMessageToUser(Integer userId, ChatMessage message) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.getBasicRemote().sendText(jsonMessage);
                System.out.println("向用户 " + userId + " 发送消息: " + jsonMessage);
            } catch (IOException e) {
                System.err.println("发送消息失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("用户 " + userId + " 不在线");
        }
    }
    
    public static boolean isUserOnline(Integer userId) {
        Session session = sessions.get(userId);
        return session != null && session.isOpen();
    }
}