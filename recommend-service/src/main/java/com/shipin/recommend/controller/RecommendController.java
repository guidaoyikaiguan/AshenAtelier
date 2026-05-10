package com.shipin.recommend.controller;

import com.shipin.recommend.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {
    @Autowired
    private RecommendService recommendService;

    @GetMapping("/videos")
    public Map<String, Object> getRecommendVideos(
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "24") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("code", 200);
            result.put("msg", "success");
            result.put("data", recommendService.recommendByContent(userId, limit, offset));
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "推荐服务异常: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}