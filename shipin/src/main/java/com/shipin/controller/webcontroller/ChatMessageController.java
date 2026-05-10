package com.shipin.controller.webcontroller;

import com.shipin.annotation.LoginRequired;
import com.shipin.entity.po.ChatMessage;
import com.shipin.entity.query.ChatSession;
import com.shipin.entity.vo.ChatSessionWithUser;
import com.shipin.entity.vo.Result;
import com.shipin.mappers.ChatSessionMapper;
import com.shipin.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/chat")
@RestController
public class ChatMessageController {
    @Resource
    private ChatMessageService chatMessageService;
    @Autowired
    private ChatSessionMapper chatSessionMapper;

    //获取聊天记录
    @LoginRequired
    @RequestMapping("/getMessages")
    public Result getMessages(@RequestParam("userId") Integer userId,
                                 @RequestParam("otherUserId") Integer otherUserId) {
        try {
            List <ChatMessage> messages=chatMessageService.getChatMessage(userId,otherUserId);
            return Result.success(messages);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("获取聊天记录失败");
        }
    }
     //发送消息
    @RequestMapping("/sendMessage")
    @LoginRequired
    public Result sendMessage(@RequestBody Map<String, Object> body) {
            try {
                Integer senderId = body.get("senderId") != null ? ((Number) body.get("senderId")).intValue() : null;
                Integer receiverId = body.get("receiverId") != null ? ((Number) body.get("receiverId")).intValue() : null;
                String content = (String) body.get("content");
                chatMessageService.sendMessage(senderId, receiverId, content);
                return Result.success("消息发送成功");
            }catch (Exception e){
                e.printStackTrace();
                return Result.error("消息发送失败");
            }
    }
     //标记消息已读
    @RequestMapping("/markAsRead")
    @LoginRequired
    public Result markAsRead(@RequestParam("userId") Integer userId,
                           @RequestParam("otherUserId") Integer otherUserId) {
        try {
            chatMessageService.markAllAsRead(userId, otherUserId);
            return Result.success("标记已读成功");
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("标记已读失败");
        }
    }
     //获取与其中一个用户未读消息数量
    @RequestMapping("/getUnreadCount")
    @LoginRequired
    public Result getUnreadCount(@RequestParam("userId") Integer userId,
                                    @RequestParam("otherUserId") Integer otherUserId) {
           try {
               Integer count=chatMessageService.getUnreadCount(userId,otherUserId);
               return Result.success(count);
           }catch (Exception e){
                e.printStackTrace();
                return Result.error("获取未读消息数量失败");
           }
        }
        //获取与所有用户未读消息数量
    @RequestMapping("/getTotalUnreadCount")
    @LoginRequired
    public Result getTotalUnreadCount(@RequestParam("userId") Integer userId) {
        try {
            Integer count=chatMessageService.getAllUnreadCount(userId);
            return Result.success(count);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("获取未读消息数量失败");
        }
    }
    //获取用户的聊天会话列表
    @RequestMapping("/getSessions")
    @LoginRequired
    public Result getSessions(@RequestParam("userId") Integer userId) {
        try {
            List<ChatSessionWithUser> sessions=chatMessageService.getSessions(userId);
            return Result.success(sessions);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("获取聊天会话列表失败");
        }
    }
    //当用户第一次跟另一个用户聊天时，给左侧导航栏加一条
    @LoginRequired
    @RequestMapping("/addChatSession")
    public Result addChatSession(@RequestParam("userId") Integer userId, @RequestParam("otherUserId") Integer otherUserId){
        try {
            System.out.println("添加聊天会话参数:" + userId + ", " + otherUserId);
            ChatSession session=chatSessionMapper.getSession(userId, otherUserId);
            System.out.println("获取会话结果:" + session);
            if(session==null){
                session=new ChatSession();
                session.setUserId1(userId);
                session.setUserId2(otherUserId);
                System.out.println("创建新会话:" + session);
                int result=chatSessionMapper.insertSession(session);
                System.out.println("插入会话结果:" + result);
            }
            return Result.success("添加聊天会话成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加聊天会话失败: " + e.getMessage());
        }
    }
}
