package com.shipin.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.shipin.entity.po.MomentLike;
import com.shipin.entity.query.MomentLikeQuery;
import com.shipin.mappers.MomentLikeMapper;
import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.MomentQuery;
import com.shipin.entity.po.Moment;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.MomentMapper;
import com.shipin.service.MomentService;
import com.shipin.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 *  业务接口实现
 */
@Service("momentService")
public class MomentServiceImpl implements MomentService {

	@Resource
	private MomentMapper<Moment, MomentQuery> momentMapper;
	@Resource
	private MomentLikeMapper<MomentLike, MomentLikeQuery>momentLikeMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Moment> findListByParam(MomentQuery param) {
		return this.momentMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(MomentQuery param) {
		return this.momentMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Moment> findListByPage(MomentQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Moment> list = this.findListByParam(param);
		PaginationResultVO<Moment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Moment bean) {
		return this.momentMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Moment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Moment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Moment bean, MomentQuery param) {
		StringTools.checkParam(param);
		return this.momentMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(MomentQuery param) {
		StringTools.checkParam(param);
		return this.momentMapper.deleteByParam(param);
	}

	/**
	 * 根据MomentId获取对象
	 */
	@Override
	public Moment getMomentByMomentId(Integer momentId) {
		return this.momentMapper.selectByMomentId(momentId);
	}

	/**
	 * 根据MomentId修改
	 */
	@Override
	public Integer updateMomentByMomentId(Moment bean, Integer momentId) {
		return this.momentMapper.updateByMomentId(bean, momentId);
	}

	/**
	 * 根据MomentId删除
	 */
	@Override
	public Integer deleteMomentByMomentId(Integer momentId) {
		return this.momentMapper.deleteByMomentId(momentId);
	}

	@Override
	@Transactional
	public void likeMoment(Integer userId, int momentId) {
		//给动态表中的点赞数量加1
		MomentQuery momentQuery = new MomentQuery();
		momentQuery.setMomentId(momentId);
		List<Moment> moments = momentMapper.selectList(momentQuery);
		Moment moment = moments.get(0);
		if(moment.getLikeCount() ==null){
			moment.setLikeCount(0);
		}
		Moment likeMoment = new Moment();
		likeMoment.setMomentId(momentId);
		likeMoment.setLikeCount(moment.getLikeCount() + 1);
		momentMapper.updateByMomentId(likeMoment, momentId);
		//给动态点赞表添加这个用户点赞的记录
		MomentLike momentLike = new MomentLike();
		momentLike.setMomentId(momentId);
		momentLike.setUserId(userId);
		momentLike.setCreateTime(new Date());
		momentLikeMapper.insert(momentLike);
	}

	@Override
	@Transactional
	public void dellikeMoment(Integer userId, int momentId) {
		//给动态表中的点赞数量减1
		MomentQuery momentQuery = new MomentQuery();
		momentQuery.setMomentId(momentId);
		List<Moment> moments = momentMapper.selectList(momentQuery);
		Moment moment = moments.get(0);
		Moment likeMoment = new Moment();
		likeMoment.setMomentId(momentId);
		likeMoment.setLikeCount(moment.getLikeCount() - 1);
		momentMapper.updateByMomentId(likeMoment, momentId);
		//给动态点赞表删除这个用户点赞的记录
		MomentLikeQuery momentLikeQuery = new MomentLikeQuery();
		momentLikeQuery.setMomentId(momentId);
		momentLikeQuery.setUserId(userId);
		momentLikeMapper.deleteByParam(momentLikeQuery);
	}
}