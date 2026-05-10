package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.MomentLikeQuery;
import com.shipin.entity.po.MomentLike;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface MomentLikeService {

	/**
	 * 根据条件查询列表
	 */
	List<MomentLike> findListByParam(MomentLikeQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(MomentLikeQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<MomentLike> findListByPage(MomentLikeQuery param);

	/**
	 * 新增
	 */
	Integer add(MomentLike bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<MomentLike> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<MomentLike> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(MomentLike bean,MomentLikeQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(MomentLikeQuery param);

	/**
	 * 根据LikeId查询对象
	 */
	MomentLike getMomentLikeByLikeId(Integer likeId);


	/**
	 * 根据LikeId修改
	 */
	Integer updateMomentLikeByLikeId(MomentLike bean,Integer likeId);


	/**
	 * 根据LikeId删除
	 */
	Integer deleteMomentLikeByLikeId(Integer likeId);


	/**
	 * 根据UserIdAndMomentId查询对象
	 */
	MomentLike getMomentLikeByUserIdAndMomentId(Integer userId,Integer momentId);


	/**
	 * 根据UserIdAndMomentId修改
	 */
	Integer updateMomentLikeByUserIdAndMomentId(MomentLike bean,Integer userId,Integer momentId);


	/**
	 * 根据UserIdAndMomentId删除
	 */
	Integer deleteMomentLikeByUserIdAndMomentId(Integer userId,Integer momentId);

}