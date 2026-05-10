package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.CarouselQuery;
import com.shipin.entity.po.Carousel;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.CarouselMapper;
import com.shipin.service.CarouselService;
import com.shipin.utils.StringTools;


/**
 * 轮播图表 业务接口实现
 */
@Service("carouselService")
public class CarouselServiceImpl implements CarouselService {

	@Resource
	private CarouselMapper<Carousel, CarouselQuery> carouselMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Carousel> findListByParam(CarouselQuery param) {
		return this.carouselMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CarouselQuery param) {
		return this.carouselMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Carousel> findListByPage(CarouselQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Carousel> list = this.findListByParam(param);
		PaginationResultVO<Carousel> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Carousel bean) {
		return this.carouselMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Carousel> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.carouselMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Carousel> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.carouselMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Carousel bean, CarouselQuery param) {
		StringTools.checkParam(param);
		return this.carouselMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CarouselQuery param) {
		StringTools.checkParam(param);
		return this.carouselMapper.deleteByParam(param);
	}

	/**
	 * 根据CarouselId获取对象
	 */
	@Override
	public Carousel getCarouselByCarouselId(Integer carouselId) {
		return this.carouselMapper.selectByCarouselId(carouselId);
	}

	/**
	 * 根据CarouselId修改
	 */
	@Override
	public Integer updateCarouselByCarouselId(Carousel bean, Integer carouselId) {
		return this.carouselMapper.updateByCarouselId(bean, carouselId);
	}

	/**
	 * 根据CarouselId删除
	 */
	@Override
	public Integer deleteCarouselByCarouselId(Integer carouselId) {
		return this.carouselMapper.deleteByCarouselId(carouselId);
	}
}