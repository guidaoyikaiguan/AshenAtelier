package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.MomentImageQuery;
import com.shipin.entity.po.MomentImage;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.MomentImageMapper;
import com.shipin.service.MomentImageService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("momentImageService")
public class MomentImageServiceImpl implements MomentImageService {

	@Resource
	private MomentImageMapper<MomentImage, MomentImageQuery> momentImageMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<MomentImage> findListByParam(MomentImageQuery param) {
		return this.momentImageMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(MomentImageQuery param) {
		return this.momentImageMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<MomentImage> findListByPage(MomentImageQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<MomentImage> list = this.findListByParam(param);
		PaginationResultVO<MomentImage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(MomentImage bean) {
		return this.momentImageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<MomentImage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentImageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<MomentImage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.momentImageMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(MomentImage bean, MomentImageQuery param) {
		StringTools.checkParam(param);
		return this.momentImageMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(MomentImageQuery param) {
		StringTools.checkParam(param);
		return this.momentImageMapper.deleteByParam(param);
	}

	/**
	 * 根据ImageId获取对象
	 */
	@Override
	public MomentImage getMomentImageByImageId(Integer imageId) {
		return this.momentImageMapper.selectByImageId(imageId);
	}

	/**
	 * 根据ImageId修改
	 */
	@Override
	public Integer updateMomentImageByImageId(MomentImage bean, Integer imageId) {
		return this.momentImageMapper.updateByImageId(bean, imageId);
	}

	/**
	 * 根据ImageId删除
	 */
	@Override
	public Integer deleteMomentImageByImageId(Integer imageId) {
		return this.momentImageMapper.deleteByImageId(imageId);
	}
}