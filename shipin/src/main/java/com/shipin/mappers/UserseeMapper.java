package com.shipin.mappers;

import com.shipin.entity.po.Usersee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface UserseeMapper<T,P> extends BaseMapper<T,P> {

    T selectByuseridAndByvideoid(@Param("userId")Integer userIdFromToken, @Param("videoId")Integer videoId);

    List<Usersee> selectAll();
}
