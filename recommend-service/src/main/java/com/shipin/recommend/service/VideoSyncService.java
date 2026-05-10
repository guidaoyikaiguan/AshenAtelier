package com.shipin.recommend.service;

import com.shipin.recommend.entity.VideoInfo;
import com.shipin.recommend.repository.VideoInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

@Service
public class VideoSyncService {

    @Autowired
    private VideoInfoRepository videoRepo;

    private JdbcTemplate jdbcTemplate;

    @Value("${existing.datasource.url}")
    private String existingDbUrl;

    @Value("${existing.datasource.username}")
    private String existingDbUsername;

    @Value("${existing.datasource.password}")
    private String existingDbPassword;

    // 初始化方法（连接 shipin 库）
    @Autowired
    public void init() {
        try {
            SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
            dataSource.setDriver(DriverManager.getDriver(existingDbUrl));
            dataSource.setUrl(existingDbUrl);
            dataSource.setUsername(existingDbUsername);
            dataSource.setPassword(existingDbPassword);

            this.jdbcTemplate = new JdbcTemplate(dataSource);
        } catch (Exception e) {
            System.err.println("初始化JdbcTemplate失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int toInt(Object obj) {
        if (obj == null) return 0;
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception e) {
            return 0;
        }
    }
    // 每小时同步视频
    @Scheduled(fixedRate = 3600000)
    public void syncVideos() {
        try {
            if (jdbcTemplate == null) {
                System.err.println("JdbcTemplate未初始化");
                return;
            }

            // 从 shipin 库查询视频（正确！）
            List<Map<String, Object>> videoData = jdbcTemplate.queryForList(
                    "SELECT v.video_id, v.title, v.cover_url, v.tags, v.play_count, v.user_id,v.categroy_id,v.like_count,v.play_count,v.duration,u.nick_name from video v join user u on v.user_id = u.user_id where `status` = 1"
            );

            for (Map<String, Object> data : videoData) {
                VideoInfo video = new VideoInfo();
                // ✅ 以下是 100% 正确的字段映射
                video.setVideoId(toInt(data.get("video_id")));
                video.setNickname((String) data.get("nick_name"));
                video.setTitle((String) data.get("title"));
                video.setCover((String) data.get("cover_url"));
                video.setTags((String) data.get("tags"));
                video.setViews(toInt(data.get("play_count")));
                video.setUserId(toInt(data.get("user_id")));
                video.setCategoryId(toInt(data.get("category_id")));
                video.setLikes(toInt(data.get("like_count")));
                video.setPlayCount(toInt(data.get("play_count")));
                video.setDuration((String) (data.get("duration")));
                // 保存到 recommend 库
                videoRepo.save(video);
            }

            System.out.println("视频数据同步完成");
        } catch (Exception e) {
            System.err.println("视频数据同步失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}