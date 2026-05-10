package com.shipin.controller.webcontroller;

import com.qiniu.util.Auth;
import com.shipin.annotation.LoginRequired;
import com.shipin.entity.po.*;
import com.shipin.entity.query.*;
import com.shipin.entity.vo.Result;
import com.shipin.service.*;
import com.shipin.utils.JwtUtil;
import com.shipin.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
@RequestMapping("/api/moment")
public class MomentController {

    @Autowired
    private MomentService momentService;
    @Resource
    private MomentImageService momentImageService;
    @Resource
    private MomentCommentService momentCommentService;

    @Autowired
    private JwtUtil jwtUtil;

    @Resource
    private RedisUtil redisUtil;
    @Resource
    private UserService userService;
    @Resource
    private MomentCommentLikeService momentCommentLikeService;
    @Resource
    private MomentLikeService momentLikeService;

    @Value("${qiniu.access-key}")
    private String accessKey;
    @Value("${qiniu.secret-key}")
    private String secretKey;
    @Value("${qiniu.bucket-name}")
    private String bucketName;
    @Value("${qiniu.domain}")
    private String domain;

    /**
     * 获取七牛云上传令牌
     */
    @GetMapping("/getQiniuToken")
    public Result getQiniuToken(HttpServletRequest request) {
        try {
            // 验证用户登录状态
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token);

            // 生成上传令牌
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucketName);

            System.out.println("生成的七牛云上传令牌: " + upToken);
            System.out.println("使用的存储桶名称: " + bucketName);

