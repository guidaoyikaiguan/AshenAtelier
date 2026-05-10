package com.shipin.service;

import com.shipin.entity.document.VideoDocument;
import com.shipin.entity.po.User;

import java.util.List;

public interface VideoSearchService {

    void saveVideo(VideoDocument videoDocument);

    void deleteVideo(Integer videoId);

    VideoDocument findById(Integer videoId);

    List<VideoDocument> searchByKeyword(String keyword, int page, int size);

    List<User> searchUsersByKeyword(String keyword, int page, int size);

    List<VideoDocument> getRecommendedVideos(int page, int size);

    List<VideoDocument> searchByCategory(Integer categoryId, int page, int size);

    List<VideoDocument> searchByTags(String tags, int page, int size);

    void syncVideoFromDatabase(Integer videoId);

    void syncAllVideosFromDatabase();
}