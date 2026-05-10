package com.shipin.controller.webcontroller;

import java.io.File;
import java.util.*;

import com.shipin.annotation.LoginRequired;
import com.shipin.controller.ABaseController;
import com.shipin.entity.enums.ResponseCodeEnum;
import com.shipin.entity.po.Comment;
import com.shipin.entity.po.Fans;
import com.shipin.entity.po.Video;
import com.shipin.entity.query.CommentLikeQuery;
import com.shipin.entity.query.CommentQuery;
import com.shipin.entity.query.UserQuery;
import com.shipin.entity.po.User;
import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.vo.Result;
import com.shipin.service.*;
import com.shipin.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *  Controller
 */
@RestController("userController")
@RequestMapping("/api/user")
public class UserController extends ABaseController {

	@Value("${app.storage.root:D:/shipin}")
	private String storageRoot;

	@Resource
	private UserService userService;
	@Resource
	private JwtUtil jwtUtil;
	@Resource
	private VideoService videoService;
	@Resource
	private VideoSearchService videoSearchService;
	@Resource
	private CommentService commentService;
	@Resource
	private CommentLikeService commentLikeService;
	/**
	 * 根据条件分页查询
	 */
	@LoginRequired
	@RequestMapping("/loadDataList")
	public Result loadDataList(UserQuery query){
		return getSuccessResponseVO(userService.findListByPage(query));
	}

	/**
	 * 新增
	 */
	@LoginRequired
	@RequestMapping("/add")
	public Result add(User bean) {
		userService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@LoginRequired
	@RequestMapping("/addBatch")
	public Result addBatch(@RequestBody List<User> listBean) {
		userService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@LoginRequired
	@RequestMapping("/addOrUpdateBatch")
	public Result addOrUpdateBatch(@RequestBody List<User> listBean) {
		userService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId查询对象
	 */
	@LoginRequired
	@RequestMapping("/getUserByUserId")
	public Result getUserByUserId(Integer userId) {
		return getSuccessResponseVO(userService.getUserByUserId(userId));
	}

	/**
	 * 根据UserId修改对象
	 */
	@LoginRequired
	@RequestMapping("/updateUserByUserId")
	public Result updateUserByUserId(User bean, Integer userId) {
		userService.updateUserByUserId(bean,userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId删除
	 */
	@LoginRequired
	@RequestMapping("/deleteUserByUserId")
	public Result deleteUserByUserId(Integer userId) {
		userService.deleteUserByUserId(userId);
		return getSuccessResponseVO(null);
	}
	@LoginRequired
	@RequestMapping("/getUserInfo")
	public Result getUserInfo(Integer userId) {
		// 检查userId是否为空
		if (userId == null) {
			return Result.error(400, "用户ID不能为空");
		}
		
		User user = userService.getUserByUserId(userId);
		// 检查user是否存在
		if (user == null) {
			return Result.error(404, "用户不存在");
		}
		
		// 创建一个包含前端需要字段的map
		Map<String, Object> userInfoMap = new HashMap<>();
		userInfoMap.put("userId", user.getUserId());
		userInfoMap.put("email", user.getEmail());
		userInfoMap.put("nickName", user.getNickName());
		userInfoMap.put("userName", "bili"); // 暂时硬编码用户名
		// 返回完整的头像URL
		String avatar = user.getAvatar();
		String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
		userInfoMap.put("avatar", fullAvatarUrl);
		userInfoMap.put("createTime", user.getCreateTime());
		userInfoMap.put("updateTime", user.getUpdateTime());
		// 添加前端需要的字段
		userInfoMap.put("signature", user.getMySignature() != null ? user.getMySignature() : "");
		// 性别转换：0-保密，1-男，2-女
		String genderStr = "secret";
		if (user.getGender() != null) {
			if (user.getGender() == 1) {
				genderStr = "male";
			} else if (user.getGender() == 2) {
				genderStr = "female";
			}
		}
		userInfoMap.put("gender", genderStr);
		userInfoMap.put("myCoin", user.getMyCoin() != null ? user.getMyCoin() : 0);
		userInfoMap.put("level", 1);
		userInfoMap.put("isVip", false);
		userInfoMap.put("followingCount", userService.getFollowingCount(userId));
		userInfoMap.put("followerCount", userService.getFollowerCount(userId));
		userInfoMap.put("likeCount", 0);
		userInfoMap.put("playCount", 0);
		userInfoMap.put("regTime", user.getCreateTime());
		userInfoMap.put("coverImage", "");
		// 添加公告字段
		userInfoMap.put("my_Announcement", user.getMy_Announcement() != null ? user.getMy_Announcement() : "");
		return Result.success(userInfoMap);
	}

	@LoginRequired
	@RequestMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody User user, HttpSession session) {
		Integer sessionUserId = (Integer) session.getAttribute("userId");
		if(sessionUserId==null){
			return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(),"用户未登录");
		}
		// 使用session中的userId，而不是前端传递的userId
		user.setUserId(sessionUserId);
		try {
			userService.updateUserByUserId(user, sessionUserId);
			// 返回更新后的用户信息
			return getUserInfo(sessionUserId);
		} catch (RuntimeException e) {
			return Result.error(e.getMessage());
		}
    }

    /**
     * 上传头像
     */
    @LoginRequired
    @RequestMapping("/uploadAvatar")
    public Result uploadAvatar(@RequestParam("avatar") MultipartFile avatar, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(),"用户未登录");
        }
        String avatarUrl = userService.uploadAvatar(userId, avatar);
        return Result.success(Collections.singletonMap("avatarUrl", avatarUrl));
    }

    /**
     * 获取用户关注列表
     */
    @LoginRequired
    @RequestMapping(value = "/getFollowingList", method = RequestMethod.POST)
    public Result getFollowingList(@RequestBody Fans fans,@RequestParam Integer type) {
		System.out.println("放哪时间跨度那四里河打死等哈手打hi啦是"+fans.getUserId());
		Integer userId = fans.getUserId();
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
		if(type==1){
			fans.setIsCreateTime(true);
		}
		if(type==2){
			fans.setIsClicks(true);
		}
        // 获取关注列表
        List<User> followingList = userService.getFollowingList(fans);
        
        // 构建返回数据
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (User user : followingList) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getUserId());
            userMap.put("nickName", user.getNickName());
            userMap.put("description", user.getMySignature() != null ? user.getMySignature() : "");
            String avatar = user.getAvatar();
            String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
            userMap.put("avatar", fullAvatarUrl);
            resultList.add(userMap);
        }

