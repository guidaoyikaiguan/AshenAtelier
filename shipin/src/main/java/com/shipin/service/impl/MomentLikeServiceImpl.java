package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.MomentLikeQuery;
import com.shipin.entity.po.MomentLike;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.MomentLikeMapper;
import com.shipin.service.MomentLikeService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("momentLikeService")
public class MomentLikeServiceImpl implements MomentLikeService {

	@Resource
	private MomentLikeMapper<MomentLike, MomentLikeQuery> momentLikeMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<MomentLike> findListByParam(MomentLikeQuery param) {
		return this.momentLikeMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(MomentLikeQuery param) {
		return this.momentLikeMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<MomentLike> findListByPage(MomentLikeQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<MomentLike> list = this.findListByParam(param);
		PaginationResultVO<MomentLike> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(MomentLike bean) {
		return this.momentLikeMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<MomentLike> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentLikeMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<MomentLike> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentLikeMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(MomentLike bean, MomentLikeQuery param) {
		StringTools.checkParam(param);
		return this.momentLikeMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(MomentLikeQuery param) {
		StringTools.checkParam(param);
		return this.momentLikeMapper.deleteByParam(param);
	}

	/**
	 * 根据LikeId获取对象
	 */
	@Override
	public MomentLike getMomentLikeByLikeId(Integer likeId) {
		return this.momentLikeMapper.selectByLikeId(likeId);
	}

	/**
	 * 根据LikeId修改
	 */
	@Override
	public Integer updateMomentLikeByLikeId(MomentLike bean, Integer likeId) {
		return this.momentLikeMapper.updateByLikeId(bean, likeId);
	}

	/**
	 * 根据LikeId删除
	 */
	@Override
	public Integer deleteMomentLikeByLikeId(Integer likeId) {
		return this.momentLikeMapper.deleteByLikeId(likeId);
	}

	/**
	 * 根据UserIdAndMomentId获取对象
	 */
	@Override
	public MomentLike getMomentLikeByUserIdAndMomentId(Integer userId, Integer momentId) {
		return this.momentLikeMapper.selectByUserIdAndMomentId(userId, momentId);
	}

	/**
	 * 根据UserIdAndMomentId修改
	 */
	@Override
	public Integer updateMomentLikeByUserIdAndMomentId(MomentLike bean, Integer userId, Integer momentId) {
		return this.momentLikeMapper.updateByUserIdAndMomentId(bean, userId, momentId);
	}

	/**
	 * 根据UserIdAndMomentId删除
	 */
	@Override
	public Integer deleteMomentLikeByUserIdAndMomentId(Integer userId, Integer momentId) {
		return this.momentLikeMapper.deleteByUserIdAndMomentId(userId, momentId);
	}
}