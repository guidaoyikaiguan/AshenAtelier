package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.CollectQuery;
import com.shipin.entity.po.Collect;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.CollectMapper;
import com.shipin.service.CollectService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("collectService")
public class CollectServiceImpl implements CollectService {

	@Resource
	private CollectMapper<Collect, CollectQuery> collectMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Collect> findListByParam(CollectQuery param) {
		return this.collectMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CollectQuery param) {
		return this.collectMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Collect> findListByPage(CollectQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Collect> list = this.findListByParam(param);
		PaginationResultVO<Collect> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Collect bean) {
		return this.collectMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Collect> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.collectMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Collect> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.collectMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Collect bean, CollectQuery param) {
		StringTools.checkParam(param);
		return this.collectMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CollectQuery param) {
		StringTools.checkParam(param);
		return this.collectMapper.deleteByParam(param);
	}

	@Override
	public List<Collect> selectFavorite(Integer userIdFromToken) {
		return this.collectMapper.selectFavorite(userIdFromToken);
	}

	@Override
	public Integer selectFavoriteNo(Integer userId) {
		return this.collectMapper.selectFavoriteNo(userId);
	}

	@Override
	public PaginationResultVO<Collect> selectOrderByTimeAsc(CollectQuery collectQuery) {
		int count = this.findCountByParam(collectQuery);
		int pageSize = collectQuery.getPageSize() == null ? PageSize.SIZE15.getSize() : collectQuery.getPageSize();

		SimplePage page = new SimplePage(collectQuery.getPageNo(), count, pageSize);
		collectQuery.setSimplePage(page);
		List<Collect> list = collectMapper.selectOrderByTimeAsc(collectQuery);
		PaginationResultVO<Collect> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	@Override
	public PaginationResultVO<Collect> selectOrderByPlayCount(CollectQuery collectQuery) {
		int count = this.findCountByParam(collectQuery);
		int pageSize = collectQuery.getPageSize() == null ? PageSize.SIZE15.getSize() : collectQuery.getPageSize();

		SimplePage page = new SimplePage(collectQuery.getPageNo(), count, pageSize);
		collectQuery.setSimplePage(page);
		List<Collect> list = collectMapper.selectOrderByPlayCount(collectQuery);
		PaginationResultVO<Collect> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	@Override
	public PaginationResultVO<Collect> selectOrderByVideoTime(CollectQuery collectQuery) {
		int count = this.findCountByParam(collectQuery);
		int pageSize = collectQuery.getPageSize() == null ? PageSize.SIZE15.getSize() : collectQuery.getPageSize();

		SimplePage page = new SimplePage(collectQuery.getPageNo(), count, pageSize);
		collectQuery.setSimplePage(page);
		List<Collect> list = collectMapper.selectOrderByVideoTime(collectQuery);
		PaginationResultVO<Collect> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

}