package com.shipin.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipin.entity.po.Comment;
import com.shipin.entity.po.Danmaku;
import com.shipin.entity.po.Video;
import com.shipin.service.AiAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("aiAgentService")
public class AiAgentServiceImpl implements AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentServiceImpl.class);

    @Value("${ai-agent.service.url:http://localhost:8084}")
    private String aiServiceUrl;

    @Resource
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void moderateVideo(Video video) {
        log.info("AI video moderation START for videoId={}, title={}, aiServiceUrl={}",
                video.getVideoId(), video.getTitle(), aiServiceUrl);
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("content_type", "video");
            req.put("content_id", video.getVideoId());
            req.put("title", video.getTitle());
            req.put("description", video.getDescription());
            req.put("tags", video.getTags());

            String url = aiServiceUrl + "/api/ai/moderation/video";
            log.info("Calling AI moderation API: {}", url);
            String response = restTemplate.postForObject(url, req, String.class);
            log.info("AI video moderation completed for videoId={}, response={}", video.getVideoId(), response);
        } catch (Exception e) {
            log.error("AI video moderation failed for videoId={}: {}", video.getVideoId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void moderateComment(Comment comment) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("content_type", "comment");
            req.put("content_id", comment.getCommentId());
            req.put("content", comment.getContent());

            String url = aiServiceUrl + "/api/ai/moderation/comment";
            restTemplate.postForObject(url, req, String.class);
            log.info("AI comment moderation submitted for commentId={}", comment.getCommentId());
        } catch (Exception e) {
            log.error("AI comment moderation failed for commentId={}: {}", comment.getCommentId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void moderateDanmaku(Danmaku danmaku) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("content_type", "danmaku");
            req.put("content_id", danmaku.getId());
            req.put("content", danmaku.getContent());

            String url = aiServiceUrl + "/api/ai/moderation/danmaku";
            restTemplate.postForObject(url, req, String.class);
            log.info("AI danmaku moderation submitted for danmakuId={}", danmaku.getId());
        } catch (Exception e) {
            log.error("AI danmaku moderation failed for danmakuId={}: {}", danmaku.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void enrichVideoTags(Video video) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("video_id", video.getVideoId());
            req.put("title", video.getTitle());
            req.put("description", video.getDescription() != null ? video.getDescription() : "");
            req.put("existing_tags", video.getTags() != null ? video.getTags() : "");

            String url = aiServiceUrl + "/api/ai/tagging/enrich";
            restTemplate.postForObject(url, req, String.class);
            log.info("AI tag enrichment submitted for videoId={}", video.getVideoId());
        } catch (Exception e) {
            log.error("AI tag enrichment failed for videoId={}: {}", video.getVideoId(), e.getMessage());
        }
    }

    @Override
    public String chatWithAi(Integer userId, String message) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("user_id", userId);
            req.put("message", message);

            String url = aiServiceUrl + "/api/ai/chat/message";
            String response = restTemplate.postForObject(url, req, String.class);
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.get("data");
                if (data != null && data.has("reply")) {
                    return data.get("reply").asText();
                }
            }
        } catch (Exception e) {
            log.error("AI chat failed for userId={}: {}", userId, e.getMessage());
        }
        return "抱歉，AI 助手暂时不可用，请稍后再试。";
    }
}
