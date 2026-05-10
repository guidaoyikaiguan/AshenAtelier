package com.shipin.mappers;

import com.shipin.entity.po.Fans;
import com.shipin.entity.po.User;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface FansMapper<T,P> extends BaseMapper<T,P>{

    Integer getFollowingCount(@Param("userId") int userid);

    Integer getFollowerCount(@Param("userId") int userid);

    List<T> getFollowerList(@Param("query") P p);

    List<T> getFollowingList(@Param("query") P p);

    void updateClicks(@Param("fans")Fans fans);

    Fans selectByAttentionId(Integer attentionId,Integer userIdFromToken);

    void followUser(Integer userIdFromToken, Integer userId, Date createTime);

    void BecomeFans(Integer userId, Integer userIdFromToken,Date createTime);

    Fans selectFowwer(@Param("userIdFromToken") Integer userIdFromToken, @Param("userId") Integer userId);

    Fans selectFans(@Param("userId")Integer userId, @Param("userIdFromToken")Integer userIdFromToken);

    void delFollowUser(@Param("userIdFromToken")Integer userIdFromToken, @Param("userId")Integer userId);

    void delBecomeFans(@Param("userId")Integer userId, @Param("userIdFromToken")Integer userIdFromToken);
}
