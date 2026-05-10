package com.shipin.mappers;

import org.apache.ibatis.annotations.Param;

/**
 *  数据库操作接口
 */
public interface MomentImageMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ImageId更新
	 */
	 Integer updateByImageId(@Param("bean") T t,@Param("imageId") Integer imageId);


	/**
	 * 根据ImageId删除
	 */
	 Integer deleteByImageId(@Param("imageId") Integer imageId);


	/**
	 * 根据ImageId获取对象
	 */
	 T selectByImageId(@Param("imageId") Integer imageId);


}
