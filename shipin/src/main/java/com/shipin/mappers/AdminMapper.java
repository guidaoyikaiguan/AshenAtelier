package com.shipin.mappers;

import com.shipin.entity.po.Admin;
import com.shipin.entity.query.AdminQuery;
import org.apache.ibatis.annotations.Param;

/**
 *  数据库操作接口
 */
public interface AdminMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据AdminId更新
	 */
	 Integer updateByAdminId(@Param("bean") T t,@Param("adminId") Integer adminId);


	/**
	 * 根据AdminId删除
	 */
	 Integer deleteByAdminId(@Param("adminId") Integer adminId);


	/**
	 * 根据AdminId获取对象
	 */
	 T selectByAdminId(@Param("adminId") Integer adminId);


    Admin selectByAdminName(String adminName);
}
