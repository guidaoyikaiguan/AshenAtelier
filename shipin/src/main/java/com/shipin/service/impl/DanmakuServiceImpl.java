package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.DanmakuQuery;
import com.shipin.entity.po.Danmaku;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.DanmakuMapper;
import com.shipin.service.DanmakuService;
import com.shipin.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("danmakuService")
public class DanmakuServiceImpl implements DanmakuService {

	@Resource
	private DanmakuMapper<Danmaku, DanmakuQuery> danmakuMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Danmaku> findListByParam(DanmakuQuery param) {
		return this.danmakuMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(DanmakuQuery param) {
		return this.danmakuMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Danmaku> findListByPage(DanmakuQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Danmaku> list = this.findListByParam(param);
		PaginationResultVO<Danmaku> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Danmaku bean) {
		return this.danmakuMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Danmaku> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.danmakuMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Danmaku> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.danmakuMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Danmaku bean, DanmakuQuery param) {
		StringTools.checkParam(param);
		return this.danmakuMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(DanmakuQuery param) {
		StringTools.checkParam(param);
		return this.danmakuMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public Danmaku getDanmakuById(Integer id) {
		return this.danmakuMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateDanmakuById(Danmaku bean, Integer id) {
		return this.danmakuMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteDanmakuById(Integer id) {
		return this.danmakuMapper.deleteById(id);
	}

	@Override
	public List<Danmaku> getDanmakuByVideoId(Integer videoId) {
		return danmakuMapper.getDanmakuByVideoId(videoId);
	}
}