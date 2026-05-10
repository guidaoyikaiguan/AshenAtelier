package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.MomentCommentQuery;
import com.shipin.entity.po.MomentComment;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface MomentCommentService {

	/**
	 * 根据条件查询列表
	 */
	List<MomentComment> findListByParam(MomentCommentQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(MomentCommentQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<MomentComment> findListByPage(MomentCommentQuery param);

	/**
	 * 新增
	 */
	Integer add(MomentComment bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<MomentComment> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<MomentComment> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(MomentComment bean,MomentCommentQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(MomentCommentQuery param);

	/**
	 * 根据CommentId查询对象
	 */
	MomentComment getMomentCommentByCommentId(Integer commentId);


	/**
	 * 根据CommentId修改
	 */
	Integer updateMomentCommentByCommentId(MomentComment bean,Integer commentId);


	/**
	 * 根据CommentId删除
	 */
	Integer deleteMomentCommentByCommentId(Integer commentId);

}