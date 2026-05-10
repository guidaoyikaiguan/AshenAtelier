package com.shipin.mappers;

import com.shipin.entity.po.Moment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface MomentMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据MomentId更新
	 */
	 Integer updateByMomentId(@Param("bean") T t,@Param("momentId") Integer momentId);


	/**
	 * 根据MomentId删除
	 */
	 Integer deleteByMomentId(@Param("momentId") Integer momentId);


	/**
	 * 根据MomentId获取对象
	 */
	 T selectByMomentId(@Param("momentId") Integer momentId);


    List<Moment> selectAll();
}
