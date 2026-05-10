package com.shipin.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.shipin.entity.enums.ResponseCodeEnum;
import com.shipin.entity.po.Fans;
import com.shipin.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.shipin.entity.vo.Result;
import com.shipin.utils.CaptchaUtil;
import com.shipin.utils.JwtUtil;
import com.shipin.utils.RedisUtil;
import com.shipin.mappers.FansMapper;
import com.shipin.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.server.Session;
// import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.UserQuery;
import com.shipin.entity.po.User;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.UserMapper;
import com.shipin.service.UserService;
import com.shipin.utils.StringTools;
import org.springframework.util.StringUtils;


/**
 *  业务接口实现
 */
@Slf4j
@Service("userService")
public class UserServiceImpl implements UserService {

	@Value("${app.storage.root:D:/shipin}")
	private String storageRoot;

	@Resource
	private UserMapper<User, UserQuery> userMapper;

	@Resource
	private CaptchaUtil captchaUtil;

	@Resource
	private PasswordUtil passwordUtil;
	
	@Resource
	private JwtUtil jwtUtil;

	@Resource
	private FansMapper fansMapper;

	@Resource
	private RedisUtil redisUtil;


	// 验证码过期时间（分钟）
	private static final long CAPTCHA_EXPIRE_MINUTES = 5;
	// 验证码Redis前缀
	private static final String CAPTCHA_PREFIX = "captcha:";




	@Override
	public Map<String, String> generateCaptcha() {
		// 生成验证码
		String code = captchaUtil.generateCode();
		// 生成验证码图片
		String image = captchaUtil.generateImage(code);
		// 生成验证码key
		String captchaKey = CAPTCHA_PREFIX + RandomStringUtils.randomAlphanumeric(16);
		// 存储验证码到Redis
		redisUtil.set(captchaKey, code, CAPTCHA_EXPIRE_MINUTES * 60);

		Map<String, String> result = new HashMap<>();
		result.put("checkCode", image);
		result.put("checkCodeKey", captchaKey);
		return result;
	}

	@Override
	@Transactional
	public Result<?> login(String email, String password, String checkCodeKey, String checkCode) {
		//检查验证码是否正确
		if(!validateCaptcha(checkCodeKey, checkCode)) {
			return Result.error(401, "验证码错误");
		}
		//检查参数是否正确
		if(!StringUtils.hasText(email)||!StringUtils.hasText(password)
				||!StringUtils.hasText(checkCodeKey)||!StringUtils.hasText(checkCode)) {
			return Result.error(400, "参数不能为空");
		}
		//根据用户名查询用户
		User user = userMapper.selectByEmail(email);
		if(user==null) {
			return Result.error(404, "用户不存在");
		}
		//检查密码是否正确
		if(!passwordUtil.verifyPassword(password, user.getPassword())) {
			return Result.error(405, "密码错误");
		}
		//检查用户是否被禁用
		if(user.getState()!=null&&user.getState()==2) {
			return Result.error(403, "用户被禁用");
		}
		Date now=new Date();
		Date date = DateUtil.formatDateToDay(now);
		Date lastLoginTime = user.getLastLoginTime();
		if(lastLoginTime==null||!DateUtil.isSameDay(lastLoginTime, date)) {
			int i = user.getMyCoin() != null ? user.getMyCoin() : 0;
			user.setLastLoginTime(date);
			user.setMyCoin(i+1);
			userMapper.updateByUserId(user, user.getUserId());
		}
		Map<String, Object> userinfo = new HashMap<>();
		userinfo.put("userId", user.getUserId());
		userinfo.put("email", user.getEmail());
		userinfo.put("nickName", user.getNickName());
		// 返回完整的头像URL
		String avatar = user.getAvatar();
		String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
		userinfo.put("avatar", fullAvatarUrl);
		userinfo.put("createTime", user.getCreateTime());
		userinfo.put("myCoin", user.getMyCoin());
		
		// 生成JWT令牌
		String token=jwtUtil.generateToken(user.getUserId(), user.getEmail());
		userinfo.put("token", token);

		// 将用户信息存储到Redis中，key为userId，value为用户信息，过期时间与token一致
		String redisKey = "user:login:" + user.getUserId();
		redisUtil.set(redisKey, userinfo, jwtUtil.getExpireTime() / 1000);

		return Result.success(userinfo);
	}

