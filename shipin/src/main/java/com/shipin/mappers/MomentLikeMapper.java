package com.shipin.mappers;

import org.apache.ibatis.annotations.Param;

/**
 *  数据库操作接口
 */
public interface MomentLikeMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据LikeId更新
	 */
	 Integer updateByLikeId(@Param("bean") T t,@Param("likeId") Integer likeId);


	/**
	 * 根据LikeId删除
	 */
	 Integer deleteByLikeId(@Param("likeId") Integer likeId);


	/**
	 * 根据LikeId获取对象
	 */
	 T selectByLikeId(@Param("likeId") Integer likeId);


	/**
	 * 根据UserIdAndMomentId更新
	 */
	 Integer updateByUserIdAndMomentId(@Param("bean") T t,@Param("userId") Integer userId,@Param("momentId") Integer momentId);


	/**
	 * 根据UserIdAndMomentId删除
	 */
	 Integer deleteByUserIdAndMomentId(@Param("userId") Integer userId,@Param("momentId") Integer momentId);


	/**
	 * 根据UserIdAndMomentId获取对象
	 */
	 T selectByUserIdAndMomentId(@Param("userId") Integer userId,@Param("momentId") Integer momentId);


}
