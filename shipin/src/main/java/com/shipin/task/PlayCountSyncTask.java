package com.shipin.task;

import com.shipin.entity.po.*;
import com.shipin.entity.query.UserseeQuery;
import com.shipin.entity.query.VideoQuery;
import com.shipin.mappers.*;
import com.shipin.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 播放量同步任务
 */
@Component
public class PlayCountSyncTask {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private VideoMapper<Video, VideoQuery> videoMapper;

    @Autowired
    private UserseeMapper userseeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private MomentCommentMapper momentCommentMapper;
    @Resource
    private MomentMapper momentMapper;
    /**
     * 每5分钟同步一次播放量到数据库
     */
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void syncPlayCountToDb() {
        try {
            // 1. 获取所有视频的ID
            List<Video> videoList = videoMapper.selectAll();
            
            for (Video video : videoList) {
                Integer videoId = video.getVideoId();
                // 2. 从Redis获取播放量
                String redisKey = "video:playCount:" + videoId;
                Object playCountObj = redisUtil.get(redisKey);
                
                if (playCountObj != null) {
                    // 3. 更新到数据库
                    Video video1 = videoMapper.selectByVideoId(videoId);
                    if(video1.getPlayCount()==null){
                        video1.setPlayCount("0");
                    }
                    int i = Integer.parseInt(video1.getPlayCount());
                    int playCount = Integer.parseInt(playCountObj.toString());
                    int newPlayCount = i + playCount;
                    Video updateVideo = new Video();
                    updateVideo.setPlayCount(String.valueOf(newPlayCount));
                    videoMapper.updateByVideoId(updateVideo, videoId);
                    
                    // 4. 同步完成后删除Redis中的播放量，避免重复计算
                    redisUtil.set(redisKey, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void syncLikeCountToDb() {
        try {
            // 1. 获取所有视频的ID
            List<Video> videoList = videoMapper.selectAll();

            for (Video video : videoList) {
                Integer videoId = video.getVideoId();
                // 2. 从Redis获取点赞数
                String redisKey = "video:likeCount:" + videoId;
                Object likeCountObj = redisUtil.get(redisKey);

                if (likeCountObj != null) {
                    // 3. 更新到数据库
                    Video video1 = videoMapper.selectByVideoId(videoId);
                    if(video1.getLikeCount()==null){
                        video1.setLikeCount("0");
                    }
                    int i = Integer.parseInt(video1.getLikeCount());
                    int likeCount = Integer.parseInt(likeCountObj.toString());
                    int newLikeCount = i + likeCount;
                    
                    // 确保点赞数不会变成负数
                    if (newLikeCount < 0) {
                        newLikeCount = 0;
                    }
                    
                    Video updateVideo = new Video();
                    updateVideo.setLikeCount(String.valueOf(newLikeCount));
                    videoMapper.updateByVideoId(updateVideo, videoId);

                    // 4. 同步完成后重置Redis中的点赞数，避免重复计算
                    redisUtil.set(redisKey, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void syncCoinsInsertedToDb() {
        try {
            // 1. 同步视频总投币数到video表
            List<Video> videoList = videoMapper.selectAll();
            for (Video video : videoList) {
                Integer videoId = video.getVideoId();
                String redisKey = "video:coinCount:" + videoId;
                Object coinsInsertedObj = redisUtil.get(redisKey);

                if (coinsInsertedObj != null) {
                    try {
                        Video video1 = videoMapper.selectByVideoId(videoId);
                        Integer userId = video1.getUserId();
                        User user = (User) userMapper.selectByUserId(userId);
                        if (video1.getCoinsInserted() == null) {
                            video1.setCoinsInserted("0");
                        }
                        if(user.getMyCoin()==null){
                            user.setMyCoin(0);
                        }
                        int currentCoins = Integer.parseInt(video1.getCoinsInserted());
                        int coinsInsertCount = Integer.parseInt(coinsInsertedObj.toString());
                        int newCoinsInsertCount = currentCoins + coinsInsertCount;

                        Video updateVideo = new Video();
                        updateVideo.setCoinsInserted(String.valueOf(newCoinsInsertCount));
                        videoMapper.updateByVideoId(updateVideo, videoId);

                        // 同时更新用户的总投币数
                        int newUserCoins = user.getMyCoin() + coinsInsertCount;
                        user.setMyCoin(newUserCoins);
                        userMapper.updateByUserId(user,userId);

                        // 同步完成后重置Redis中的投币数
                        redisUtil.set(redisKey, 0);
                    } catch (NumberFormatException e) {
                        System.err.println("同步视频投币数失败，videoId: " + videoId + ", error: " + e.getMessage());
                    }
                }
            }

            // 2. 同步用户投币数到usersee表
            List<Usersee> userseeList = userseeMapper.selectAll();
            for (Usersee usersee : userseeList) {
                Integer videoId = usersee.getVideoId();
                Integer userId = usersee.getUserId();
                String redisKey = "video:coinCount:" + videoId + ":" + userId;
                Object coinsInsertedObj = redisUtil.get(redisKey);

                if (coinsInsertedObj != null) {
                    try {
                        int currentCoins = usersee.getInsertcoins() != null ? usersee.getInsertcoins() : 0;
                        int coinsInsertCount = Integer.parseInt(coinsInsertedObj.toString());
                        int newCoinsInsertCount = currentCoins + coinsInsertCount;

                        Usersee updateUsersee = new Usersee();
                        updateUsersee.setInsertcoins(newCoinsInsertCount);
                        UserseeQuery queryUsersee = new UserseeQuery();
                        queryUsersee.setUserId(userId);
                        queryUsersee.setVideoId(videoId);
                        userseeMapper.updateByParam(updateUsersee, queryUsersee);

                        // 同步完成后删除Redis中的投币数
                        redisUtil.del(redisKey);
                    } catch (NumberFormatException e) {
                        System.err.println("同步用户投币数失败，userId: " + userId + ", videoId: " + videoId + ", error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void syncFollowCountToDb() {
        try {
            // 1. 获取所有视频的ID
            List<Video> videoList = videoMapper.selectAll();

            for (Video video : videoList) {
                Integer videoId = video.getVideoId();
                // 2. 从Redis获取收藏数
                String redisKey = "video:followCount:" + videoId;
                Object followCountObj = redisUtil.get(redisKey);

                if (followCountObj != null) {
                    // 3. 更新到数据库
                    Video video1 = videoMapper.selectByVideoId(videoId);
                    if(video1.getFollowCount()==null){
                        video1.setFollowCount("0");
                    }
                    int i = Integer.parseInt(video1.getFollowCount());
                    int followCount = Integer.parseInt(followCountObj.toString());
                    int newFollowCount = i + followCount;
                    
                    // 确保收藏数不会变成负数
                    if (newFollowCount < 0) {
                        newFollowCount = 0;
                    }
                    
                    Video updateVideo = new Video();
                    updateVideo.setFollowCount(String.valueOf(newFollowCount));
                    videoMapper.updateByVideoId(updateVideo, videoId);

                    // 4. 同步完成后重置Redis中的收藏数，避免重复计算
                    redisUtil.set(redisKey, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //评论的点赞同步到数据库中
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void syncCommentLikeCountToDb() {
        try {
            // 1. 获取所有评论
            List<Comment> commentList = commentMapper.selectAll();

            for (Comment comment : commentList) {
                Integer commentId = comment.getCommentId();
                // 2. 从Redis获取点赞数
                String commentLikeCountKey = "comment:likeCount:" + commentId;
                Object commentLikeCountObj = redisUtil.get(commentLikeCountKey);

                if (commentLikeCountObj != null) {
                    try {
                        // 3. 获取数据库中的当前点赞数
                        int currentLikeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
                        int redisLikeCount = Integer.parseInt(commentLikeCountObj.toString());
                        int newLikeCount = currentLikeCount + redisLikeCount;

                        // 确保点赞数不会变成负数
                        if (newLikeCount < 0) {
                            newLikeCount = 0;
                        }

                        // 4. 更新到数据库
                        Comment updateComment = new Comment();
                        updateComment.setLikeCount(newLikeCount);
                        commentMapper.updateByCommentId(updateComment, commentId);

                        // 5. 同步完成后重置Redis中的点赞数，避免重复计算
                        redisUtil.set(commentLikeCountKey, 0);
                    } catch (NumberFormatException e) {
                        System.err.println("同步评论点赞数失败，commentId: " + commentId + ", error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //把存的评论数量转到数据库中
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void syncCommentCountToDb() {
        try {
            // 1. 获取所有视频的ID
            List<Video> videoList = videoMapper.selectAll();

            for (Video video : videoList) {
                Integer videoId = video.getVideoId();
                // 2. 从Redis获取评论数增量
                String redisKey = "video:commentCount:" + videoId;
                Object commentCountObj = redisUtil.get(redisKey);

                if (commentCountObj != null) {
                    try {
                        // 3. 获取数据库中的当前评论数
                        Video video1 = videoMapper.selectByVideoId(videoId);
                        if (video1.getCommentCount() == null) {
                            video1.setCommentCount("0");
                        }
                        int currentCommentCount = Integer.parseInt(video1.getCommentCount());
                        int redisCommentCount = Integer.parseInt(commentCountObj.toString());
                        int newCommentCount = currentCommentCount + redisCommentCount;

                        // 确保评论数不会变成负数
                        if (newCommentCount < 0) {
                            newCommentCount = 0;
                        }

                        // 4. 更新到数据库
                        Video updateVideo = new Video();
                        updateVideo.setCommentCount(String.valueOf(newCommentCount));
                        videoMapper.updateByVideoId(updateVideo, videoId);

                        // 5. 同步完成后重置Redis中的评论数，避免重复计算
                        redisUtil.set(redisKey, 0);
                    } catch (NumberFormatException e) {
                        System.err.println("同步视频评论数失败，videoId: " + videoId + ", error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //动态评论的点赞同步到数据库中
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void syncMomentCommentLikeCountToDb() {
        try {
            // 1. 获取所有评论
            List<MomentComment> commentList = momentCommentMapper.selectAll();

            for (MomentComment comment : commentList) {
                Integer commentId = comment.getCommentId();
                // 2. 从Redis获取点赞数
                String commentLikeCountKey = "momentcomment:likeCount:" + commentId;
                Object commentLikeCountObj = redisUtil.get(commentLikeCountKey);

                if (commentLikeCountObj != null) {
                    try {
                        // 3. 获取数据库中的当前点赞数
                        int currentLikeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
                        int redisLikeCount = Integer.parseInt(commentLikeCountObj.toString());
                        int newLikeCount = currentLikeCount + redisLikeCount;

                        // 确保点赞数不会变成负数
                        if (newLikeCount < 0) {
                            newLikeCount = 0;
                        }

                        // 4. 更新到数据库
                        MomentComment updateComment = new MomentComment();
                        updateComment.setLikeCount(newLikeCount);
                        momentCommentMapper.updateByCommentId(updateComment, commentId);

                        // 5. 同步完成后重置Redis中的点赞数，避免重复计算
                        redisUtil.set(commentLikeCountKey, 0);
                    } catch (NumberFormatException e) {
                        System.err.println("同步评论点赞数失败，commentId: " + commentId + ", error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //把存的动态评论数量转到数据库中
    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
    public void syncMomentCommentCountToDb() {
        try {
            // 1. 获取所有视频的ID
            List<Moment> momentList = momentMapper.selectAll();

            for (Moment moment : momentList) {
                Integer momentId = moment.getMomentId();
                // 2. 从Redis获取评论数增量
                String redisKey = "momentId:commentCount:" + momentId;
                Object commentCountObj = redisUtil.get(redisKey);

                if (commentCountObj != null) {
                    try {
                        // 3. 获取数据库中的当前评论数
                        Moment moment1 = (Moment) momentMapper.selectByMomentId(momentId);
                        if (moment1.getCommentCount() == null) {
                            moment1.setCommentCount(0);
                        }
                        int currentCommentCount = moment1.getCommentCount();
                        int redisCommentCount = Integer.parseInt(commentCountObj.toString());
                        int newCommentCount = currentCommentCount + redisCommentCount;

                        // 确保评论数不会变成负数
                        if (newCommentCount < 0) {
                            newCommentCount = 0;
                        }

                        // 4. 更新到数据库
                        Moment updateMoment = new Moment();
                        updateMoment.setCommentCount(newCommentCount);
                        momentMapper.updateByMomentId(updateMoment, momentId);

                        // 5. 同步完成后重置Redis中的评论数，避免重复计算
                        redisUtil.set(redisKey, 0);
                    } catch (NumberFormatException e) {
                        System.err.println("同步动态评论数失败，momentId: " + momentId + ", error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
