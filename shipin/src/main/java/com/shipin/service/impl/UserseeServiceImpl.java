package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.shipin.entity.po.User;
import com.shipin.mappers.UserMapper;
import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.UserseeQuery;
import com.shipin.entity.po.Usersee;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.UserseeMapper;
import com.shipin.service.UserseeService;
import com.shipin.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 *  业务接口实现
 */
@Service("userseeService")
public class UserseeServiceImpl implements UserseeService {

	@Resource
	private UserseeMapper<Usersee, UserseeQuery> userseeMapper;

	@Resource
	private UserMapper userMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Usersee> findListByParam(UserseeQuery param) {
		return this.userseeMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserseeQuery param) {
		return this.userseeMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Usersee> findListByPage(UserseeQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Usersee> list = this.findListByParam(param);
		PaginationResultVO<Usersee> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Usersee bean) {
		return this.userseeMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Usersee> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userseeMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Usersee> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userseeMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Usersee bean, UserseeQuery param) {
		StringTools.checkParam(param);
		return this.userseeMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserseeQuery param) {
		StringTools.checkParam(param);
		return this.userseeMapper.deleteByParam(param);
	}

	@Override
	public Usersee selectByuseridAndByvideoid(Integer userIdFromToken, Integer videoId) {
		Usersee usersee=userseeMapper.selectByuseridAndByvideoid(userIdFromToken,videoId);
		return usersee;
	}

	@Override
	public Boolean selectIfCoins(Integer userIdFromToken, Integer coinsCount) {
		User user = (User) userMapper.selectByUserId(userIdFromToken);
		if(user.getMyCoin()>=coinsCount){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public Boolean deductUserCoins(Integer userId, Integer coinsCount) {
		User user = (User) userMapper.selectByUserId(userId);
		if(user == null || user.getMyCoin() < coinsCount) {
			return false;
		}
		user.setMyCoin(user.getMyCoin() - coinsCount);
		userMapper.updateByUserId(user, userId);
		return true;
	}

	@Override
	@Transactional
	public Boolean refundUserCoins(Integer userId, Integer coinsCount) {
		User user = (User) userMapper.selectByUserId(userId);
		if(user == null) {
			return false;
		}
		user.setMyCoin(user.getMyCoin() + coinsCount);
		userMapper.updateByUserId(user, userId);
		return true;
	}
}