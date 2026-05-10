package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.CollectQuery;
import com.shipin.entity.po.Collect;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface CollectService {

	/**
	 * 根据条件查询列表
	 */
	List<Collect> findListByParam(CollectQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CollectQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Collect> findListByPage(CollectQuery param);

	/**
	 * 新增
	 */
	Integer add(Collect bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<Collect> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<Collect> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(Collect bean,CollectQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CollectQuery param);

    List<Collect> selectFavorite(Integer userIdFromToken);

	Integer selectFavoriteNo(Integer userId);

	PaginationResultVO<Collect> selectOrderByTimeAsc(CollectQuery collectQuery);

	PaginationResultVO<Collect> selectOrderByPlayCount(CollectQuery collectQuery);

	PaginationResultVO<Collect> selectOrderByVideoTime(CollectQuery collectQuery);
}