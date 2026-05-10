package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.CarouselQuery;
import com.shipin.entity.po.Carousel;
import com.shipin.entity.vo.PaginationResultVO;


/**
 * 轮播图表 业务接口
 */
public interface CarouselService {

	/**
	 * 根据条件查询列表
	 */
	List<Carousel> findListByParam(CarouselQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CarouselQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Carousel> findListByPage(CarouselQuery param);

	/**
	 * 新增
	 */
	Integer add(Carousel bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<Carousel> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<Carousel> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(Carousel bean,CarouselQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CarouselQuery param);

	/**
	 * 根据CarouselId查询对象
	 */
	Carousel getCarouselByCarouselId(Integer carouselId);


	/**
	 * 根据CarouselId修改
	 */
	Integer updateCarouselByCarouselId(Carousel bean,Integer carouselId);


	/**
	 * 根据CarouselId删除
	 */
	Integer deleteCarouselByCarouselId(Integer carouselId);

}