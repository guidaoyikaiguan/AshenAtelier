package com.shipin.service;

import java.util.List;

import com.shipin.entity.query.DanmakuQuery;
import com.shipin.entity.po.Danmaku;
import com.shipin.entity.vo.PaginationResultVO;


/**
 *  业务接口
 */
public interface DanmakuService {

	/**
	 * 根据条件查询列表
	 */
	List<Danmaku> findListByParam(DanmakuQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(DanmakuQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<Danmaku> findListByPage(DanmakuQuery param);

	/**
	 * 新增
	 */
	Integer add(Danmaku bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<Danmaku> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<Danmaku> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(Danmaku bean,DanmakuQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(DanmakuQuery param);

	/**
	 * 根据Id查询对象
	 */
	Danmaku getDanmakuById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updateDanmakuById(Danmaku bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deleteDanmakuById(Integer id);

	List<Danmaku> getDanmakuByVideoId(Integer videoId);
}