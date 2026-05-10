package com.shipin.mappers;

import org.apache.ibatis.annotations.Param;

/**
 *  数据库操作接口
 */
public interface CommentLikeMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据UserIdAndCommentId更新
	 */
	 Integer updateByUserIdAndCommentId(@Param("bean") T t,@Param("userId") Integer userId,@Param("commentId") Integer commentId);


	/**
	 * 根据UserIdAndCommentId删除
	 */
	 Integer deleteByUserIdAndCommentId(@Param("userId") Integer userId,@Param("commentId") Integer commentId);


	/**
	 * 根据UserIdAndCommentId获取对象
	 */
	 T selectByUserIdAndCommentId(@Param("userId") Integer userId,@Param("commentId") Integer commentId);


}
