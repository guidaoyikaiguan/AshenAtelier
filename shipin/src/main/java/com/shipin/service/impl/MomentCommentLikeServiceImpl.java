package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.MomentCommentLikeQuery;
import com.shipin.entity.po.MomentCommentLike;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.MomentCommentLikeMapper;
import com.shipin.service.MomentCommentLikeService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("momentCommentLikeService")
public class MomentCommentLikeServiceImpl implements MomentCommentLikeService {

	@Resource
	private MomentCommentLikeMapper<MomentCommentLike, MomentCommentLikeQuery> momentCommentLikeMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<MomentCommentLike> findListByParam(MomentCommentLikeQuery param) {
		return this.momentCommentLikeMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(MomentCommentLikeQuery param) {
		return this.momentCommentLikeMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<MomentCommentLike> findListByPage(MomentCommentLikeQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<MomentCommentLike> list = this.findListByParam(param);
		PaginationResultVO<MomentCommentLike> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(MomentCommentLike bean) {
		return this.momentCommentLikeMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<MomentCommentLike> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentCommentLikeMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<MomentCommentLike> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentCommentLikeMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(MomentCommentLike bean, MomentCommentLikeQuery param) {
		StringTools.checkParam(param);
		return this.momentCommentLikeMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(MomentCommentLikeQuery param) {
		StringTools.checkParam(param);
		return this.momentCommentLikeMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public MomentCommentLike getMomentCommentLikeByUserId(Integer userId) {
		return this.momentCommentLikeMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateMomentCommentLikeByUserId(MomentCommentLike bean, Integer userId) {
		return this.momentCommentLikeMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteMomentCommentLikeByUserId(Integer userId) {
		return this.momentCommentLikeMapper.deleteByUserId(userId);
	}
}