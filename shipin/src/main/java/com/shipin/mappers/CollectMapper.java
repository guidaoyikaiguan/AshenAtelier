package com.shipin.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface CollectMapper<T,P> extends BaseMapper<T,P> {

    List<T> selectFavorite(@Param("userId")Integer userIdFromToken);

    Integer selectFavoriteNo(@Param("userId")Integer userId);

    List<T> selectOrderByTimeAsc(@Param("query")P collectQuery);

    List<T> selectOrderByPlayCount(@Param("query")P collectQuery);

    List<T> selectOrderByVideoTime(@Param("query")P collectQuery);
}
