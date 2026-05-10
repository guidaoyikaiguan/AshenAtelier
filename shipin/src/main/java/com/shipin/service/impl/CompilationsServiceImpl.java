package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.CompilationsQuery;
import com.shipin.entity.po.Compilations;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.CompilationsMapper;
import com.shipin.service.CompilationsService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("compilationsService")
public class CompilationsServiceImpl implements CompilationsService {

	@Resource
	private CompilationsMapper<Compilations, CompilationsQuery> compilationsMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Compilations> findListByParam(CompilationsQuery param) {
		return this.compilationsMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CompilationsQuery param) {
		return this.compilationsMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Compilations> findListByPage(CompilationsQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Compilations> list = this.findListByParam(param);
		PaginationResultVO<Compilations> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Compilations bean) {
		return this.compilationsMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Compilations> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.compilationsMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Compilations> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.compilationsMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Compilations bean, CompilationsQuery param) {
		StringTools.checkParam(param);
		return this.compilationsMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CompilationsQuery param) {
		StringTools.checkParam(param);
		return this.compilationsMapper.deleteByParam(param);
	}

	@Override
	public Integer selectCompilationsNo(Integer userIdFromToken) {
		return this.compilationsMapper.selectCompilationsNo(userIdFromToken);
	}

	@Override
	public PaginationResultVO<Compilations> selectCompilationsByPage(CompilationsQuery compilationsQuery) {
		int count = this.findCountByParam(compilationsQuery);
		int pageSize = compilationsQuery.getPageSize() == null ? PageSize.SIZE15.getSize() : compilationsQuery.getPageSize();

		SimplePage page = new SimplePage(compilationsQuery.getPageNo(), count, pageSize);
		compilationsQuery.setSimplePage(page);
		List<Compilations> list = compilationsMapper.selectCompilationsByPage(compilationsQuery);
		PaginationResultVO<Compilations> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

}