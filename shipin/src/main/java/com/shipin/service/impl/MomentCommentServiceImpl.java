package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.MomentCommentQuery;
import com.shipin.entity.po.MomentComment;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.MomentCommentMapper;
import com.shipin.service.MomentCommentService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("momentCommentService")
public class MomentCommentServiceImpl implements MomentCommentService {

	@Resource
	private MomentCommentMapper<MomentComment, MomentCommentQuery> momentCommentMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<MomentComment> findListByParam(MomentCommentQuery param) {
		return this.momentCommentMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(MomentCommentQuery param) {
		return this.momentCommentMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<MomentComment> findListByPage(MomentCommentQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<MomentComment> list = this.findListByParam(param);
		PaginationResultVO<MomentComment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(MomentComment bean) {
		return this.momentCommentMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<MomentComment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentCommentMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<MomentComment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentCommentMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(MomentComment bean, MomentCommentQuery param) {
		StringTools.checkParam(param);
		return this.momentCommentMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(MomentCommentQuery param) {
		StringTools.checkParam(param);
		return this.momentCommentMapper.deleteByParam(param);
	}

	/**
	 * 根据CommentId获取对象
	 */
	@Override
	public MomentComment getMomentCommentByCommentId(Integer commentId) {
		return this.momentCommentMapper.selectByCommentId(commentId);
	}

	/**
	 * 根据CommentId修改
	 */
	@Override
	public Integer updateMomentCommentByCommentId(MomentComment bean, Integer commentId) {
		return this.momentCommentMapper.updateByCommentId(bean, commentId);
	}

	/**
	 * 根据CommentId删除
	 */
	@Override
	public Integer deleteMomentCommentByCommentId(Integer commentId) {
		return this.momentCommentMapper.deleteByCommentId(commentId);
	}
}