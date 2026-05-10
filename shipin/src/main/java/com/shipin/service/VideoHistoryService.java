package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.VideoHistoryQuery;
import com.shipin.entity.po.VideoHistory;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.vo.VideoHistoryVo;


/**
 * 视频观看历史记录 业务接口
 */
public interface VideoHistoryService {

	/**
	 * 根据条件查询列表
	 */
	List<VideoHistory> findListByParam(VideoHistoryQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(VideoHistoryQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<VideoHistory> findListByPage(VideoHistoryQuery param);

	/**
	 * 新增
	 */
	Integer add(VideoHistory bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoHistory> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoHistory> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(VideoHistory bean,VideoHistoryQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(VideoHistoryQuery param);

	/**
	 * 根据Id查询对象
	 */
	VideoHistory getVideoHistoryById(Long id);


	/**
	 * 根据Id修改
	 */
	Integer updateVideoHistoryById(VideoHistory bean,Long id);


	/**
	 * 根据Id删除
	 */
	Integer deleteVideoHistoryById(Long id);


	/**
	 * 根据UserIdAndVideoId查询对象
	 */
	VideoHistory getVideoHistoryByUserIdAndVideoId(Long userId,Long videoId);


	/**
	 * 根据UserIdAndVideoId修改
	 */
	Integer updateVideoHistoryByUserIdAndVideoId(VideoHistory bean,Long userId,Long videoId);


	/**
	 * 根据UserIdAndVideoId删除
	 */
	Integer deleteVideoHistoryByUserIdAndVideoId(Long userId,Long videoId);

	List<VideoHistoryVo> getVideoHistory(VideoHistoryQuery videoHistoryQuery);

	List searchVideoHistory(Integer userId, String keyword);
}