package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.UserseeQuery;
import com.shipin.entity.po.Usersee;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface UserseeService {

	/**
	 * 根据条件查询列表
	 */
	List<Usersee> findListByParam(UserseeQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserseeQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Usersee> findListByPage(UserseeQuery param);

	/**
	 * 新增
	 */
	Integer add(Usersee bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<Usersee> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<Usersee> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(Usersee bean,UserseeQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserseeQuery param);

    Usersee selectByuseridAndByvideoid(Integer userIdFromToken, Integer videoId);

    Boolean selectIfCoins(Integer userIdFromToken, Integer coinsCount);

	Boolean deductUserCoins(Integer userId, Integer coinsCount);
	Boolean refundUserCoins(Integer userId, Integer coinsCount);

}