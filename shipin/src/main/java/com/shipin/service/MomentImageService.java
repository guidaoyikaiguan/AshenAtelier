package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.MomentImageQuery;
import com.shipin.entity.po.MomentImage;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface MomentImageService {

	/**
	 * 根据条件查询列表
	 */
	List<MomentImage> findListByParam(MomentImageQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(MomentImageQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<MomentImage> findListByPage(MomentImageQuery param);

	/**
	 * 新增
	 */
	Integer add(MomentImage bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<MomentImage> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<MomentImage> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(MomentImage bean,MomentImageQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(MomentImageQuery param);

	/**
	 * 根据ImageId查询对象
	 */
	MomentImage getMomentImageByImageId(Integer imageId);


	/**
	 * 根据ImageId修改
	 */
	Integer updateMomentImageByImageId(MomentImage bean,Integer imageId);


	/**
	 * 根据ImageId删除
	 */
	Integer deleteMomentImageByImageId(Integer imageId);

}