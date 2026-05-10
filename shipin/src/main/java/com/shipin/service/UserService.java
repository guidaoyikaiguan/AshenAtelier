package com.shipin.service;

import java.util.List;
import java.util.Map;

import com.shipin.entity.po.Fans;
import com.shipin.entity.query.UserQuery;
import com.shipin.entity.po.User;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.vo.Result;

import javax.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;


/**
 *  业务接口
 */
public interface UserService {

	/**
	 * 根据条件查询列表
	 */
	List<User> findListByParam(UserQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<User> findListByPage(UserQuery param);

	/**
	 * 新增
	 */
	Integer add(User bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<User> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<User> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(User bean,UserQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(UserQuery param);

	/**
	 * 根据UserId查询对象
	 */
	User getUserByUserId(Integer userId);


	/**
	 * 根据UserId修改
	 */
	Integer updateUserByUserId(User bean,Integer userId);


	/**
	 * 根据UserId删除
	 */
	Integer deleteUserByUserId(Integer userId);

	boolean validateCaptcha(String captchaKey, String captchaValue);

	Map<String, String> generateCaptcha();

	Result<?> login(String email, String password, String checkCodeKey, String checkCode);

    Result<?> register(String email, String nickName, String registerPassword, String reRegisterPassword, String checkCode, String checkCodekey);

    /**
     * 上传头像
     */
    String uploadAvatar(Integer userId, MultipartFile avatar);
    
    /**
     * 获取用户的关注数量
     */
    Integer getFollowingCount(Integer userId);
    
    /**
     * 获取用户的粉丝数量
     */
    Integer getFollowerCount(Integer userId);
    
    /**
     * 获取用户的关注列表
     */
    List<User> getFollowingList(Fans fans);
    
    /**
     * 获取用户的粉丝列表
     */
    List<User> getFollowerList(Fans fans);

	void insertClicks(Integer userId,Integer userIdFromToken);

    void followUser(Integer userIdFromToken, Integer userId);

	Boolean selectFollow(Integer userId, Integer userIdFromToken);

    void delFollowUser(Integer userIdFromToken, Integer userId);

    void banUser(Integer userId);

	void unbanUser(Integer userId);
}