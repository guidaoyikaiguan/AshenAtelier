package com.shipin.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface VideoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据VideoId更新
	 */
	 Integer updateByVideoId(@Param("bean") T t,@Param("videoId") Integer videoId);


	/**
	 * 根据VideoId删除
	 */
	 Integer deleteByVideoId(@Param("videoId") Integer videoId);


	/**
	 * 根据VideoId获取对象
	 */
	 T selectByVideoId(@Param("videoId") Integer videoId);


    List<T> selectVideo(@Param("query")P query);

    T selectSeeVideo(@Param("query")P query);

    /**
     * 获取所有视频
     */
    List<T> selectAll();

    Integer selectUserLikes(@Param("userId")Integer userIdFromToken);

	Integer selectUserPlays(@Param("userId")Integer userIdFromToken);

	/**
	 * 获取所有已审核通过的视频
	 */
	List<T> selectAllApprovedVideos();

	/**
	 * 根据标签搜索视频
	 */
	List<T> selectByTags(@Param("tag") String tag, @Param("query") P query);
}
