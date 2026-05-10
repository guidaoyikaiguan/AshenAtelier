package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.CompilationsQuery;
import com.shipin.entity.po.Compilations;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface CompilationsService {

	/**
	 * 根据条件查询列表
	 */
	List<Compilations> findListByParam(CompilationsQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CompilationsQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Compilations> findListByPage(CompilationsQuery param);

	/**
	 * 新增
	 */
	Integer add(Compilations bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<Compilations> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<Compilations> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(Compilations bean,CompilationsQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CompilationsQuery param);

	Integer selectCompilationsNo(Integer userIdFromToken);

	PaginationResultVO<Compilations> selectCompilationsByPage(CompilationsQuery compilationsQuery);
}