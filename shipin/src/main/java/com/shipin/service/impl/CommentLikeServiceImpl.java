package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.CommentLikeQuery;
import com.shipin.entity.po.CommentLike;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.CommentLikeMapper;
import com.shipin.service.CommentLikeService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("commentLikeService")
public class CommentLikeServiceImpl implements CommentLikeService {

	@Resource
	private CommentLikeMapper<CommentLike, CommentLikeQuery> commentLikeMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CommentLike> findListByParam(CommentLikeQuery param) {
		return this.commentLikeMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CommentLikeQuery param) {
		return this.commentLikeMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CommentLike> findListByPage(CommentLikeQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CommentLike> list = this.findListByParam(param);
		PaginationResultVO<CommentLike> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CommentLike bean) {
		return this.commentLikeMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CommentLike> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.commentLikeMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CommentLike> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.commentLikeMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CommentLike bean, CommentLikeQuery param) {
		StringTools.checkParam(param);
		return this.commentLikeMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CommentLikeQuery param) {
		StringTools.checkParam(param);
		return this.commentLikeMapper.deleteByParam(param);
	}

	/**
	 * 根据UserIdAndCommentId获取对象
	 */
	@Override
	public CommentLike getCommentLikeByUserIdAndCommentId(Integer userId, Integer commentId) {
		return this.commentLikeMapper.selectByUserIdAndCommentId(userId, commentId);
	}

	/**
	 * 根据UserIdAndCommentId修改
	 */
	@Override
	public Integer updateCommentLikeByUserIdAndCommentId(CommentLike bean, Integer userId, Integer commentId) {
		return this.commentLikeMapper.updateByUserIdAndCommentId(bean, userId, commentId);
	}

	/**
	 * 根据UserIdAndCommentId删除
	 */
	@Override
	public Integer deleteCommentLikeByUserIdAndCommentId(Integer userId, Integer commentId) {
		return this.commentLikeMapper.deleteByUserIdAndCommentId(userId, commentId);
	}
}