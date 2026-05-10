package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.po.Video;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface VideoService {

	/**
	 * 根据条件查询列表
	 */
	List<Video> findListByParam(VideoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(VideoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Video> findListByPage(VideoQuery param);

	/**
	 * 新增
	 */
	Integer add(Video bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<Video> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<Video> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(Video bean,VideoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(VideoQuery param);

	/**
	 * 根据VideoId查询对象
	 */
	Video getVideoByVideoId(Integer videoId);


	/**
	 * 根据VideoId修改
	 */
	Integer updateVideoByVideoId(Video bean,Integer videoId);


	/**
	 * 根据VideoId删除
	 */
	Integer deleteVideoByVideoId(Integer videoId);

    List<Video> selectVideo(VideoQuery query);

	Video selectSeeVideo(VideoQuery query);

    /**
     * 异步处理视频（如生成缩略图、转码等）
     * @param videoPath 视频路径
     * @param videoId 视频ID
     * @return 处理结果
     */
    java.util.concurrent.CompletableFuture<Boolean> processVideoAsync(String videoPath, Integer videoId);

    Integer selectUserLikes(Integer userIdFromToken);

	Integer selectUserPlays(Integer userIdFromToken);

	/**
	 * 根据标签搜索视频
	 */
	List<Video> selectByTags(String tag, VideoQuery query);
}