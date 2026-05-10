package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.CommentLikeQuery;
import com.shipin.entity.po.CommentLike;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface CommentLikeService {

	/**
	 * 根据条件查询列表
	 */
	List<CommentLike> findListByParam(CommentLikeQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CommentLikeQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CommentLike> findListByPage(CommentLikeQuery param);

	/**
	 * 新增
	 */
	Integer add(CommentLike bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CommentLike> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CommentLike> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CommentLike bean,CommentLikeQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CommentLikeQuery param);

	/**
	 * 根据UserIdAndCommentId查询对象
	 */
	CommentLike getCommentLikeByUserIdAndCommentId(Integer userId,Integer commentId);


	/**
	 * 根据UserIdAndCommentId修改
	 */
	Integer updateCommentLikeByUserIdAndCommentId(CommentLike bean,Integer userId,Integer commentId);


	/**
	 * 根据UserIdAndCommentId删除
	 */
	Integer deleteCommentLikeByUserIdAndCommentId(Integer userId,Integer commentId);

}