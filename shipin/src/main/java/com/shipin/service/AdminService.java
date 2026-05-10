package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.AdminQuery;
import com.shipin.entity.po.Admin;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface AdminService {

	/**
	 * 根据条件查询列表
	 */
	List<Admin> findListByParam(AdminQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(AdminQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Admin> findListByPage(AdminQuery param);

	/**
	 * 新增
	 */
	Integer add(Admin bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<Admin> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<Admin> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(Admin bean,AdminQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(AdminQuery param);

	/**
	 * 根据AdminId查询对象
	 */
	Admin getAdminByAdminId(Integer adminId);


	/**
	 * 根据AdminId修改
	 */
	Integer updateAdminByAdminId(Admin bean,Integer adminId);


	/**
	 * 根据AdminId删除
	 */
	Integer deleteAdminByAdminId(Integer adminId);

    Admin login(AdminQuery query);
}