package com.shipin.recommend.service;

import com.shipin.recommend.entity.UserBehavior;
import com.shipin.recommend.repository.UserBehaviorRepository;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserBehaviorConsumer {
    @Autowired
    private UserBehaviorRepository behaviorRepo;

    // 自动声明队列并监听
    @RabbitListener(
            queuesToDeclare = @Queue(name = "user.behavior", durable = "true"),
            containerFactory = "rabbitListenerContainerFactory" // 强制使用我们配置的带JSON转换器的工厂
    )
    public void processUserBehavior(Map<String, Object> data) {
        try {
            // 处理用户行为数据
            Integer userId = (Integer) data.get("userId");
            Integer videoId = (Integer) data.get("videoId");
            String action = (String) data.get("action");
            Long timestamp = (Long) data.get("timestamp");

            // 保存到数据库
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setVideoId(videoId);
            behavior.setAction(action);
            behavior.setTimestamp(timestamp);

            // 保存到数据库
            behaviorRepo.save(behavior);

            System.out.println("用户行为数据处理完成");
        } catch (Exception e) {
            System.err.println("处理用户行为数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}