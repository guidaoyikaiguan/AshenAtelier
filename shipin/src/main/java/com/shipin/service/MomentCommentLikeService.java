package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.MomentCommentLikeQuery;
import com.shipin.entity.po.MomentCommentLike;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface MomentCommentLikeService {

	/**
	 * 根据条件查询列表
	 */
	List<MomentCommentLike> findListByParam(MomentCommentLikeQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(MomentCommentLikeQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<MomentCommentLike> findListByPage(MomentCommentLikeQuery param);

	/**
	 * 新增
	 */
	Integer add(MomentCommentLike bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<MomentCommentLike> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<MomentCommentLike> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(MomentCommentLike bean,MomentCommentLikeQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(MomentCommentLikeQuery param);

	/**
	 * 根据UserId查询对象
	 */
	MomentCommentLike getMomentCommentLikeByUserId(Integer userId);


	/**
	 * 根据UserId修改
	 */
	Integer updateMomentCommentLikeByUserId(MomentCommentLike bean,Integer userId);


	/**
	 * 根据UserId删除
	 */
	Integer deleteMomentCommentLikeByUserId(Integer userId);

}