            return Result.success(upToken);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取上传令牌失败");
        }
    }

    /**
     * 发布动态
     */
    @LoginRequired
    @PostMapping("/publishMoment")
    public Result publishMoment(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            // 验证用户登录状态
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer userId = jwtUtil.getUserIdFromToken(token);

            // 解析参数
            String content = (String) params.get("content");
            List<String> imageUrls = (List<String>) params.get("images");

            // 创建动态对象
            Moment moment = new Moment();
            moment.setUserId(userId);
            moment.setContent(content);
            moment.setImageCount(imageUrls != null ? imageUrls.size() : 0);
            moment.setCreateTime(new Date());
            moment.setUpdateTime(new Date());

            // 保存动态
            momentService.add(moment);

            // 保存图片信息
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (int i = 0; i < imageUrls.size(); i++) {
                    MomentImage momentImage = new MomentImage();
                    momentImage.setMomentId(moment.getMomentId());
                    momentImage.setImageUrl(imageUrls.get(i));
                    momentImage.setSortOrder(i);
                    momentImage.setCreateTime(new Date());
                    momentImageService.add(momentImage);
                }
            }

            return Result.success("动态发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "动态发布失败");
        }
    }

    /**
     * 获取一个用户动态列表
     */
    @LoginRequired
    @GetMapping("/getMomentList")
    public Result getMomentList(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                HttpServletRequest request,
                                Integer userId) {
        try {
            // 验证用户登录状态
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            jwtUtil.getUserIdFromToken(token);

            // 获取动态列表
            MomentQuery momentQuery = new MomentQuery();
            momentQuery.setPageNo(page);
            momentQuery.setPageSize(size);
            momentQuery.setUserId(userId);
            List<Moment> momentList = momentService.findListByPage(momentQuery).getList();
            for(Moment moment:momentList) {
                UserQuery userQuery = new UserQuery();
                userQuery.setUserId(moment.getUserId());
                List<User> users = userService.findListByParam(userQuery);
                if (users != null && !users.isEmpty()) {
                    User user1 = users.get(0);
                    moment.setUserName(user1.getNickName());
                    String avatar = user1.getAvatar();
                    String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
                    moment.setAvatar(fullAvatarUrl);
                }
            }
            // 处理私有空间的图片URL（添加访问签名）
            for (Moment moment : momentList) {
                MomentImageQuery momentImageQuery = new MomentImageQuery();
                momentImageQuery.setMomentId(moment.getMomentId());
                List<MomentImage> images = momentImageService.findListByParam(momentImageQuery);
                for (MomentImage image : images) {
                    String imageUrl = image.getImageUrl();
                    // 如果是七牛云私有空间的URL，添加访问签名
                    if (imageUrl.contains(domain)) {
                        String key = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                        String signedUrl = getPrivateUrl(key);
                        image.setImageUrl(signedUrl);
                    }
                }
                moment.setImages(images);
            }

            return Result.success(momentList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取动态列表失败");
        }
    }

    /**
     * 点赞动态
     */
    @LoginRequired
    @PostMapping("/likeMoment")
    public Result likeMoment(@RequestParam int momentId, HttpServletRequest request) {
        try {
            // 验证用户登录状态
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Integer userId = jwtUtil.getUserIdFromToken(token);
            MomentLikeQuery momentLikeQuery = new MomentLikeQuery();
            momentLikeQuery.setMomentId(momentId);
            momentLikeQuery.setUserId(userId);
            List<MomentLike> listByParam = momentLikeService.findListByParam(momentLikeQuery);
            if (listByParam != null && !listByParam.isEmpty()) {
                // 已经点过赞了，取消点赞
                momentService.dellikeMoment(userId, momentId);
                Moment m = momentService.getMomentByMomentId(momentId);
                Map<String, Object> unlikeResult = new HashMap<>();
                unlikeResult.put("action", "unliked");
                unlikeResult.put("likeCount", m.getLikeCount());
                return Result.success(unlikeResult);
            }
            // 点赞动态
            momentService.likeMoment(userId, momentId);
            Moment m = momentService.getMomentByMomentId(momentId);
            Map<String, Object> likeResult = new HashMap<>();
            likeResult.put("action", "liked");
            likeResult.put("likeCount", m.getLikeCount());
            return Result.success(likeResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "点赞失败");
        }
    }

    /**
     * 评论动态
     */
    //在一条动态下评论
    @LoginRequired
    @RequestMapping("/addComment")
    public Result addComment(Integer momentId, String content, Integer userId, Integer parentId) {
        MomentComment comment = new MomentComment();
        if (momentId == null) {
            return Result.error("动态ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 验证动态是否存在
        List<Moment> moments =momentService.findListByParam(new MomentQuery() {{ setMomentId(momentId); }});
        if (moments == null || moments.isEmpty()) {
            return Result.error("动态不存在");
        }

        comment.setMomentId(momentId);
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setCreateTime(new Date());
        comment.setLikeCount(0);
        System.out.println("大沙地阿斯加德哦啊世界打撒老大"+parentId);
        momentCommentService.add(comment);

        //限流控制：每用户每分钟最多评论20次
        String rateLimitKey = "momentcomment:" + userId;
        boolean allowed = redisUtil.isAllowed(rateLimitKey, 20, 60000);
        if (!allowed) {
            return Result.error(429, "操作频率过高，请稍后再试");
        }

        //分布式锁：防止并发评论导致数据不一致
        String lockKey = "lock:comment:" + userId + ":" + momentId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisUtil.acquireLock(lockKey, lockValue, 5000);
        if (!locked) {
            return Result.error(503, "系统繁忙，请稍后再试");
        }

        try {
            String redisKey = "momentId:commentCount:" + momentId;
            redisUtil.incr(redisKey, 1);
            redisUtil.expire(redisKey, 3600);
        } finally {
            redisUtil.releaseLock(lockKey, lockValue);
        }

        return Result.success("评论成功");
    }
    //递归查询一个动态下的评论
    private List<MomentComment> getCommentsByMomentId(Integer momentId, Integer parentId, Integer sortord, Integer currentUserId) {
        MomentCommentQuery momentCommentQuery = new MomentCommentQuery();
        if(sortord == 0){
            momentCommentQuery.setOrderBy("like_count desc");
        }
        if(sortord == 1){
            momentCommentQuery.setOrderBy("create_time desc");
        }
        momentCommentQuery.setMomentId(momentId);
        momentCommentQuery.setParentId(parentId);
        List<MomentComment> momentComments = momentCommentService.findListByParam(momentCommentQuery);

        // 为每个评论设置用户信息和点赞状态
        for (MomentComment momentComment : momentComments) {
            // 查询用户信息
            com.shipin.entity.po.User user = userService.getUserByUserId(momentComment.getUserId());
            if (user != null) {
                momentComment.setNickName(user.getNickName());
                // 设置完整的头像URL
                String avatar = user.getAvatar();
                String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
                momentComment.setUserAvatar(fullAvatarUrl);
                momentComment.setUsername(user.getNickName()); // 使用昵称作为用户名
            }

            // 检查当前用户是否点赞过
            if (currentUserId != null) {
                MomentCommentLikeQuery momentCommentLikeQuery = new MomentCommentLikeQuery();
                momentCommentLikeQuery.setMomentCommentId(momentComment.getCommentId());
                momentCommentLikeQuery.setUserId(currentUserId);
                List<MomentCommentLike> likes = momentCommentLikeService.findListByParam(momentCommentLikeQuery);
                momentComment.setLiked(likes != null && !likes.isEmpty());
            }

            // 递归查询每个评论的子评论
            List<MomentComment> subComments = getCommentsByMomentId(momentId, momentComment.getCommentId(), sortord, currentUserId);
            if (!subComments.isEmpty()) {
                momentComment.setSubComments(subComments);
            }
        }

        return momentComments;
    }

    // 获取动态的评论列表（包括所有回复）
    @LoginRequired
    @RequestMapping("/getMomentComments")
    public Result getMomentComments(Integer momentId, Integer sortord, Integer userId) {
        System.out.println("调用getMomentComments方法，momentId: " + momentId + ", sortord: " + sortord + ", userId: " + userId);
        if (momentId == null) {
            return Result.error("动态ID不能为空");
        }
        // 当 sortord 未传递时，默认按评论时间降序排序
        if (sortord == null) {
            sortord = 1;
        }
        // 调用递归方法获取评论列表，parentId为null表示获取顶级评论
        List<MomentComment> comments = getCommentsByMomentId(momentId, null, sortord, userId);

        // 打印评论数据，查看是否包含用户信息
        System.out.println("获取到的评论数量: " + (comments != null ? comments.size() : 0));
        if (comments != null && comments.size() > 0) {
            MomentComment firstComment = comments.get(0);
            System.out.println("第一条评论的用户信息: userId=" + firstComment.getUserId() + ", nickName=" + firstComment.getNickName() + ", userAvatar=" + firstComment.getUserAvatar());
        }

        return Result.success(comments);
    }
    // 删除评论（包括所有子评论）
    @LoginRequired
    @RequestMapping("/deleteComment")
    public Result deleteComment(Integer commentId, Integer momentId, Integer userId) {
        // 1. 验证必要参数
        if (commentId == null) {
            return Result.error("评论ID不能为空");
        }
        if (momentId == null) {
            return Result.error("动态ID不能为空");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 2. 验证动态是否存在
        List<Moment> moments = momentService.findListByParam(new MomentQuery() {{ setMomentId(momentId); }});
        if (moments == null || moments.isEmpty()) {
            return Result.error("动态不存在");
        }
        Moment moment = moments.get(0);

        // 3. 验证评论是否存在且属于指定视频
        List<MomentComment> comments = momentCommentService.findListByParam(new MomentCommentQuery() {{ setCommentId(commentId); }});
        if (comments == null || comments.isEmpty()) {
            return Result.error("评论不存在");
        }
        MomentComment comment = comments.get(0);
        if (!comment.getMomentId().equals(momentId)) {
            return Result.error("评论不属于该视频");
        }

        // 4. 验证用户是否有权限删除（视频作者或评论作者）
        if (!moment.getUserId().equals(userId) && !comment.getUserId().equals(userId)) {
            return Result.error("无权限删除该评论");
        }

        // 5. 递归删除所有子评论（包括嵌套的子评论）
        int deletedCount = deleteCommentsRecursively(commentId);

        // 6. 删除评论本身
        MomentCommentQuery deleteQuery = new MomentCommentQuery();
        deleteQuery.setCommentId(commentId);
        momentCommentService.deleteByParam(deleteQuery);

        // 7. 更新 Redis 中的评论数（包括子评论）
        String redisKey = "momentId:commentCount:" + momentId;
        redisUtil.decr(redisKey, deletedCount + 1);

        return Result.success("评论删除成功");
    }
    // 递归删除评论及其所有子评论，返回删除的子评论数量
    private int deleteCommentsRecursively(Integer parentId) {
        int count = 0;
        // 1. 查询直接子评论
        MomentCommentQuery commentQuery = new MomentCommentQuery();
        commentQuery.setParentId(parentId);
        List<MomentComment> childComments = momentCommentService.findListByParam(commentQuery);

        // 2. 检查 childComments 是否为 null 或空
        if (childComments == null || childComments.isEmpty()) {
            return count;
        }

        // 3. 递归删除每个子评论及其子评论
        for (MomentComment childComment : childComments) {
            // 递归删除子评论的子评论
            count += deleteCommentsRecursively(childComment.getCommentId());
            // 删除直接子评论
            MomentCommentQuery commentQuery1 = new MomentCommentQuery();
            commentQuery1.setCommentId(childComment.getCommentId());
            momentCommentService.deleteByParam(commentQuery1);
            count++;
        }

        return count;
    }
    //对评论进行点赞或者取消点赞
    @LoginRequired
    @RequestMapping("/likeComment")
    public Result likeComment(Integer commentId, Integer momentId, Integer userId) {
        if (commentId == null) {
            return Result.error("评论ID不能为空");
        }
        if (momentId == null) {
            return Result.error("动态ID不能为空");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 验证视频是否存在
        List<Moment> moments = momentService.findListByParam(new MomentQuery() {{
            setMomentId(momentId);
        }});
        if (moments == null || moments.isEmpty()) {
            return Result.error("动态不存在");
        }
        Moment moment = moments.get(0);

        // 验证评论是否存在
        MomentCommentQuery momentCommentQuery = new MomentCommentQuery();
        momentCommentQuery.setCommentId(commentId);
        List<MomentComment> comments = momentCommentService.findListByParam(momentCommentQuery);
        if (comments == null || comments.isEmpty()) {
            return Result.error("评论不存在");
        }
        MomentComment comment = comments.get(0);

        // 检查评论是否属于指定视频
        if (!comment.getMomentId().equals(momentId)) {
            return Result.error("评论不属于该动态");
        }

        // 先查询是否已经点过赞,点过赞就取消点赞
        MomentCommentLikeQuery commentLikeQuery = new MomentCommentLikeQuery();
        commentLikeQuery.setMomentCommentId(commentId);
        commentLikeQuery.setUserId(userId);
        List<MomentCommentLike> listByParam = momentCommentLikeService.findListByParam(commentLikeQuery);

        if (listByParam != null && !listByParam.isEmpty()) {
            // 取消点赞
            MomentCommentLikeQuery commentLikeQuery1 = new MomentCommentLikeQuery();
            commentLikeQuery1.setMomentCommentId(commentId);
            commentLikeQuery1.setUserId(userId);
            momentCommentLikeService.deleteByParam(commentLikeQuery1);


            // 更新 Redis 中的点赞数
            String commentLikeCountKey = "momentcomment:likeCount:" + commentId;
            Integer commentLike = (Integer) redisUtil.get(commentLikeCountKey);
            if (commentLike != null) {
                redisUtil.incr(commentLikeCountKey, -1);
            }

            return Result.success("取消点赞成功");
        }

        // 限流控制：每用户每分钟最多点赞20次
        String rateLimitKey = "momentcommentlike:" + userId;
        boolean allowed = redisUtil.isAllowed(rateLimitKey, 20, 60000); // 20次/60秒
        if (!allowed) {
            return Result.error(429, "操作频率过高，请稍后再试");
        }

        // 分布式锁：防止并发点赞导致数据不一致
        String lockKey = "lock:like:" + userId + ":" + momentId;
        String lockValue = UUID.randomUUID().toString();
        boolean locked = redisUtil.acquireLock(lockKey, lockValue, 5000);
        if (!locked) {
            return Result.error(503, "系统繁忙，请稍后再试");
        }

        try {
            // 再次检查是否已经点过赞（防止并发操作）
            List<MomentCommentLike> doubleCheckList = momentCommentLikeService.findListByParam(commentLikeQuery);
            if (doubleCheckList != null && !doubleCheckList.isEmpty()) {
                return Result.success("取消点赞成功");
            }

            // 执行点赞操作
            MomentCommentLike commentLike = new MomentCommentLike();
            commentLike.setMomentCommentId(commentId);
            commentLike.setUserId(userId);
            commentLike.setLikeTime(new Date());
            momentCommentLikeService.add(commentLike);

            // 更新 Redis 中的点赞数
            String commentLikeCountKey = "momentcomment:likeCount:" + commentId;
            redisUtil.incr(commentLikeCountKey, 1);
            redisUtil.expire(commentLikeCountKey, 3600);
        } finally {
            // 释放分布式锁
            redisUtil.releaseLock(lockKey, lockValue);
        }

        return Result.success("点赞成功");
    }

    //获取关注用户的动态列表
    @LoginRequired
    @GetMapping("/getFollowedMomentList")
    public Result getFollowedMomentList(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      HttpServletRequest request) {
        //先查询登录用户关注的都有谁
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer userId = jwtUtil.getUserIdFromToken(token);
        Fans fans=new Fans();
        fans.setUserId(userId);
        fans.setIsCreateTime(true);
        List<User> followUser=userService.getFollowingList(fans);
        List<List<Moment>> followedMomentList = new ArrayList<>();

        // 先加入自己的动态
        List<Moment> myMoments = buildMomentList(userId, page, size);
        if (!myMoments.isEmpty()) {
            followedMomentList.add(myMoments);
        }

        for(User user:followUser){
            List<Moment> momentList = buildMomentList(user.getUserId(), page, size);
            if (!momentList.isEmpty()) {
                followedMomentList.add(momentList);
            }
        }
        return Result.success(followedMomentList);
    }

    // 构建带用户信息和签名URL的动态列表
    private List<Moment> buildMomentList(Integer userId, int page, int size) {
        MomentQuery momentQuery = new MomentQuery();
        momentQuery.setPageNo(page);
        momentQuery.setPageSize(size);
        momentQuery.setUserId(userId);
        List<Moment> momentList = momentService.findListByPage(momentQuery).getList();
        for(Moment moment:momentList) {
            UserQuery userQuery = new UserQuery();
            userQuery.setUserId(moment.getUserId());
            List<User> users = userService.findListByParam(userQuery);
            if (users != null && !users.isEmpty()) {
                User user1 = users.get(0);
                moment.setUserName(user1.getNickName());
                String avatar = user1.getAvatar();
                String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
                moment.setAvatar(fullAvatarUrl);
            }
        }
        for (Moment moment : momentList) {
            MomentImageQuery momentImageQuery = new MomentImageQuery();
            momentImageQuery.setMomentId(moment.getMomentId());
            List<MomentImage> images = momentImageService.findListByParam(momentImageQuery);
            for (MomentImage image : images) {
                String imageUrl = image.getImageUrl();
                if (imageUrl.contains(domain)) {
                    String key = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    String signedUrl = getPrivateUrl(key);
                    image.setImageUrl(signedUrl);
                }
            }
            moment.setImages(images);
        }
        return momentList;
    }

    //删除一条动态
    @LoginRequired
    @RequestMapping("/deleteMoment")
    public Result deleteMoment(Integer momentId, Integer userId) {
        MomentQuery momentQuery = new MomentQuery();
        momentQuery.setMomentId(momentId);
        List<Moment> moments=momentService.findListByParam(momentQuery);
        if(moments==null||moments.isEmpty()){
            return Result.error("动态不存在");
        }
        Moment moment = moments.get(0);
        if (!moment.getUserId().equals(userId)) {
            return Result.error("无权限删除该动态");
        }
        // 删除动态
        momentService.deleteByParam(momentQuery);
        // 删除动态相关的图片
        MomentImageQuery momentImageQuery = new MomentImageQuery();
        momentImageQuery.setMomentId(momentId);
        momentImageService.deleteByParam(momentImageQuery);
        // 删除动态相关的评论
        // 递归删除所有子评论（包括嵌套的子评论）
        int deletedCount = deleteMomentCommentsRecursively(null, momentId);
        // 删除动态相关的点赞记录
        MomentLikeQuery momentLikeQuery = new MomentLikeQuery();
        momentLikeQuery.setMomentId(momentId);
        momentLikeService.deleteByParam(momentLikeQuery);
        //删除评论的点赞记录
        MomentCommentLikeQuery commentLikeQuery = new MomentCommentLikeQuery();
        commentLikeQuery.setMomentCommentId(momentId);
        momentCommentLikeService.deleteByParam(commentLikeQuery);
        return Result.success("删除动态成功");
    }
    //递归删除动态中的评论
    // 递归删除评论及其所有子评论，返回删除的子评论数量
    private int deleteMomentCommentsRecursively(Integer parentId,Integer momentId) {
        int count = 0;
        // 1. 查询直接子评论
        MomentCommentQuery commentQuery = new MomentCommentQuery();
        commentQuery.setParentId(parentId);
        commentQuery.setMomentId(momentId);
        List<MomentComment> childComments = momentCommentService.findListByParam(commentQuery);

        // 2. 检查 childComments 是否为 null 或空
        if (childComments == null || childComments.isEmpty()) {
            return count;
        }

        // 3. 递归删除每个子评论及其子评论
        for (MomentComment childComment : childComments) {
            // 递归删除子评论的子评论
            count += deleteMomentCommentsRecursively(childComment.getCommentId(), momentId);
            // 删除直接子评论
            MomentCommentQuery commentQuery1 = new MomentCommentQuery();
            commentQuery1.setCommentId(childComment.getCommentId());
            momentCommentService.deleteByParam(commentQuery1);
            count++;
        }

        return count;
    }

    // 修改动态内容
    @LoginRequired
    @RequestMapping("/updateMoment")
    public Result updateMoment(Integer momentId, String content, Integer userId) {
        if (momentId == null || content == null || content.trim().isEmpty()) {
            return Result.error("参数不能为空");
        }
        MomentQuery momentQuery = new MomentQuery();
        momentQuery.setMomentId(momentId);
        List<Moment> moments = momentService.findListByParam(momentQuery);
        if (moments == null || moments.isEmpty()) {
            return Result.error("动态不存在");
        }
        Moment moment = moments.get(0);
        if (!moment.getUserId().equals(userId)) {
            return Result.error("无权限修改该动态");
        }
        Moment updateBean = new Moment();
        updateBean.setContent(content.trim());
        updateBean.setUpdateTime(new Date());
        momentService.updateMomentByMomentId(updateBean, momentId);
        return Result.success("修改成功");
    }

    /**
     * 获取七牛云私有空间的访问签名URL
     */
    private String getPrivateUrl(String key) {
        Auth auth = Auth.create(accessKey, secretKey);
        String baseUrl = "http://" + domain + "/" + key;
        long expireInSeconds = 3600; // 1小时过期
        return auth.privateDownloadUrl(baseUrl, expireInSeconds);
    }
}