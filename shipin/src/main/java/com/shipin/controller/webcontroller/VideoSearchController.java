package com.shipin.controller.webcontroller;

import com.shipin.controller.ABaseController;
import com.shipin.entity.document.VideoDocument;
import com.shipin.entity.po.User;
import com.shipin.entity.vo.Result;
import com.shipin.service.VideoSearchService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video/search")
public class VideoSearchController extends ABaseController {

    @Resource
    private VideoSearchService videoSearchService;

    @GetMapping("/keyword")
    public Result searchByKeyword(@RequestParam String keyword, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<VideoDocument> videos = videoSearchService.searchByKeyword(keyword, page, size);
            List<User> users = videoSearchService.searchUsersByKeyword(keyword, page, size);
            result.put("videos", videos);
            result.put("users", users);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    @GetMapping("/recommend")
    public Result getRecommendedVideos(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            List<VideoDocument> result = videoSearchService.getRecommendedVideos(page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取推荐视频失败: " + e.getMessage());
        }
    }

    @GetMapping("/category")
    public Result searchByCategory(@RequestParam Integer categoryId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            List<VideoDocument> result = videoSearchService.searchByCategory(categoryId, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取分类视频失败: " + e.getMessage());
        }
    }

    @GetMapping("/byTags")
    public Result searchByTags(@RequestParam String tags, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            List<VideoDocument> result = videoSearchService.searchByTags(tags, page, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("搜索标签视频失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync/{videoId}")
    public Result syncVideo(@PathVariable Integer videoId) {
        try {
            videoSearchService.syncVideoFromDatabase(videoId);
            return Result.success("同步成功");
        } catch (Exception e) {
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    @PostMapping("/sync/all")
    public Result syncAllVideos() {
        try {
            videoSearchService.syncAllVideosFromDatabase();
            return Result.success("同步成功");
        } catch (Exception e) {
            return Result.error("同步失败: " + e.getMessage());
        }
    }
}
