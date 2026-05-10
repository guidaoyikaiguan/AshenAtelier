package com.shipin.controller.admincontroller;

import java.util.Date;
import java.util.List;

import com.shipin.annotation.AdminRequired;
import com.shipin.controller.ABaseController;
import com.shipin.entity.query.CategoryQuery;
import com.shipin.entity.po.Category;
import com.shipin.entity.vo.Result;
import com.shipin.service.CategoryService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 视频分类表 Controller
 */
@RestController("categoryController")
@RequestMapping("/api/category")
public class CategoryController extends ABaseController {
	@Resource
	private CategoryService categoryService;

	@AdminRequired
	@RequestMapping("/admin/add")
	public Result insertCategory(@RequestBody Category category) {
		category.setCreateTime(new Date());
		category.setUpdateTime(new Date());
		categoryService.add(category);
		return Result.success("新增类型成功");
	}

	@RequestMapping("/loadCategories")
	public Result<List> loadCategories() {
		List<Category> categoryList=categoryService.findListByParam(new CategoryQuery());
		return Result.success(categoryList);
	}

	@AdminRequired
	@RequestMapping("/admin/list")
	public Result<List> selectCategory(CategoryQuery categoryQuery) {
		List<Category> categoryList=categoryService.findListByParam(categoryQuery);
		return Result.success(categoryList);
	}

	@AdminRequired
	@RequestMapping("/admin/update")
	public Result updateCategory(@RequestBody Category category) {
		category.setUpdateTime(new Date());
		categoryService.updateCategoryByCategoryId(category, category.getCategoryId());
		return Result.success("修改类型成功");
	}

	@AdminRequired
	@RequestMapping("/admin/delete")
	public Result deleteCategory(@RequestParam Integer categoryId) {
		categoryService.deleteCategoryByCategoryId(categoryId);
		return Result.success("删除类型成功");
	}

	@AdminRequired
	@RequestMapping("/admin/detail")
	public Result<Category> getCategoryById(@RequestParam Integer categoryId) {
		Category category = categoryService.getCategoryByCategoryId(categoryId);
		return Result.success(category);
	}
}