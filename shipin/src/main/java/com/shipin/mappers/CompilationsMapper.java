package com.shipin.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface CompilationsMapper<T,P> extends BaseMapper<T,P> {

    Integer selectCompilationsNo(@Param("userId")Integer userIdFromToken);

    List<T> selectCompilationsByPage(@Param("query")P compilationsQuery);
}