	@Override
	@Transactional
	public Result<?> register(String email, String nickName, String registerPassword, String reRegisterPassword, String checkCode, String checkCodeKey) {
		if(!StringUtils.hasText(email)||!StringUtils.hasText(nickName)||!StringUtils.hasText(registerPassword)||!StringUtils.hasText(reRegisterPassword)
				||!StringUtils.hasText(checkCode)||!StringUtils.hasText(checkCodeKey))
				{
					return Result.error(400, "参数不能为空");
		}
		if(!validateCaptcha(checkCodeKey, checkCode)) {
			return Result.error(401, "验证码错误");
		}
		//检查两次密码是否一致
		if(!registerPassword.equals(reRegisterPassword)) {
			return Result.error(403, "两次输入的密码不一致");
		}
		//检查用户是否存在
		User user = userMapper.selectByEmail(email);
		if(user!=null) {
			return Result.error(402, "邮箱已存在");
		}
		User user1 = new User();
		user1.setEmail(email);
		user1.setNickName(nickName);
		user1.setPassword(passwordUtil.encryptPassword(registerPassword));
		user1.setCreateTime(new Date());
		user1.setMyCoin(10);
		user1.setState(1);
		try {
			userMapper.insert(user1);
			return Result.success("注册成功");
		}catch (Exception e) {
			log.error("注册失败",e);
			return Result.error("注册失败");
		}
	}

