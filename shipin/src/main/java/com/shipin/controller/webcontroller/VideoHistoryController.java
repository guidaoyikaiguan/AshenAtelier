package com.shipin.controller.webcontroller;

import com.shipin.annotation.LoginRequired;
import com.shipin.entity.po.VideoHistory;
import com.shipin.entity.query.VideoHistoryQuery;
import com.shipin.entity.vo.Result;
import com.shipin.entity.vo.VideoHistoryVo;
import com.shipin.service.VideoHistoryService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("VideoHistoryController")
@RequestMapping("/api/videoHistory")
public class VideoHistoryController {
    @Resource
    private VideoHistoryService videoHistoryService;
    
    @Resource
    private RabbitTemplate rabbitTemplate;

    //TODO 现在视频不记录上回播放到哪，历史播放也只是显示视频的总时间而不显示上回播放到哪

    //当用户观看一条视频后，将其添加进历史记录表
    @RequestMapping("/addVideoHistory")
    @LoginRequired
    public Result addVideoHistory(@RequestParam("videoId") Integer videoId,
                                  @RequestParam("userId") Integer userId,
                                  @RequestParam("progress") Integer progress,
                                  @RequestParam("watchDuration") Integer watchDuration) {
        //这里可以添加一些业务逻辑，比如检查用户是否已经观看过该视频，或者更新观看时间等
        VideoHistoryQuery videoHistoryQuery = new VideoHistoryQuery();
        videoHistoryQuery.setUserId(userId.longValue());
        videoHistoryQuery.setVideoId(videoId.longValue());
        List<VideoHistory> listByParam = videoHistoryService.findListByParam(videoHistoryQuery);
        if (listByParam != null && listByParam.size() > 0) {
            //如果已经观看过该视频，则更新观看时间和观看进度
            VideoHistory videoHistory = listByParam.get(0);
            videoHistory.setProgress(progress);
            videoHistory.setWatchDuration(watchDuration);
            videoHistory.setLastWatchTime(new Date());
            videoHistory.setUpdatedAt(new Date());
            videoHistoryService.updateByParam(videoHistory, videoHistoryQuery);
        } else {
            //如果没有观看过该视频，则添加一条新的记录
            VideoHistory videoHistory = new VideoHistory();
            videoHistory.setUserId(userId.longValue());
            videoHistory.setVideoId(videoId.longValue());
            videoHistory.setProgress(progress);
            videoHistory.setWatchDuration(watchDuration);
            videoHistory.setLastWatchTime(new Date());
            videoHistory.setUpdatedAt(new Date());
            videoHistory.setCreatedAt(new Date());
            videoHistoryService.add(videoHistory);
        }
        
        // 发送用户行为数据到消息队列
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("videoId", videoId);
            data.put("action", "watch");
            data.put("timestamp", System.currentTimeMillis());
            rabbitTemplate.convertAndSend("user.behavior", data);
            System.out.println("发送用户行为数据: " + data);
        } catch (Exception e) {
            System.err.println("发送用户行为数据失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Result.success("视频观看历史记录添加成功");
    }

    //用户可以查询自己的观看历史记录
    @RequestMapping("/getVideoHistory")
    @LoginRequired
    public Result getVideoHistory(@RequestParam("userId") Integer userId) {
        VideoHistoryQuery videoHistoryQuery = new VideoHistoryQuery();
        videoHistoryQuery.setUserId(userId.longValue());
        List<VideoHistoryVo> listByParam = videoHistoryService.getVideoHistory(videoHistoryQuery);
        return Result.success(listByParam);
    }
    //用户可以删除自己的观看历史记录
    @RequestMapping("/deleteVideoHistory")
    @LoginRequired
    public Result deleteVideoHistory(@RequestParam("userId") Integer userId,
                                    @RequestParam("videoId") Integer videoId) {
        VideoHistoryQuery videoHistoryQuery = new VideoHistoryQuery();
        videoHistoryQuery.setUserId(userId.longValue());
        videoHistoryQuery.setVideoId(videoId.longValue());
        videoHistoryService.deleteByParam(videoHistoryQuery);
        return Result.success("视频观看历史记录删除成功");
    }
    //用户可以清空自己的观看历史记录
    @RequestMapping("/clearVideoHistory")
    @LoginRequired
    public Result clearVideoHistory(@RequestParam("userId") Integer userId) {
        VideoHistoryQuery videoHistoryQuery = new VideoHistoryQuery();
        videoHistoryQuery.setUserId(userId.longValue());
        videoHistoryService.deleteByParam(videoHistoryQuery);
        return Result.success("视频观看历史记录清空成功");
    }
    //批量删除观看历史记录
    @RequestMapping("/deleteBatchVideoHistory")
    @LoginRequired
    public Result deleteBatchVideoHistory(@RequestParam("userId") Integer userId,
                                         @RequestParam("videoIds") List<Integer> videoIds) {
        for (Integer videoId : videoIds) {
            VideoHistoryQuery videoHistoryQuery = new VideoHistoryQuery();
            videoHistoryQuery.setUserId(userId.longValue());
            videoHistoryQuery.setVideoId(videoId.longValue());
            videoHistoryService.deleteByParam(videoHistoryQuery);
        }
        return Result.success("删除成功");
    }
    //在用户观看视频时，更新观看进度和观看时间
    @RequestMapping("/updateVideoHistory")
    @LoginRequired
    public Result updateVideoHistory(@RequestParam("userId") Integer userId,
                                     @RequestParam("videoId") Integer videoId,
                                     @RequestParam("progress") Integer progress,
                                     @RequestParam("watchDuration") Integer watchDuration) {
        VideoHistoryQuery videoHistoryQuery = new VideoHistoryQuery();
        videoHistoryQuery.setUserId(userId.longValue());
        videoHistoryQuery.setVideoId(videoId.longValue());
        List<VideoHistory> listByParam = videoHistoryService.findListByParam(videoHistoryQuery);
        if (listByParam != null && listByParam.size() > 0) {
            VideoHistory videoHistory = listByParam.get(0);
            videoHistory.setProgress(progress);
            videoHistory.setWatchDuration(watchDuration);
            videoHistory.setLastWatchTime(new Date());
            videoHistory.setUpdatedAt(new Date());
            videoHistoryService.updateByParam(videoHistory, videoHistoryQuery);
            return Result.success("视频观看历史记录更新成功");
        } else {
            return Result.error("未找到对应的视频观看历史记录");
        }
    }
    //在历史记录中模糊查询
    @RequestMapping("/searchVideoHistory")
    @LoginRequired
    public Result searchVideoHistory(@RequestParam("userId") Integer userId,
                                    @RequestParam("keyword") String keyword) {
        List videoHistory=videoHistoryService.searchVideoHistory(userId,keyword);
        return Result.success(videoHistory);
    }
}
