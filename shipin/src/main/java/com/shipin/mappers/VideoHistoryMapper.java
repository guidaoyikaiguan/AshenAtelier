package com.shipin.mappers;

import com.shipin.entity.vo.VideoHistoryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频观看历史记录 数据库操作接口
 */
public interface VideoHistoryMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据Id更新
	 */
	 Integer updateById(@Param("bean") T t,@Param("id") Long id);


	/**
	 * 根据Id删除
	 */
	 Integer deleteById(@Param("id") Long id);


	/**
	 * 根据Id获取对象
	 */
	 T selectById(@Param("id") Long id);


	/**
	 * 根据UserIdAndVideoId更新
	 */
	 Integer updateByUserIdAndVideoId(@Param("bean") T t,@Param("userId") Long userId,@Param("videoId") Long videoId);


	/**
	 * 根据UserIdAndVideoId删除
	 */
	 Integer deleteByUserIdAndVideoId(@Param("userId") Long userId,@Param("videoId") Long videoId);


	/**
	 * 根据UserIdAndVideoId获取对象
	 */
	 T selectByUserIdAndVideoId(@Param("userId") Long userId,@Param("videoId") Long videoId);


	List<VideoHistoryVo> getVideoHistory(@Param("query") P videoHistoryQuery);

	List searchVideoHistory(@Param("userId") Integer userId, @Param("keyword") String keyword);
}
