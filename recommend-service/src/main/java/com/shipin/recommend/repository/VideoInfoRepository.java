package com.shipin.recommend.repository;

import com.shipin.recommend.entity.VideoInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoInfoRepository extends JpaRepository<VideoInfo, Integer> {
    List<VideoInfo> findByTagsContainingOrderByViewsDesc(String tag);
    List<VideoInfo> findAllByOrderByViewsDesc();
}