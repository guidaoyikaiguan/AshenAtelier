package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.AdminQuery;
import com.shipin.entity.po.Admin;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.AdminMapper;
import com.shipin.service.AdminService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("adminService")
public class AdminServiceImpl implements AdminService {

	@Resource
	private AdminMapper<Admin, AdminQuery> adminMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Admin> findListByParam(AdminQuery param) {
		return this.adminMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(AdminQuery param) {
		return this.adminMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Admin> findListByPage(AdminQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Admin> list = this.findListByParam(param);
		PaginationResultVO<Admin> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Admin bean) {
		return this.adminMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Admin> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.adminMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Admin> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.adminMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Admin bean, AdminQuery param) {
		StringTools.checkParam(param);
		return this.adminMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(AdminQuery param) {
		StringTools.checkParam(param);
		return this.adminMapper.deleteByParam(param);
	}

	/**
	 * 根据AdminId获取对象
	 */
	@Override
	public Admin getAdminByAdminId(Integer adminId) {
		return this.adminMapper.selectByAdminId(adminId);
	}

	/**
	 * 根据AdminId修改
	 */
	@Override
	public Integer updateAdminByAdminId(Admin bean, Integer adminId) {
		return this.adminMapper.updateByAdminId(bean, adminId);
	}

	/**
	 * 根据AdminId删除
	 */
	@Override
	public Integer deleteAdminByAdminId(Integer adminId) {
		return this.adminMapper.deleteByAdminId(adminId);
	}

	@Override
	public Admin login(AdminQuery query) {
		Admin admin = adminMapper.selectByAdminName(query.getAdminName());
		if (admin != null && admin.getPassword().equals(query.getPassword())) {
			return admin;
		}
		return null;
	}
}