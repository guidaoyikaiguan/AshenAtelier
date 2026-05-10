package com.shipin.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频分类表 数据库操作接口
 */
public interface CategoryMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据CategoryId更新
	 */
	 Integer updateByCategoryId(@Param("bean") T t,@Param("categoryId") Integer categoryId);


	/**
	 * 根据CategoryId删除
	 */
	 Integer deleteByCategoryId(@Param("categoryId") Integer categoryId);


	/**
	 * 根据CategoryId获取对象
	 */
	 T selectByCategoryId(@Param("categoryId") Integer categoryId);


	/**
	 * 根据CategoryName更新
	 */
	 Integer updateByCategoryName(@Param("bean") T t,@Param("categoryName") String categoryName);


	/**
	 * 根据CategoryName删除
	 */
	 Integer deleteByCategoryName(@Param("categoryName") String categoryName);


	/**
	 * 根据CategoryName获取对象
	 */
	 T selectByCategoryName(@Param("categoryName") String categoryName);


	List<T> selectCategory();

}
