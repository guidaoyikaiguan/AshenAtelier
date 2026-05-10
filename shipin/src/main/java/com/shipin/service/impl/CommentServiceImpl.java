package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.CommentQuery;
import com.shipin.entity.po.Comment;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.CommentMapper;
import com.shipin.service.CommentService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("commentService")
public class CommentServiceImpl implements CommentService {

	@Resource
	private CommentMapper<Comment, CommentQuery> commentMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Comment> findListByParam(CommentQuery param) {
		return this.commentMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CommentQuery param) {
		return this.commentMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Comment> findListByPage(CommentQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Comment> list = this.findListByParam(param);
		PaginationResultVO<Comment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Comment bean) {
		return this.commentMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Comment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.commentMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Comment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.commentMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Comment bean, CommentQuery param) {
		StringTools.checkParam(param);
		return this.commentMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CommentQuery param) {
		StringTools.checkParam(param);
		return this.commentMapper.deleteByParam(param);
	}

	/**
	 * 根据CommentId获取对象
	 */
	@Override
	public Comment getCommentByCommentId(Integer commentId) {
		return this.commentMapper.selectByCommentId(commentId);
	}

	/**
	 * 根据CommentId修改
	 */
	@Override
	public Integer updateCommentByCommentId(Comment bean, Integer commentId) {
		return this.commentMapper.updateByCommentId(bean, commentId);
	}

	/**
	 * 根据CommentId删除
	 */
	@Override
	public Integer deleteCommentByCommentId(Integer commentId) {
		return this.commentMapper.deleteByCommentId(commentId);
	}
}