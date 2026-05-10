package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.shipin.entity.vo.VideoHistoryVo;
import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.VideoHistoryQuery;
import com.shipin.entity.po.VideoHistory;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.VideoHistoryMapper;
import com.shipin.service.VideoHistoryService;
import com.shipin.utils.StringTools;


/**
 * 视频观看历史记录 业务接口实现
 */
@Service("videoHistoryService")
public class VideoHistoryServiceImpl implements VideoHistoryService {

	@Resource
	private VideoHistoryMapper<VideoHistory, VideoHistoryQuery> videoHistoryMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<VideoHistory> findListByParam(VideoHistoryQuery param) {
		return this.videoHistoryMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(VideoHistoryQuery param) {
		return this.videoHistoryMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<VideoHistory> findListByPage(VideoHistoryQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<VideoHistory> list = this.findListByParam(param);
		PaginationResultVO<VideoHistory> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(VideoHistory bean) {
		return this.videoHistoryMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<VideoHistory> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoHistoryMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<VideoHistory> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoHistoryMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(VideoHistory bean, VideoHistoryQuery param) {
		StringTools.checkParam(param);
		return this.videoHistoryMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(VideoHistoryQuery param) {
		StringTools.checkParam(param);
		return this.videoHistoryMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public VideoHistory getVideoHistoryById(Long id) {
		return this.videoHistoryMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateVideoHistoryById(VideoHistory bean, Long id) {
		return this.videoHistoryMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteVideoHistoryById(Long id) {
		return this.videoHistoryMapper.deleteById(id);
	}

	/**
	 * 根据UserIdAndVideoId获取对象
	 */
	@Override
	public VideoHistory getVideoHistoryByUserIdAndVideoId(Long userId, Long videoId) {
		return this.videoHistoryMapper.selectByUserIdAndVideoId(userId, videoId);
	}

	/**
	 * 根据UserIdAndVideoId修改
	 */
	@Override
	public Integer updateVideoHistoryByUserIdAndVideoId(VideoHistory bean, Long userId, Long videoId) {
		return this.videoHistoryMapper.updateByUserIdAndVideoId(bean, userId, videoId);
	}

	/**
	 * 根据UserIdAndVideoId删除
	 */
	@Override
	public Integer deleteVideoHistoryByUserIdAndVideoId(Long userId, Long videoId) {
		return this.videoHistoryMapper.deleteByUserIdAndVideoId(userId, videoId);
	}

	@Override
	public List<VideoHistoryVo> getVideoHistory(VideoHistoryQuery videoHistoryQuery) {
		List<VideoHistoryVo> videoHistoryList=videoHistoryMapper.getVideoHistory(videoHistoryQuery);
		return videoHistoryList;
	}

	@Override
	public List searchVideoHistory(Integer userId, String keyword) {
		List videoHistoryList=videoHistoryMapper.searchVideoHistory(userId,keyword);
		return videoHistoryList;
	}
}