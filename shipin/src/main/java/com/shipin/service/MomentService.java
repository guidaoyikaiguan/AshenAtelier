package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.MomentQuery;
import com.shipin.entity.po.Moment;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface MomentService {

	/**
	 * 根据条件查询列表
	 */
	List<Moment> findListByParam(MomentQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(MomentQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Moment> findListByPage(MomentQuery param);

	/**
	 * 新增
	 */
	Integer add(Moment bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<Moment> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<Moment> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(Moment bean,MomentQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(MomentQuery param);

	/**
	 * 根据MomentId查询对象
	 */
	Moment getMomentByMomentId(Integer momentId);


	/**
	 * 根据MomentId修改
	 */
	Integer updateMomentByMomentId(Moment bean,Integer momentId);


	/**
	 * 根据MomentId删除
	 */
	Integer deleteMomentByMomentId(Integer momentId);

	void likeMoment(Integer userId, int momentId);

    void dellikeMoment(Integer userId, int momentId);
}