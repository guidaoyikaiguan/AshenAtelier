package com.shipin.controller.webcontroller;

import com.shipin.annotation.LoginRequired;
import com.shipin.entity.po.Comment;
import com.shipin.entity.po.CommentLike;
import com.shipin.entity.po.Video;
import com.shipin.entity.query.CollectQuery;
import com.shipin.entity.query.CommentLikeQuery;
import com.shipin.entity.query.CommentQuery;
import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.vo.Result;
import com.shipin.service.AiAgentService;
import com.shipin.service.CommentLikeService;
import com.shipin.service.CommentService;
import com.shipin.service.UserService;
import com.shipin.service.VideoService;
import com.shipin.utils.RedisUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController("CommentController")
@RequestMapping("/api/comment")
public class CommentController {
    @Resource
    private CommentService commentService;
    @Resource
    private VideoService videoService;
    @Resource
    private CommentLikeService commentLikeService;
    @Resource
    private UserService userService;
    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AiAgentService aiAgentService;

    private static Integer delcommentLikeCount = 0;
    //在一条视频下评论
    @LoginRequired
    @RequestMapping("/addComment")
    public Result addComment(Integer videoId, String content, Integer userId, Integer parentId) {
        Comment comment = new Comment();
        if (videoId == null) {
            return Result.error("视频ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        // 验证视频是否存在
        List<Video> videos = videoService.findListByParam(new VideoQuery() {{ setVideoId(videoId); }});
        if (videos == null || videos.isEmpty()) {
            return Result.error("视频不存在");
        }
        
        // 验证父评论是否存在（如果有）
        if (parentId != null) {
            List<Comment> parentComments = commentService.findListByParam(new CommentQuery() {{ setCommentId(parentId); }});
            if (parentComments == null || parentComments.isEmpty()) {
                return Result.error("父评论不存在");
            }
            Comment parentComment = parentComments.get(0);
            if (!parentComment.getVideoId().equals(videoId)) {
                return Result.error("父评论不属于该视频");
            }
            // 继承父评论的置顶状态
            comment.setIfTop(parentComment.getIfTop());
        } else {
            // 顶级评论默认不置顶
            comment.setIfTop(0);
        }
        
        comment.setVideoId(videoId);
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setCommentTime(new Date());
        comment.setLikeCount(0);
        
        commentService.add(comment);

        // 异步 AI 内容审核
        aiAgentService.moderateComment(comment);

        //限流控制：每用户每分钟最多评论20次
        String rateLimitKey = "comment:" + userId;
        boolean allowed = redisUtil.isAllowed(rateLimitKey, 20, 60000);
        if (!allowed) {
            return Result.error(429, "操作频率过高，请稍后再试");
        }

        //分布式锁：防止并发评论导致数据不一致
        String lockKey = "lock:comment:" + userId + ":" + videoId;
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisUtil.acquireLock(lockKey, lockValue, 5000);
        if (!locked) {
            return Result.error(503, "系统繁忙，请稍后再试");
        }
        
        try {
            String redisKey = "video:commentCount:" + videoId;
            redisUtil.incr(redisKey, 1);
            redisUtil.expire(redisKey, 3600);
        } finally {
            redisUtil.releaseLock(lockKey, lockValue);
        }

        return Result.success("评论成功");
    }

    //递归查询一个视频下的评论
    private List<Comment> getCommentsByVideoId(Integer videoId, Integer parentId, Integer sortord, Integer currentUserId) {
        CommentQuery commentQuery = new CommentQuery();
        if(sortord == 0){
            commentQuery.setOrderBy("like_count desc");
        }
        if(sortord == 1){
            commentQuery.setOrderBy("comment_time desc");
        }
        commentQuery.setVideoId(videoId);
        commentQuery.setParentId(parentId);
        List<Comment> comments = commentService.findListByParam(commentQuery);

        // 为每个评论设置用户信息和点赞状态
        for (Comment comment : comments) {
            // 查询用户信息
            com.shipin.entity.po.User user = userService.getUserByUserId(comment.getUserId());
            if (user != null) {
                comment.setNickName(user.getNickName());
                // 设置完整的头像URL
                String avatar = user.getAvatar();
                String fullAvatarUrl = avatar != null ? "/avatar/" + avatar : "/default-avatar.png";
                comment.setUserAvatar(fullAvatarUrl);
                comment.setUsername(user.getNickName()); // 使用昵称作为用户名
            }

            // 检查当前用户是否点赞过
            if (currentUserId != null) {
                CommentLikeQuery likeQuery = new CommentLikeQuery();
                likeQuery.setCommentId(comment.getCommentId());
                likeQuery.setUserId(currentUserId);
                List<CommentLike> likes = commentLikeService.findListByParam(likeQuery);
                comment.setLiked(likes != null && !likes.isEmpty());
            }

            // 递归查询每个评论的子评论
            List<Comment> subComments = getCommentsByVideoId(videoId, comment.getCommentId(), sortord, currentUserId);
            if (!subComments.isEmpty()) {
                comment.setSubComments(subComments);
            }
        }

        return comments;
    }

    // 获取视频的评论列表（包括所有回复）
    @RequestMapping("/getVideoComments")
    public Result getVideoComments(Integer videoId, Integer sortord, Integer userId) {
        System.out.println("调用getVideoComments方法，videoId: " + videoId + ", sortord: " + sortord + ", userId: " + userId);
        if (videoId == null) {
            return Result.error("视频ID不能为空");
        }
        // 当 sortord 未传递时，默认按评论时间降序排序
        if (sortord == null) {
            sortord = 1;
        }
        // 调用递归方法获取评论列表，parentId为null表示获取顶级评论
        List<Comment> comments = getCommentsByVideoId(videoId, null, sortord, userId);
        
        // 打印评论数据，查看是否包含用户信息
        System.out.println("获取到的评论数量: " + (comments != null ? comments.size() : 0));
        if (comments != null && comments.size() > 0) {
            Comment firstComment = comments.get(0);
            System.out.println("第一条评论的用户信息: userId=" + firstComment.getUserId() + ", nickName=" + firstComment.getNickName() + ", userAvatar=" + firstComment.getUserAvatar());
        }
        
        return Result.success(comments);
    }

    // 删除评论（包括所有子评论）
    @LoginRequired
    @RequestMapping("/deleteComment")
    public Result deleteComment(Integer commentId, Integer videoId, Integer userId) {
        // 1. 验证必要参数
        if (commentId == null) {
            return Result.error("评论ID不能为空");
        }
        if (videoId == null) {
            return Result.error("视频ID不能为空");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 2. 验证视频是否存在
        List<Video> videos = videoService.findListByParam(new VideoQuery() {{ setVideoId(videoId); }});
        if (videos == null || videos.isEmpty()) {
            return Result.error("视频不存在");
        }
        Video video = videos.get(0);

        // 3. 验证评论是否存在且属于指定视频
        List<Comment> comments = commentService.findListByParam(new CommentQuery() {{ setCommentId(commentId); }});
        if (comments == null || comments.isEmpty()) {
            return Result.error("评论不存在");
        }
        Comment comment = comments.get(0);
        if (!comment.getVideoId().equals(videoId)) {
            return Result.error("评论不属于该视频");
        }

        // 4. 验证用户是否有权限删除（视频作者或评论作者）
        if (!video.getUserId().equals(userId) && !comment.getUserId().equals(userId)) {
            return Result.error("无权限删除该评论");
        }

        // 5. 递归删除所有子评论（包括嵌套的子评论）
        int deletedCount = deleteCommentsRecursively(commentId);

        // 6. 删除评论本身
        CommentQuery deleteQuery = new CommentQuery();
        deleteQuery.setCommentId(commentId);
        commentService.deleteByParam(deleteQuery);

        // 7. 更新 Redis 中的评论数（包括子评论）
        String redisKey = "video:commentCount:" + videoId;
        redisUtil.decr(redisKey, deletedCount + 1);

        return Result.success("评论删除成功");
    }

    // 递归删除评论及其所有子评论，返回删除的子评论数量
    private int deleteCommentsRecursively(Integer parentId) {
        int count = 0;
        // 1. 查询直接子评论
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setParentId(parentId);
        List<Comment> childComments = commentService.findListByParam(commentQuery);

        // 2. 检查 childComments 是否为 null 或空
        if (childComments == null || childComments.isEmpty()) {
            return count;
        }

        // 3. 递归删除每个子评论及其子评论
        for (Comment childComment : childComments) {
            // 递归删除子评论的子评论
            count += deleteCommentsRecursively(childComment.getCommentId());
            // 删除直接子评论
            CommentQuery commentQuery1 = new CommentQuery();
            commentQuery1.setCommentId(childComment.getCommentId());
            commentService.deleteByParam(commentQuery1);
            count++;
        }

        return count;
    }

    //将一条评论置顶
    @LoginRequired
    @RequestMapping("/topComment")
    public Result topComment(Integer commentId, Integer videoId, Integer userId) {
        if (commentId == null) {
            return Result.error("评论ID不能为空");
        }
        if (videoId == null) {
            return Result.error("视频ID不能为空");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        // 验证视频是否存在
        List<Video> videos = videoService.findListByParam(new VideoQuery() {{
            setVideoId(videoId);
        }});
        if (videos == null || videos.isEmpty()) {
            return Result.error("视频不存在");
        }
        Integer upUserId = videos.get(0).getUserId();
        
        // 验证用户权限（只有视频作者可以置顶）
        if (!upUserId.equals(userId)) {
            return Result.error("无权限操作");
        }
        
        // 验证评论是否存在
        List<Comment> comments = commentService.findListByParam(new CommentQuery() {{ setCommentId(commentId); }});
        if (comments == null || comments.isEmpty()) {
            return Result.error("评论不存在");
        }
        
        // 递归置顶子评论
        topSubComments(commentId);
        
        // 置顶评论本身
        Comment updateComment = new Comment();
        updateComment.setIfTop(1);
        commentService.updateCommentByCommentId(updateComment, commentId);
        
        return Result.success("置顶成功");
    }
    //将一条评论下的所有子评论置顶
    private void topSubComments(Integer commentId) {
        // 1. 查询直接子评论
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setParentId(commentId);
        List<Comment> childComments = commentService.findListByParam(commentQuery);

        // 2. 检查 childComments 是否为 null 或空
        if (childComments == null || childComments.isEmpty()) {
            return;
        }

        // 3. 递归置顶每个子评论及其子评论
        for (Comment childComment : childComments) {
            // 置顶直接子评论
            Comment updateComment = new Comment();
            updateComment.setIfTop(1);
            CommentQuery updateQuery = new CommentQuery();
            updateQuery.setCommentId(childComment.getCommentId());
            commentService.updateByParam(updateComment, updateQuery);
            // 递归置顶子评论的子评论
            topSubComments(childComment.getCommentId());
        }
    }
    //取消一条评论的置顶
    @LoginRequired
    @RequestMapping("/untopComment")
    public Result untopComment(Integer commentId, Integer videoId, Integer userId) {
        if (commentId == null) {
            return Result.error("评论ID不能为空");
        }
        if (videoId == null) {
            return Result.error("视频ID不能为空");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        // 验证视频是否存在
        List<Video> videos = videoService.findListByParam(new VideoQuery() {{
            setVideoId(videoId);
        }});
        if (videos == null || videos.isEmpty()) {
            return Result.error("视频不存在");
        }
        Integer upUserId = videos.get(0).getUserId();
        
        // 验证用户权限（只有视频作者可以取消置顶）
        if (!upUserId.equals(userId)) {
            return Result.error("无权限操作");
        }
        
        // 递归取消置顶子评论
        untopSubComments(commentId);
        
        // 取消置顶评论本身
        Comment updateComment = new Comment();
        updateComment.setIfTop(0);
        commentService.updateCommentByCommentId(updateComment, commentId);
        
        return Result.success("取消置顶成功");
    }
    private void untopSubComments(Integer commentId) {
        // 1. 查询直接子评论
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setParentId(commentId);
        List<Comment> childComments = commentService.findListByParam(commentQuery);

        // 2. 检查 childComments 是否为 null 或空
        if (childComments == null || childComments.isEmpty()) {
            return;
        }

        // 3. 递归取消置顶每个子评论及其子评论
        for (Comment childComment : childComments) {
            // 取消置顶直接子评论
            Comment updateComment = new Comment();
            updateComment.setIfTop(0);
            CommentQuery updateQuery = new CommentQuery();
            updateQuery.setCommentId(childComment.getCommentId());
            commentService.updateByParam(updateComment, updateQuery);
            // 递归取消置顶子评论的子评论
            untopSubComments(childComment.getCommentId());
        }
    }
    //对评论进行点赞或者取消点赞
    @LoginRequired
    @RequestMapping("/likeComment")
    public Result likeComment(Integer commentId, Integer videoId, Integer userId) {
        if (commentId == null) {
            return Result.error("评论ID不能为空");
        }
        if (videoId == null) {
            return Result.error("视频ID不能为空");
        }
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        // 验证视频是否存在
        List<Video> videos = videoService.findListByParam(new VideoQuery() {{
            setVideoId(videoId);
        }});
        if (videos == null || videos.isEmpty()) {
            return Result.error("视频不存在");
        }
        Video video = videos.get(0);
        
        // 验证评论是否存在
        CommentQuery commentQuery = new CommentQuery();
        commentQuery.setCommentId(commentId);
        List<Comment> comments = commentService.findListByParam(commentQuery);
        if (comments == null || comments.isEmpty()) {
            return Result.error("评论不存在");
        }
        Comment comment = comments.get(0);
        
        // 检查评论是否属于指定视频
        if (!comment.getVideoId().equals(videoId)) {
            return Result.error("评论不属于该视频");
        }
        
        // 先查询是否已经点过赞,点过赞就取消点赞
        CommentLikeQuery commentLikeQuery = new CommentLikeQuery();
        commentLikeQuery.setCommentId(commentId);
        commentLikeQuery.setUserId(userId);
        List<CommentLike> listByParam = commentLikeService.findListByParam(commentLikeQuery);
        
        if (listByParam != null && !listByParam.isEmpty()) {
            // 取消点赞
            CommentLikeQuery commentLikeQuery1 = new CommentLikeQuery();
            commentLikeQuery1.setCommentId(commentId);
            commentLikeQuery1.setUserId(userId);
            commentLikeService.deleteByParam(commentLikeQuery1);


            // 更新 Redis 中的点赞数
            String commentLikeCountKey = "comment:likeCount:" + commentId;
            Integer commentLike = (Integer) redisUtil.get(commentLikeCountKey);
            if (commentLike != null) {
                redisUtil.incr(commentLikeCountKey, -1);
            }
            
            return Result.success("取消点赞成功");
        }
        
        // 限流控制：每用户每分钟最多点赞20次
        String rateLimitKey = "like:" + userId;
        boolean allowed = redisUtil.isAllowed(rateLimitKey, 20, 60000); // 20次/60秒
        if (!allowed) {
            return Result.error(429, "操作频率过高，请稍后再试");
        }
        
        // 分布式锁：防止并发点赞导致数据不一致
        String lockKey = "lock:like:" + userId + ":" + videoId;
        String lockValue = UUID.randomUUID().toString();
        boolean locked = redisUtil.acquireLock(lockKey, lockValue, 5000);
        if (!locked) {
            return Result.error(503, "系统繁忙，请稍后再试");
        }
        
        try {
            // 再次检查是否已经点过赞（防止并发操作）
            List<CommentLike> doubleCheckList = commentLikeService.findListByParam(commentLikeQuery);
            if (doubleCheckList != null && !doubleCheckList.isEmpty()) {
                return Result.success("取消点赞成功");
            }
            
            // 执行点赞操作
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUserId(userId);
            commentLike.setLikeTime(new Date());
            commentLikeService.add(commentLike);

            // 更新 Redis 中的点赞数
            String commentLikeCountKey = "comment:likeCount:" + commentId;
            redisUtil.incr(commentLikeCountKey, 1);
            redisUtil.expire(commentLikeCountKey, 3600);
        } finally {
            // 释放分布式锁
            redisUtil.releaseLock(lockKey, lockValue);
        }

        return Result.success("点赞成功");
    }
}