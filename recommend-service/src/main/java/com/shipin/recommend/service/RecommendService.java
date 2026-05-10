package com.shipin.recommend.service;

import com.shipin.recommend.entity.UserBehavior;
import com.shipin.recommend.entity.VideoInfo;
import com.shipin.recommend.repository.UserBehaviorRepository;
import com.shipin.recommend.repository.VideoInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {
    @Autowired
    private UserBehaviorRepository behaviorRepo;
    @Autowired
    private VideoInfoRepository videoRepo;

    // 【最终版】带调试日志 + 修复中文逗号问题 + 允许推荐看过的视频
    public List<VideoInfo> recommendByContent(Integer userId, int limit, int offset) {
        List<VideoInfo> recommended = new ArrayList<>();
        Set<Integer> recommendedVideoIds = new HashSet<>(); // 已推荐的视频ID，快速去重
        Map<String, Integer> tagWeightMap = new HashMap<>(); // 标签-权重映射

        System.out.println("\n========== 推荐开始 ==========");
        System.out.println("【输入】userId=" + userId + ", limit=" + limit + ", offset=" + offset);

        if (userId != null) {
            // 1. 获取用户最近10条观看行为
            List<UserBehavior> recentWatches = behaviorRepo.findTop10ByUserIdAndActionOrderByTimestampDesc(userId, "watch");
            System.out.println("【步骤1】查到用户观看行为数量：" + recentWatches.size());

            if (recentWatches.isEmpty()) {
                System.out.println("【提示】用户无观看行为，直接走热门兜底");
                return getHotVideos(limit, offset, recommendedVideoIds, recommended);
            }

            // 2. 统计每个视频的观看次数
            Map<Integer, Integer> videoWatchCount = new HashMap<>();
            for (UserBehavior behavior : recentWatches) {
                Integer videoId = behavior.getVideoId();
                videoWatchCount.put(videoId, videoWatchCount.getOrDefault(videoId, 0) + 1);
            }
            System.out.println("【步骤2】用户观看视频统计（videoId:观看次数）：" + videoWatchCount);

            // 3. 提取用户标签，计算权重（兼容中英文逗号）
            System.out.println("\n【步骤3】开始提取用户标签...");
            for (Map.Entry<Integer, Integer> entry : videoWatchCount.entrySet()) {
                Integer videoId = entry.getKey();
                Integer watchCount = entry.getValue(); // 观看次数作为权重基数

                VideoInfo video = videoRepo.findById(videoId).orElse(null);
                if (video == null) {
                    System.out.println("  [跳过] 视频" + videoId + "在数据库中不存在");
                    continue;
                }

                String tags = video.getTags();
                System.out.println("  [处理] 视频" + videoId + " - 原始标签：" + tags);

                if (tags == null || tags.trim().isEmpty()) {
                    System.out.println("  [跳过] 视频" + videoId + "无有效标签");
                    continue;
                }

                // 【核心修复】兼容中英文逗号：正则 [,，] 同时匹配英文逗号 , 和中文逗号 ，
                String[] tagArray = tags.split("[,，]");
                System.out.println("  [分割] 分割后标签数组：" + Arrays.toString(tagArray));

                for (String tag : tagArray) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        // 标签权重累加：观看次数越多的视频，标签权重越高
                        int oldWeight = tagWeightMap.getOrDefault(trimmedTag, 0);
                        int newWeight = oldWeight + watchCount;
                        tagWeightMap.put(trimmedTag, newWeight);
                        System.out.println("  [权重] 标签[" + trimmedTag + "]：" + oldWeight + " → " + newWeight);
                    }
                }
            }
            System.out.println("【步骤3完成】用户最终标签权重：" + tagWeightMap);

            // 4. 如果用户有标签，基于标签匹配推荐
            if (!tagWeightMap.isEmpty()) {
                System.out.println("\n【步骤4】开始基于标签匹配视频...");
                List<VideoInfo> candidateVideos = videoRepo.findAllByOrderByViewsDesc();
                List<ScoredVideo> scoredVideos = new ArrayList<>();

                for (VideoInfo video : candidateVideos) {
                    Integer videoId = video.getVideoId();

                    // 计算该视频与用户的匹配分数
                    int totalScore = 0;
                    String videoTags = video.getTags();
                    if (videoTags != null && !videoTags.trim().isEmpty()) {
                        String[] tagArray = videoTags.split("[,，]"); // 同样兼容中英文逗号
                        for (String tag : tagArray) {
                            String trimmedTag = tag.trim();
                            totalScore += tagWeightMap.getOrDefault(trimmedTag, 0);
                        }
                    }

                    // 有匹配分就加入候选列表
                    if (totalScore > 0) {
                        scoredVideos.add(new ScoredVideo(video, totalScore));
                        System.out.println("  [匹配] 视频" + videoId + "(" + video.getTitle() + ") - 匹配分数：" + totalScore);
                    }
                }

                // 【核心排序】先按匹配分降序（重合度高的在前），分相同按播放量降序
                System.out.println("  [排序] 对" + scoredVideos.size() + "个候选视频按分数+播放量排序...");
                scoredVideos.sort((a, b) -> {
                    if (b.score != a.score) {
                        return b.score - a.score; // 分数高的排前面
                    } else {
                        // 分数相同，播放量高的排前面（处理null防止空指针）
                        Integer viewsA = a.video.getViews() == null ? 0 : a.video.getViews();
                        Integer viewsB = b.video.getViews() == null ? 0 : b.video.getViews();
                        return viewsB - viewsA;
                    }
                });

                // 提取排序后的推荐结果
                System.out.println("  [提取] 开始提取推荐结果...");
                for (ScoredVideo sv : scoredVideos) {
                    VideoInfo video = sv.video;
                    if (!recommendedVideoIds.contains(video.getVideoId())) {
                        recommended.add(video);
                        recommendedVideoIds.add(video.getVideoId());
                        System.out.println("    → 加入推荐：视频" + video.getVideoId() + "(" + video.getTitle() + ") - 分数" + sv.score);
                        if (recommended.size() >= limit + offset) {
                            System.out.println("    [停止] 已达到推荐数量上限");
                            break;
                        }
                    }
                }
                System.out.println("【步骤4完成】标签匹配推荐到的视频数量：" + recommended.size());
            } else {
                System.out.println("【提示】用户无有效标签，直接走热门兜底");
            }
        } else {
            System.out.println("【提示】userId为null，直接走热门兜底");
        }

        // 5. 兜底补充热门视频
        System.out.println("\n【步骤5】开始补充热门视频兜底...");
        return getHotVideos(limit, offset, recommendedVideoIds, recommended);
    }

    // 抽取热门视频补充逻辑，复用代码
    private List<VideoInfo> getHotVideos(int limit, int offset, Set<Integer> recommendedVideoIds, List<VideoInfo> recommended) {
        int needCount = (limit + offset) - recommended.size();
        System.out.println("  [需要] 还需补充 " + needCount + " 个热门视频");

        if (needCount > 0) {
            List<VideoInfo> hotVideos = videoRepo.findAllByOrderByViewsDesc();
            int addedCount = 0;
            for (VideoInfo video : hotVideos) {
                Integer videoId = video.getVideoId();
                if (!recommendedVideoIds.contains(videoId)) {
                    recommended.add(video);
                    recommendedVideoIds.add(videoId);
                    addedCount++;
                    System.out.println("    → 加入热门兜底：视频" + videoId + "(" + video.getTitle() + ") - 播放量" + video.getViews());
                    if (addedCount >= needCount) {
                        break;
                    }
                }
            }
            System.out.println("  [补充] 实际补充了 " + addedCount + " 个热门视频");
        }

        // 分页处理
        System.out.println("\n【步骤6】应用分页 offset=" + offset + ", limit=" + limit);
        List<VideoInfo> result;
        if (offset > 0 && offset < recommended.size()) {
            int endIndex = Math.min(recommended.size(), offset + limit);
            result = recommended.subList(offset, endIndex);
            System.out.println("  [分页] 截取 [" + offset + ", " + endIndex + ")");
        } else if (offset >= recommended.size()) {
            result = new ArrayList<>();
            System.out.println("  [分页] offset超出范围，返回空列表");
        } else {
            int endIndex = Math.min(recommended.size(), limit);
            result = recommended.subList(0, endIndex);
            System.out.println("  [分页] 截取前 " + endIndex + " 条");
        }

        System.out.println("【最终结果】返回推荐视频数量：" + result.size());
        System.out.println("========== 推荐结束 ==========\n");
        return result;
    }

    // 辅助类：用于存储视频和对应的匹配分数
    private static class ScoredVideo {
        VideoInfo video;
        int score;

        public ScoredVideo(VideoInfo video, int score) {
            this.video = video;
            this.score = score;
        }
    }
}