        return Result.success(resultList);
    }

    /**
     * 获取用户粉丝列表
     */
    @LoginRequired
    @RequestMapping(value = "/getFollowerList", method = RequestMethod.POST)
    public Result getFollowerList(@RequestBody Fans fans,HttpServletRequest request) {
		Integer userId = fans.getUserId();
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")){
			token = token.substring(7);
		}
		Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        // 获取粉丝列表
        List<User> followerList = userService.getFollowerList(fans);
        // 构建返回数据
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (User user : followerList) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getUserId());
            userMap.put("nickName", user.getNickName());
            userMap.put("description", user.getMySignature() != null ? user.getMySignature() : "");
            String avatar = user.getAvatar();
            String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
            userMap.put("avatar", fullAvatarUrl);
            // 为每个粉丝单独检查关注状态
            Boolean isFollow = userService.selectFollow(user.getUserId(), userIdFromToken);
            userMap.put("isFollow", isFollow);
            resultList.add(userMap);
        }

        return Result.success(resultList);
    }
	//获取关注用户信息
	@LoginRequired
	@RequestMapping("/getAttentionUserInfo")
	public Result getAttentionUserInfo(Integer userId,HttpServletRequest request) {
		// 检查userId是否为空
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")){
			token = token.substring(7);
		}
		Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
		if (userId == null) {
			return Result.error(400, "用户ID不能为空");
		}
		User user = userService.getUserByUserId(userId);
		// 检查user是否存在
		if (user == null) {
			return Result.error(404, "用户不存在");
		}
		userService.insertClicks(userId,userIdFromToken);
		// 创建一个包含前端需要字段的map
		Map<String, Object> userInfoMap = new HashMap<>();
		userInfoMap.put("userId", user.getUserId());
		userInfoMap.put("email", user.getEmail());
		userInfoMap.put("nickName", user.getNickName());
		userInfoMap.put("userName", "bili"); // 暂时硬编码用户名
		// 返回完整的头像URL
		String avatar = user.getAvatar();
		String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
		userInfoMap.put("avatar", fullAvatarUrl);
		userInfoMap.put("createTime", user.getCreateTime());
		userInfoMap.put("updateTime", user.getUpdateTime());
		// 添加前端需要的字段
		userInfoMap.put("signature", user.getMySignature() != null ? user.getMySignature() : "");
		// 性别转换：0-保密，1-男，2-女
		String genderStr = "secret";
		if (user.getGender() != null) {
			if (user.getGender() == 1) {
				genderStr = "male";
			} else if (user.getGender() == 2) {
				genderStr = "female";
			}
		}
		userInfoMap.put("gender", genderStr);
		userInfoMap.put("myCoin", user.getMyCoin() != null ? user.getMyCoin() : 0);
		userInfoMap.put("level", 1);
		userInfoMap.put("isVip", false);
		userInfoMap.put("followingCount", userService.getFollowingCount(userId));
		userInfoMap.put("followerCount", userService.getFollowerCount(userId));
		userInfoMap.put("likeCount", 0);
		userInfoMap.put("playCount", 0);
		userInfoMap.put("regTime", user.getCreateTime());
		userInfoMap.put("coverImage", "");
		// 添加公告字段
		userInfoMap.put("my_Announcement", user.getMy_Announcement() != null ? user.getMy_Announcement() : "");
		return Result.success(userInfoMap);
	}
	//关注用户
	@LoginRequired
	@RequestMapping("/followUser")
	public Result FollowUser(Integer userId,HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")){
			token = token.substring(7);
		}
		Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
		Boolean ifFoller=userService.selectFollow(userId,userIdFromToken);
		if(ifFoller){
			userService.delFollowUser(userIdFromToken,userId);
			return Result.success("取消关注成功");
		}
		if (userIdFromToken == null) {
			return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "用户未登录");
		}
		if (userIdFromToken.equals(userId)) {
			return Result.error(400, "不能关注自己");
		}
		try {
			userService.followUser(userIdFromToken, userId);
			return Result.success("关注成功");
		} catch (RuntimeException e) {
			return Result.error(e.getMessage());
		}
	}
	//查询是否关注用户
	@LoginRequired
	@RequestMapping("/selectIfFollow")
	public Result selectIfFollow(Integer userId,HttpServletRequest request){
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")){
			token = token.substring(7);
		}
		Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
		Boolean ifFollow=userService.selectFollow(userId,userIdFromToken);
		return Result.success(ifFollow);
	}
	//除了查看关注者用户主页以外的查询用户主页
	@LoginRequired
	@RequestMapping("/getEXAttentionUserInfo")
	public Result getEXAttentionUserInfo(Integer userId) {
		// 检查userId是否为空
		if (userId == null) {
			return Result.error(400, "用户ID不能为空");
		}

		User user = userService.getUserByUserId(userId);
		// 检查user是否存在
		if (user == null) {
			return Result.error(404, "用户不存在");
		}
		// 创建一个包含前端需要字段的map
		Map<String, Object> userInfoMap = new HashMap<>();
		userInfoMap.put("userId", user.getUserId());
		userInfoMap.put("email", user.getEmail());
		userInfoMap.put("nickName", user.getNickName());
		userInfoMap.put("userName", "bili"); // 暂时硬编码用户名
		// 返回完整的头像URL
		String avatar = user.getAvatar();
		String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
		userInfoMap.put("avatar", fullAvatarUrl);
		userInfoMap.put("createTime", user.getCreateTime());
		userInfoMap.put("updateTime", user.getUpdateTime());
		// 添加前端需要的字段
		userInfoMap.put("signature", user.getMySignature() != null ? user.getMySignature() : "");
		// 性别转换：0-保密，1-男，2-女
		String genderStr = "secret";
		if (user.getGender() != null) {
			if (user.getGender() == 1) {
				genderStr = "male";
			} else if (user.getGender() == 2) {
				genderStr = "female";
			}
		}
		userInfoMap.put("gender", genderStr);
		userInfoMap.put("myCoin", user.getMyCoin() != null ? user.getMyCoin() : 0);
		userInfoMap.put("level", 1);
		userInfoMap.put("isVip", false);
		userInfoMap.put("followingCount", userService.getFollowingCount(userId));
		userInfoMap.put("followerCount", userService.getFollowerCount(userId));
		userInfoMap.put("likeCount", 0);
		userInfoMap.put("playCount", 0);
		userInfoMap.put("regTime", user.getCreateTime());
		userInfoMap.put("coverImage", "");
		// 添加公告字段
		userInfoMap.put("my_Announcement", user.getMy_Announcement() != null ? user.getMy_Announcement() : "");
		return Result.success(userInfoMap);
	}
	//获取用户投稿的视频
	@LoginRequired
	@RequestMapping("/getUserVideos")
	public Result getUserVideos(Integer userId,Integer pageNo) {
		// 检查userId是否为空
		if (userId == null) {
			return Result.error(400, "用户ID不能为空");
		}
		VideoQuery videoQuery = new VideoQuery();
		videoQuery.setUserId(userId);
		if(pageNo==null||pageNo<=0){
			pageNo=1;
		}
		videoQuery.setPageNo(pageNo);
		videoQuery.setPageSize(18);
		// 查询用户投稿的视频列表

		List<Video> videoList = videoService.selectVideo(videoQuery);
		return Result.success(videoList);
	}
	//获取用户一共投稿了多少视频
	@LoginRequired
	@RequestMapping("/getUserVideoCount")
	public Result getUserVideoCount(Integer userId) {
		// 检查userId是否为空
		if (userId == null) {
			return Result.error(400, "用户ID不能为空");
		}
		VideoQuery videoQuery = new VideoQuery();
		videoQuery.setUserId(userId);
		Integer count = videoService.findCountByParam(videoQuery);
		return Result.success(count);
	}
	//获取用户的总获赞数
	@LoginRequired
	@RequestMapping("/getUserTotalLikes")
	public Result getUserTotalLikes(Integer userId,HttpServletRequest request) {
		if (userId == null) {
			return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "用户未登录");
		}
		Integer totalLikes = videoService.selectUserLikes(userId);
		return Result.success(totalLikes);
	}
	//获取用户的总播放量
	@LoginRequired
	@RequestMapping("/getUserTotalPlays")
	public Result getUserTotalPlays(Integer userId,HttpServletRequest request) {
		if (userId == null) {
			return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "用户未登录");
		}
		Integer totalLikes = videoService.selectUserPlays(userId);
		return Result.success(totalLikes);
	}
	//用户查询自己发布的视频
	@LoginRequired
	@RequestMapping("/getMyVideos")
	public Result getMyVideos(Integer pageNo,HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")){
			token = token.substring(7);
		}
		Integer userId = jwtUtil.getUserIdFromToken(token);
		if (userId == null) {
			return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "用户未登录");
		}
		VideoQuery videoQuery = new VideoQuery();
		videoQuery.setUserId(userId);
		if(pageNo==null||pageNo<=0){
			pageNo=1;
		}
		videoQuery.setPageNo(pageNo);
		videoQuery.setPageSize(18);
		List<Video> videoList = videoService.selectVideo(videoQuery);
		return Result.success(videoList);
	}
	//用户查询自己发布的视频总数
	@LoginRequired
	@RequestMapping("/getMyVideoCount")
	public Result getMyVideoCount(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")){
			token = token.substring(7);
		}
		Integer userId = jwtUtil.getUserIdFromToken(token);
		if (userId == null) {
			return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "用户未登录");
		}
		VideoQuery videoQuery = new VideoQuery();
		videoQuery.setUserId(userId);
		Integer count = videoService.findCountByParam(videoQuery);
		return Result.success(count);
	}
	//用户删除一条自己发布的视频
	@LoginRequired
	@RequestMapping("/deleteMyVideo")
	public Result deleteMyVideo(Integer videoId, HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")){
			token = token.substring(7);
		}
		Integer userId = jwtUtil.getUserIdFromToken(token);
		if (userId == null) {
			return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "用户未登录");
		}
		VideoQuery videoQuery = new VideoQuery();
		videoQuery.setVideoId(videoId);
		List<Video> videoList = videoService.findListByParam(videoQuery);
		if (videoList == null || videoList.isEmpty()) {
			return Result.error(404, "视频不存在");
		}
		Video video = videoList.get(0);
		if (!video.getUserId().equals(userId)) {
			return Result.error(403, "没有权限删除该视频");
		}

		try {
			// 删除视频文件
			if (video.getVideoUrl() != null && !video.getVideoUrl().isEmpty()) {
				String videoPath = storageRoot + video.getVideoUrl();
				File videoFile = new File(videoPath);
				deleteDirectory(videoFile);
			}

			// 删除封面文件
			if (video.getCoverUrl() != null && !video.getCoverUrl().isEmpty()) {
				String relativePath = video.getCoverUrl();
				if (relativePath.startsWith("http")) {
					int idx = relativePath.indexOf("/", 8);
					if (idx > 0) relativePath = relativePath.substring(idx);
				}
				File coverFile = new File(storageRoot + relativePath);
				deleteDirectory(coverFile);
			}

			// 从数据库中删除视频
			videoService.deleteVideoByVideoId(videoId);

			//删除对应视频的评论
			deleteCommentsRecursively(null,videoId);
			//删除视频评论中的点赞记录
			CommentQuery commentQuery = new CommentQuery();
			commentQuery.setVideoId(videoId);
			List<Comment> commentList = commentService.findListByParam(commentQuery);
			if (commentList != null && !commentList.isEmpty()) {
				for (Comment comment : commentList) {
					CommentLikeQuery commentLikeQuery = new CommentLikeQuery();
					commentLikeQuery.setCommentId(comment.getCommentId());
					commentLikeService.deleteByParam(commentLikeQuery);
				}
			}
			// 从Elasticsearch中删除视频索引
			try {
				videoSearchService.deleteVideo(videoId);
				System.out.println("视频从ES删除成功: " + videoId);
			} catch (Exception e) {
				System.err.println("视频从ES删除失败: " + e.getMessage());
			}

			return Result.success("视频删除成功");
		} catch (RuntimeException e) {
			return Result.error(e.getMessage());
		}
	}
	//用户对自己发布的一个视频进行修改
	@RequestMapping("/updateMyVideo")
	public Result updateMyVideo(@RequestParam Integer videoId, @RequestBody Video video, HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")){
			token = token.substring(7);
		}
		Integer userId = jwtUtil.getUserIdFromToken(token);
		if (userId == null) {
			return Result.error(ResponseCodeEnum.NOT_LOGIN.getCode(), "用户未登录");
		}
		VideoQuery videoQuery = new VideoQuery();
		videoQuery.setVideoId(videoId);
		List<Video> videoList = videoService.findListByParam(videoQuery);
		if (videoList == null || videoList.isEmpty()) {
			return Result.error(404, "视频不存在");
		}
		Video existingVideo = videoList.get(0);
		if (!existingVideo.getUserId().equals(userId)) {
			return Result.error(403, "没有权限修改该视频");
		}

		// 当视频链接发生变化时，删除原来的视频文件
		if (existingVideo.getVideoUrl() != null && video.getVideoUrl() != null &&
				!existingVideo.getVideoUrl().equals(video.getVideoUrl())) {
			String oldVideoPath = storageRoot + existingVideo.getVideoUrl();
			File oldVideoFile = new File(oldVideoPath);
			deleteDirectory(oldVideoFile);
		}

		// 当封面链接发生变化时，删除原来的视频封面文件
		if (existingVideo.getCoverUrl() != null && video.getCoverUrl() != null &&
				!existingVideo.getCoverUrl().equals(video.getCoverUrl())) {
			String relativePath = existingVideo.getCoverUrl();
			if (relativePath.startsWith("http")) {
				int idx = relativePath.indexOf("/", 8);
				if (idx > 0) relativePath = relativePath.substring(idx);
			}
			File oldCoverFile = new File(storageRoot + relativePath);
			deleteDirectory(oldCoverFile);
		}
		video.setStatus("0");
		video.setUpdateTime(new Date());
		try {
			videoService.updateVideoByVideoId(video, videoId);
			if (video.getVideoUrl() != null && !video.getVideoUrl().isEmpty() && videoId != null) {
				String videoPath = storageRoot + video.getVideoUrl();
				// 调用异步处理方法，不等待处理完成
				videoService.processVideoAsync(videoPath, videoId);
			}

			// 同步更新后的视频到Elasticsearch
			try {
				videoSearchService.syncVideoFromDatabase(videoId);
				System.out.println("视频同步到ES成功: " + videoId);
			} catch (Exception e) {
				System.err.println("视频同步到ES失败: " + e.getMessage());
				// 同步ES失败不影响视频修改的主流程
			}
			videoService.updateVideoByVideoId(video, videoId);
			return Result.success("视频修改成功");
		} catch (RuntimeException e) {
			return Result.error(e.getMessage());
		}
	}
	// 删除目录
	private void deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					deleteDirectory(file);
				}
			}
			directory.delete();
		}
	}
	// 递归删除评论及其所有子评论，返回删除的子评论数量
	private int deleteCommentsRecursively(Integer parentId,Integer videoId) {
		int count = 0;
		// 1. 查询直接子评论
		CommentQuery commentQuery = new CommentQuery();
		commentQuery.setParentId(parentId);
		commentQuery.setVideoId(videoId);
		List<Comment> childComments = commentService.findListByParam(commentQuery);

		// 2. 检查 childComments 是否为 null 或空
		if (childComments == null || childComments.isEmpty()) {
			return count;
		}

		// 3. 递归删除每个子评论及其子评论
		for (Comment childComment : childComments) {
			// 递归删除子评论的子评论
			count += deleteCommentsRecursively(childComment.getCommentId(),videoId);
			// 删除直接子评论
			CommentQuery commentQuery1 = new CommentQuery();
			commentQuery1.setCommentId(childComment.getCommentId());
			commentService.deleteByParam(commentQuery1);
			count++;
		}
		return count;
	}
}
