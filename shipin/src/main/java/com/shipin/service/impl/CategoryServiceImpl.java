package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.CategoryQuery;
import com.shipin.entity.po.Category;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.CategoryMapper;
import com.shipin.service.CategoryService;
import com.shipin.utils.StringTools;


/**
 * 视频分类表 业务接口实现
 */
@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {

	@Resource
	private CategoryMapper<Category, CategoryQuery> categoryMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Category> findListByParam(CategoryQuery param) {
		return this.categoryMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CategoryQuery param) {
		return this.categoryMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Category> findListByPage(CategoryQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Category> list = this.findListByParam(param);
		PaginationResultVO<Category> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Category bean) {
		return this.categoryMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Category> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.categoryMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Category> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.categoryMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Category bean, CategoryQuery param) {
		StringTools.checkParam(param);
		return this.categoryMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CategoryQuery param) {
		StringTools.checkParam(param);
		return this.categoryMapper.deleteByParam(param);
	}

	/**
	 * 根据CategoryId获取对象
	 */
	@Override
	public Category getCategoryByCategoryId(Integer categoryId) {
		return this.categoryMapper.selectByCategoryId(categoryId);
	}

	/**
	 * 根据CategoryId修改
	 */
	@Override
	public Integer updateCategoryByCategoryId(Category bean, Integer categoryId) {
		return this.categoryMapper.updateByCategoryId(bean, categoryId);
	}

	/**
	 * 根据CategoryId删除
	 */
	@Override
	public Integer deleteCategoryByCategoryId(Integer categoryId) {
		return this.categoryMapper.deleteByCategoryId(categoryId);
	}

	/**
	 * 根据CategoryName获取对象
	 */
	@Override
	public Category getCategoryByCategoryName(String categoryName) {
		return this.categoryMapper.selectByCategoryName(categoryName);
	}

	/**
	 * 根据CategoryName修改
	 */
	@Override
	public Integer updateCategoryByCategoryName(Category bean, String categoryName) {
		return this.categoryMapper.updateByCategoryName(bean, categoryName);
	}

	/**
	 * 根据CategoryName删除
	 */
	@Override
	public Integer deleteCategoryByCategoryName(String categoryName) {
		return this.categoryMapper.deleteByCategoryName(categoryName);
	}

}