	@Override
	public boolean validateCaptcha(String captchaKey, String captchaValue) {
		if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaValue)) {
			return false;
		}
		// 从Redis中获取验证码（captchaKey已经包含了CAPTCHA_PREFIX前缀）
		String storedCaptcha = (String) redisUtil.get(captchaKey);
		// 如果验证码不存在，返回false
		if (storedCaptcha == null) {
			return false;
		}
		// 验证码不区分大小写
		boolean isValid = storedCaptcha.equalsIgnoreCase(captchaValue);
		// 验证成功后删除验证码，防止重复使用
		if (isValid) {
			redisUtil.del(captchaKey);
		}
		return isValid;
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<User> findListByParam(UserQuery param) {
		return this.userMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserQuery param) {
		return this.userMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<User> findListByPage(UserQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<User> list = this.findListByParam(param);
		PaginationResultVO<User> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(User bean) {
		return this.userMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<User> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<User> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(User bean, UserQuery param) {
		StringTools.checkParam(param);
		return this.userMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserQuery param) {
		StringTools.checkParam(param);
		return this.userMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public User getUserByUserId(Integer userId) {
		return this.userMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	@Transactional
	public Integer updateUserByUserId(User bean, Integer userId) {
		User user = userMapper.selectByUserId(userId);
		if (user == null) {
			throw new RuntimeException("用户不存在");
		}

		// 检查是否修改了昵称
		if (bean.getNickName() != null && !bean.getNickName().equals(user.getNickName())) {
			Integer myCoin = user.getMyCoin() != null ? user.getMyCoin() : 0;
			if (myCoin < 6) {
				throw new RuntimeException("昵称修改失败，硬币不足");
			}
			// 扣除6个硬币
			bean.setMyCoin(myCoin - 6);
		}

		return this.userMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserByUserId(Integer userId) {
		return this.userMapper.deleteByUserId(userId);
	}

	/**
	 * 上传头像
	 */
	@Override
	@Transactional
	public String uploadAvatar(Integer userId, MultipartFile avatar) {
		if (avatar == null || avatar.isEmpty()) {
			return null;
		}

		try {
			// 获取上传文件的扩展名
			String originalFilename = avatar.getOriginalFilename();
			String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

			String newFilename=userId+"_"+System.currentTimeMillis()+"."+extension;

			String FilePath = storageRoot + "/avatar/";
			File uploadDir=new File(FilePath);
			if(!uploadDir.exists()){
				uploadDir.mkdirs();
			}
			File file=new File(FilePath+newFilename);
			avatar.transferTo(file);

			User newUser=new User();
			newUser.setAvatar(newFilename);
			userMapper.updateByUserId(newUser,userId);
			// 返回头像文件名
			return newFilename;
		} catch (IOException e) {
			log.error("上传头像失败: ", e);
			return null;
		}
	}

	/**
     * 获取用户的关注数量
     */
    @Override
    public Integer getFollowingCount(Integer userId) {
        Integer count = fansMapper.getFollowingCount(userId);
        return count != null ? count : 0;
    }

    /**
     * 获取用户的粉丝数量
     */
    @Override
    public Integer getFollowerCount(Integer userId) {
        Integer count = fansMapper.getFollowerCount(userId);
        return count != null ? count : 0;
    }

    /**
     * 获取用户的关注列表
     */
    @Override
    public List<User> getFollowingList(Fans fans) {
		int count = this.getFollowingCount(fans.getUserId());
		int pageSize = fans.getPageSize() == null ? PageSize.SIZE6.getSize() : fans.getPageSize();

		SimplePage page = new SimplePage(fans.getPageNo(), count, pageSize);
		fans.setSimplePage(page);
		List<User> list = this.fansMapper.getFollowingList(fans);
		PaginationResultVO<User> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result.getList();
    }

    /**
     * 获取用户的粉丝列表
     */
    @Override
    public List<User> getFollowerList(Fans fans) {
		int count = this.getFollowerCount(fans.getUserId());
		int pageSize = fans.getPageSize() == null ? PageSize.SIZE6.getSize() : fans.getPageSize();

		SimplePage page = new SimplePage(fans.getPageNo(), count, pageSize);
		fans.setSimplePage(page);
		List<User> list = this.fansMapper.getFollowerList(fans);
		PaginationResultVO<User> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result.getList();
    }

	@Override
	public void insertClicks(Integer userId,Integer userIdFromToken) {
		Fans fans = fansMapper.selectByAttentionId(userId,userIdFromToken);
		if(fans.getClicks()==null){
			fans.setClicks(0);
		}
		fans.setClicks(fans.getClicks() + 1);
		fansMapper.updateClicks(fans);
	}

	@Override
	@Transactional
	public void followUser(Integer userIdFromToken, Integer userId) {
		Date currentTime = new Date();
		fansMapper.followUser(userIdFromToken, userId,currentTime);
		fansMapper.BecomeFans(userId, userIdFromToken,currentTime);
	}

	@Override
	public Boolean selectFollow(Integer userId, Integer userIdFromToken) {
		Fans fans = fansMapper.selectFowwer(userIdFromToken, userId);
		Fans fans1 = fansMapper.selectFans(userId, userIdFromToken);
		if(fans1!=null&&fans!=null){
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public void delFollowUser(Integer userIdFromToken, Integer userId) {
		fansMapper.delFollowUser(userIdFromToken, userId);
		fansMapper.delBecomeFans(userId, userIdFromToken);
	}

	@Override
	public void banUser(Integer userId) {
		User user = userMapper.selectByUserId(userId);
		if (user != null) {
			user.setState(2); // 设置状态为2表示禁用
			userMapper.updateByUserId(user, userId);
		}
	}

	@Override
	public void unbanUser(Integer userId) {
		User user = userMapper.selectByUserId(userId);
		if (user != null) {
			user.setState(1); // 设置状态为1表示正常
			userMapper.updateByUserId(user, userId);
		}
	}
}