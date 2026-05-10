package com.shipin.controller.webcontroller;

import com.shipin.annotation.LoginRequired;
import com.shipin.controller.ABaseController;
import com.shipin.entity.po.*;
import com.shipin.entity.query.CollectQuery;
import com.shipin.entity.query.CompilationsQuery;
import com.shipin.entity.query.UserseeQuery;
import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.vo.Result;
import com.shipin.service.AiAgentService;
import com.shipin.service.CollectService;
import com.shipin.service.CompilationsService;
import com.shipin.service.UserseeService;
import com.shipin.service.VideoService;
import com.shipin.utils.JwtUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.shipin.utils.VideoUtil;
import com.shipin.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;

@RestController("videoController")
@RequestMapping("/api/video")
public class VideoController extends ABaseController {

    @Value("${app.storage.root:D:/shipin}")
    private String storageRoot;

    @Resource
    private VideoService videoService;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private UserseeService userseeService;

    @Resource
    private CollectService collectService;

    @Resource
    private CompilationsService compilationsService;

    @Resource
    private AiAgentService aiAgentService;

    @Resource
    private com.shipin.service.VideoSearchService videoSearchService;

    // 上传视频文件（兼容旧版本）
    @LoginRequired
    @RequestMapping("/uploadVideoFile")
    public Result uploadVideoFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")){
                token = token.substring(7);
            }
            Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
            // 视频存储路径
            String randomAlphanumeric = RandomStringUtils.randomAlphanumeric(10);
            String videoDir = randomAlphanumeric+"+"+userIdFromToken;
            String videoPath = storageRoot + "/video/"+videoDir+"/";
            File dir = new File(videoPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;
            String filePath = videoPath + fileName;

            // 保存文件
            file.transferTo(new File(filePath));

            // 返回视频路径（相对路径）
            String relativePath = "/video/" + videoDir + "/" + fileName;
            return Result.success(relativePath);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件上传失败");
        }
    }

    // 上传视频分片
    @LoginRequired
    @RequestMapping("/uploadVideoChunk")
    public Result uploadVideoChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileHash") String fileHash,
            @RequestParam("fileName") String fileName,
            HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")){
                token = token.substring(7);
            }
            Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
            
            // 分片临时存储路径
            String tempPath = storageRoot + "/temp/" + fileHash + "/";
            File tempDir = new File(tempPath);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            
            // 保存分片
            String chunkFileName = chunkIndex + ".part";
            String chunkFilePath = tempPath + chunkFileName;
            file.transferTo(new File(chunkFilePath));
            
            return Result.success("分片上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "分片上传失败");
        }
    }

    // 合并视频分片
    @LoginRequired
    @RequestMapping("/mergeVideoChunks")
    public Result mergeVideoChunks(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        String fileHash = (String) params.get("fileHash");
        String fileName = (String) params.get("fileName");
        int totalChunks = Integer.parseInt(params.get("totalChunks").toString());
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")){
                token = token.substring(7);
            }
            Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
            
            // 分片临时存储路径
            String tempPath = storageRoot + "/temp/" + fileHash + "/";
            File tempDir = new File(tempPath);
            if (!tempDir.exists()) {
                return Result.error(400, "分片不存在");
            }
            
            // 视频存储路径
            String randomAlphanumeric = RandomStringUtils.randomAlphanumeric(10);
            String videoDir = randomAlphanumeric+"+"+userIdFromToken;
            String videoPath = storageRoot + "/video/"+videoDir+"/";
            File dir = new File(videoPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            String finalFileName = UUID.randomUUID().toString() + suffix;
            String finalFilePath = videoPath + finalFileName;
            
            // 合并分片
            try (FileOutputStream fos = new FileOutputStream(finalFilePath)) {
                for (int i = 0; i < totalChunks; i++) {
                    String chunkFileName = i + ".part";
                    String chunkFilePath = tempPath + chunkFileName;
                    File chunkFile = new File(chunkFilePath);
                    if (!chunkFile.exists()) {
                        return Result.error(400, "分片不完整");
                    }
                    
                    try (FileInputStream fis = new FileInputStream(chunkFile)) {
                        byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
            
            // 清理临时文件
            deleteDirectory(tempDir);
            
            // 返回视频路径（相对路径）
            String relativePath = "/video/" + videoDir + "/" + finalFileName;
            return Result.success(relativePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "合并分片失败");
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

    // 上传视频封面
    @LoginRequired
    @RequestMapping("/uploadVideoCover")
    public Result uploadVideoCover(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 获取当前登录用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")){
                token = token.substring(7);
            }
            Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
            // 封面存储路径
            String randomAlphanumeric = RandomStringUtils.randomAlphanumeric(10);
            String coverDir = randomAlphanumeric+"+"+userIdFromToken;
            String coverPath = storageRoot + "/cover/"+coverDir+"/";
            File dir = new File(coverPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String finalFileName = UUID.randomUUID().toString() + suffix;
            String finalFilePath = coverPath + finalFileName;
            
            // 保存封面图片
            file.transferTo(new File(finalFilePath));
            
            // 返回封面路径（相对路径）
            String relativePath = "/cover/" + coverDir + "/" + finalFileName;
            return Result.success(relativePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "封面上传失败");
        }
    }

    // 上传视频信息
    @LoginRequired
    @RequestMapping("/uploadVideo")
    public Result uploadVideo(@RequestBody Video video, HttpServletRequest request) {
        try {
            // 获取当前登录用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")){
                token = token.substring(7);
            }
            Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
            
            // 1. 限流控制：每用户每分钟最多上传3个视频
            String rateLimitKey = "upload_video:" + userIdFromToken;
            boolean allowed = redisUtil.isAllowed(rateLimitKey, 3, 60000); // 3次/60秒
            if (!allowed) {
                return Result.error(429, "上传频率过高，请稍后再试");
            }
            
            video.setUserId(userIdFromToken);
            
            // 设置默认值
            if (video.getStatus() == null) {
                video.setStatus("0"); // 0：待处理
            }
            if (video.getPlayCount() == null) {
                video.setPlayCount("0");
            }
            if (video.getLikeCount() == null) {
                video.setLikeCount("0");
            }
            if (video.getCommentCount() == null) {
                video.setCommentCount("0");
            }
            video.setCreateTime(new Date());
            video.setUpdateTime(new Date());

            // 保存视频信息到数据库
            videoService.add(video);

            // 获取自动生成的videoId
            Integer videoId = video.getVideoId();

            // 同步到 Elasticsearch 搜索索引
            try {
                videoSearchService.syncVideoFromDatabase(videoId);
            } catch (Exception esEx) {
                System.err.println("ES 同步失败（不影响上传）: " + esEx.getMessage());
            }
            
            // 异步处理视频（生成封面、提取视频信息、转码等）
            if (video.getVideoUrl() != null && !video.getVideoUrl().isEmpty() && videoId != null) {
                String videoPath = storageRoot + video.getVideoUrl();
                // 调用异步处理方法，不等待处理完成
                videoService.processVideoAsync(videoPath, videoId);
            }
            
            // 异步 AI 审核 + 智能标签
            aiAgentService.moderateVideo(video);
            aiAgentService.enrichVideoTags(video);

            // 立即返回成功响应，不等待异步处理完成
            return Result.success(video);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "视频上传失败");
        }
    }

    // 管理员端获取视频列表
    @RequestMapping("/loadAllVideo")
    public Result loadAllVideo(VideoQuery query) {
        try {
            List<Video> videoList= videoService.selectVideo(query);
            return Result.success(videoList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取视频列表失败");
        }
    }

    // 获取我的视频列表
    @LoginRequired
    @RequestMapping("/loadMyVideos")
    public Result loadMyVideos(VideoQuery query, HttpServletRequest request) {
        try {
            // 获取当前登录用户ID
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            query.setUserId(userId);
            
            List<Video> videoList = videoService.findListByParam(query);
            return Result.success(videoList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取视频列表失败");
        }
    }

    // 获取主页推荐视频
    @RequestMapping("/loadRecommendVideo")
    public Result loadRecommendVideo(Integer pageNo, Integer pageSize) {
        try {
            VideoQuery query = new VideoQuery();
            query.setStatus("1"); // 只返回已审核通过的视频
            if (pageNo == null) {
                pageNo = 1;
            }
            if (pageSize == null) {
                pageSize = 10; // 每页10个视频
            }
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            
            PaginationResultVO<Video> result = videoService.findListByPage(query);
            
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("list", result.getList());
            resultData.put("pageNo", result.getPageNo());
            resultData.put("pageSize", result.getPageSize());
            resultData.put("totalCount", result.getTotalCount());
            resultData.put("pageTotal", result.getPageTotal());
            
            return Result.success(resultData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取推荐视频失败");
        }
    }
    //按照分类获取视频
    @RequestMapping("/loadVideoByCategory")
    public Result loadVideoByCategory(Integer categoryId, Integer pageNo, Integer pageSize) {
        try {
            VideoQuery query = new VideoQuery();
            query.setStatus("1"); // 只返回已审核通过的视频
            if (pageNo == null) {
                pageNo = 1;
            }
            if (pageSize == null) {
                pageSize = 10; // 每页10个视频
            }
            query.setPageNo(pageNo);
            query.setPageSize(pageSize);
            query.setCategroyId(categoryId);
            PaginationResultVO<Video> result = videoService.findListByPage(query);
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("list", result.getList());
            resultData.put("pageNo", result.getPageNo());
            resultData.put("pageSize", result.getPageSize());
            resultData.put("totalCount", result.getTotalCount());
            resultData.put("pageTotal", result.getPageTotal());
            return Result.success(resultData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取视频失败");
        }
    }
    // 更新视频状态
    @RequestMapping("/updateVideoStatus")
    public Result updateVideoStatus(@RequestBody Map<String, Object> params) {
        try {
            Integer videoId = params.get("videoId") != null ? ((Number) params.get("videoId")).intValue() : null;
            String status = (String) params.get("status");

            Video video = new Video();
            video.setVideoId(videoId);
            video.setStatus(status);
            video.setUpdateTime(new Date());

            videoService.updateVideoByVideoId(video, video.getVideoId());
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新视频状态失败");
        }
    }
    // 更新视频标签（AI 标签 Agent 回调）
    @RequestMapping("/updateVideoTags")
    public Result updateVideoTags(@RequestBody Map<String, Object> params) {
        try {
            Integer videoId = params.get("videoId") != null ? ((Number) params.get("videoId")).intValue() : null;
            String tags = (String) params.get("tags");

            Video video = new Video();
            video.setVideoId(videoId);
            video.setTags(tags);
            video.setUpdateTime(new Date());

            videoService.updateVideoByVideoId(video, video.getVideoId());
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "更新视频标签失败");
        }
    }
    // 获取视频详情
    @RequestMapping("/loadVideo/{videoId}")
    public Result loadVideo(@PathVariable("videoId") Integer videoId,HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")){
                token = token.substring(7);
            }
            Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
            Usersee usersee = null;
            if (userIdFromToken != null) {
                UserseeQuery userseeQuery = new UserseeQuery();
                userseeQuery.setUserId(userIdFromToken);
                userseeQuery.setVideoId(videoId);
                Integer count = userseeService.findCountByParam(userseeQuery);
                if(count == null || count == 0){
                    Usersee usersee1 = new Usersee();
                    usersee1.setUserId(userIdFromToken);
                    usersee1.setVideoId(videoId);
                    usersee1.setIflike("0"); // 设置默认值为0，表示未点赞
                    userseeService.add(usersee1);
                } else {
                    usersee = userseeService.selectByuseridAndByvideoid(userIdFromToken, videoId);
                }
            }
            VideoQuery query = new VideoQuery();
            query.setVideoId(videoId);
            Video video = videoService.selectSeeVideo(query);
            
            // 将点赞状态添加到返回的数据中
            if (usersee != null) {
                video.setIflike(usersee.getIflike());
                video.setIfcollect(usersee.getIfcollect());
                video.setUserCoinsInserted(usersee.getInsertcoins());
            }
            
            return Result.success(video);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取视频失败");
        }
    }

    // 增加视频播放量
    @RequestMapping("/increasePlayCount")
    public Result increasePlayCount(Integer videoId, HttpServletRequest request) {
        if (videoId == null) {
            return Result.error(400, "视频ID不能为空");
        }


        // 1. 限流控制：每IP每分钟最多增加10个视频的播放量
        String clientIp = request.getRemoteAddr();
        String rateLimitKey = "play_count:" + clientIp;
        boolean allowed = redisUtil.isAllowed(rateLimitKey, 10, 60000); // 10次/60秒
        if (!allowed) {
            return Result.error(429, "操作频率过高，请稍后再试");
        }
        

        // 2. 使用Redis的INCR命令增加播放量（原子操作，天然支持并发）
        String redisKey = "video:playCount:" + videoId;
        redisUtil.incr(redisKey, 1); // 每次加1
        redisUtil.expire(redisKey, 3600);
        // 这里可以先只更新Redis，然后通过定时任务同步到数据库

        return Result.success("播放量增加成功");
    }

    //点赞功能
    @LoginRequired
    @RequestMapping("/increaseLikeCount")
    public Result increaseLikeCount(VideoQuery query,HttpServletRequest request) {
        if (query.getVideoId() == null) {
            return Result.error(400, "视频ID不能为空");
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        if (userIdFromToken == null) {
            return Result.error(401, "用户未登录");
        }

        // 1. 限流控制：每用户每分钟最多点赞20次
        String rateLimitKey = "like:" + userIdFromToken;
        boolean allowed = redisUtil.isAllowed(rateLimitKey, 20, 60000); // 20次/60秒
        if (!allowed) {
            return Result.error(429, "操作频率过高，请稍后再试");
        }

        // 2. 分布式锁：防止并发点赞导致数据不一致
        String lockKey = "lock:like:" + userIdFromToken + ":" + query.getVideoId();
        String lockValue = UUID.randomUUID().toString();
        try {
            // 尝试获取锁，过期时间5秒
            boolean locked = redisUtil.acquireLock(lockKey, lockValue, 5000);
            if (!locked) {
                return Result.error(503, "系统繁忙，请稍后再试");
            }

            Usersee usersee=userseeService.selectByuseridAndByvideoid(userIdFromToken,query.getVideoId());
            boolean isLike = false;
            
            if(usersee == null) {
                // 如果没有记录，创建新记录并点赞
                usersee = new Usersee();
                usersee.setUserId(userIdFromToken);
                usersee.setVideoId(query.getVideoId());
                usersee.setIflike("1");
                userseeService.add(usersee);
                isLike = true;
            } else if ("1".equals(usersee.getIflike())) {
                // 如果已点赞，取消点赞
                usersee.setIflike("0");
                UserseeQuery updateParam = new UserseeQuery();
                updateParam.setUserId(userIdFromToken);
                updateParam.setVideoId(query.getVideoId());
                userseeService.updateByParam(usersee, updateParam);
                isLike = false;
            } else {
                // 如果有记录但未点赞，更新为已点赞
                usersee.setIflike("1");
                UserseeQuery updateParam = new UserseeQuery();
                updateParam.setUserId(userIdFromToken);
                updateParam.setVideoId(query.getVideoId());
                userseeService.updateByParam(usersee, updateParam);
                isLike = true;
            }

            String redisKey = "video:likeCount:" + query.getVideoId();
            if (isLike) {
                redisUtil.incr(redisKey, 1); // 点赞，加1
            } else {
                redisUtil.decr(redisKey, 1); // 取消点赞，减1
            }
            redisUtil.expire(redisKey, 3600);

            return Result.success(isLike ? "点赞成功" : "取消点赞成功");
        } finally {
            // 释放锁
            redisUtil.releaseLock(lockKey, lockValue);
        }
    }
    // 投币功能
    @LoginRequired
    @RequestMapping("/insertCoins")
    public Result insertCoins(Integer coinsCount, HttpServletRequest request, Integer videoId) {
        // 参数校验
        if (videoId == null || coinsCount == null || coinsCount <= 0) {
            return Result.error(400, "参数错误");
        }

        if (coinsCount > 2) {
            return Result.error(400, "单次投币不能超过2个");
        }

        // 获取用户ID
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        if (userIdFromToken == null) {
            return Result.error(401, "用户未登录");
        }

        // 1. 限流控制：每用户每分钟最多投币10次
        String rateLimitKey = "coin:" + userIdFromToken;
        boolean allowed = redisUtil.isAllowed(rateLimitKey, 10, 60000); // 10次/60秒
        if (!allowed) {
            return Result.error(429, "操作频率过高，请稍后再试");
        }

        // 2. 分布式锁：防止并发投币导致数据不一致
        String lockKey = "lock:coin:" + userIdFromToken + ":" + videoId;
        String lockValue = UUID.randomUUID().toString();
        try {
            // 尝试获取锁，过期时间5秒
            boolean locked = redisUtil.acquireLock(lockKey, lockValue, 5000);
            if (!locked) {
                return Result.error(503, "系统繁忙，请稍后再试");
            }

            // 检查余额并扣除硬币
            if (!userseeService.deductUserCoins(userIdFromToken, coinsCount)) {
                return Result.error(400, "余额不足，投币失败");
            }

            // 获取用户对该视频的投币记录
            Usersee userSeeFrom = userseeService.selectByuseridAndByvideoid(userIdFromToken, videoId);

            // 如果没有记录，创建新记录
            if (userSeeFrom == null) {
                userSeeFrom = new Usersee();
                userSeeFrom.setUserId(userIdFromToken);
                userSeeFrom.setVideoId(videoId);
                userSeeFrom.setInsertcoins(0);
                userseeService.add(userSeeFrom);
            }

            // 获取当前投币数（数据库+Redis）
            Integer currentCoins = userSeeFrom.getInsertcoins() != null ? userSeeFrom.getInsertcoins() : 0;
            String redisKey = "video:coinCount:" + videoId + ":" + userIdFromToken;
            Object redisCoinsObj = redisUtil.get(redisKey);
            if (redisCoinsObj != null) {
                currentCoins += Integer.parseInt(redisCoinsObj.toString());
            }

            // 检查投币上限
            if (currentCoins + coinsCount > 2) {
                // 超过上限，退还硬币
                userseeService.refundUserCoins(userIdFromToken, coinsCount);
                return Result.error(400, "投币上限为2，您已达到上限");
            }

            // 更新Redis中的投币数
            redisUtil.incr(redisKey, coinsCount);
            redisUtil.expire(redisKey, 3600);

            // 更新视频总投币数
            String videoCoinKey = "video:coinCount:" + videoId;
            redisUtil.incr(videoCoinKey, coinsCount);
            redisUtil.expire(videoCoinKey, 3600);

            return Result.success("投币成功");
        } finally {
            // 释放分布式锁
            redisUtil.releaseLock(lockKey, lockValue);
        }
    }

    //新增收藏功能
    @LoginRequired
    @RequestMapping("/increaseCollect")
    public Result increaseCollect(Integer videoId,HttpServletRequest request) {
        if (videoId == null) {
            return Result.error(400, "视频ID不能为空");
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        if (userIdFromToken == null) {
            return Result.error(401, "用户未登录");
        }

        // 1. 限流控制：每用户每分钟最多收藏20次
        String rateLimitKey = "collect:" + userIdFromToken;
        boolean allowed = redisUtil.isAllowed(rateLimitKey, 20, 60000); // 20次/60秒
        if (!allowed) {
            return Result.error(429, "操作频率过高，请稍后再试");
        }

        // 2. 分布式锁：防止并发收藏导致数据不一致
        String lockKey = "lock:collect:" + userIdFromToken + ":" + videoId;
        String lockValue = UUID.randomUUID().toString();
        try {
            // 尝试获取锁，过期时间5秒
            boolean locked = redisUtil.acquireLock(lockKey, lockValue, 5000);
            if (!locked) {
                return Result.error(503, "系统繁忙，请稍后再试");
            }

            Usersee usersee=userseeService.selectByuseridAndByvideoid(userIdFromToken,videoId);
            boolean iscollect = false;

            if(usersee == null) {
                // 如果没有记录，创建新记录并收藏
                usersee = new Usersee();
                usersee.setUserId(userIdFromToken);
                usersee.setVideoId(videoId);
                usersee.setIfcollect("1");
                userseeService.add(usersee);
                iscollect = true;
                // 添加到收藏表
                Collect collect=new Collect();
                collect.setVideoId(videoId);
                collect.setUserId(userIdFromToken);
                collect.setFavorite(0);
                collect.setFavoriteName("默认收藏夹");
                collect.setCreateTime(new Date());
                collect.setIsPublic(1);
                collectService.add(collect);
            } else if ("1".equals(usersee.getIfcollect())) {
                // 如果已收藏，取消收藏
                usersee.setIfcollect("0");
                UserseeQuery updateParam = new UserseeQuery();
                updateParam.setUserId(userIdFromToken);
                updateParam.setVideoId(videoId);
                userseeService.updateByParam(usersee, updateParam);
                iscollect = false;
                CollectQuery updateParam2 = new CollectQuery();
                updateParam2.setUserId(userIdFromToken);
                updateParam2.setVideoId(videoId);
                collectService.deleteByParam(updateParam2);
            } else {
                // 如果有记录但未收藏，更新为已收藏
                usersee.setIfcollect("1");
                UserseeQuery updateParam = new UserseeQuery();
                updateParam.setUserId(userIdFromToken);
                updateParam.setVideoId(videoId);
                userseeService.updateByParam(usersee, updateParam);
                iscollect = true;
                // 添加到收藏表
                Collect collect=new Collect();
                collect.setVideoId(videoId);
                collect.setUserId(userIdFromToken);
                collect.setFavorite(0);
                collect.setFavoriteName("默认收藏夹");
                collect.setCreateTime(new Date());
                collect.setIsPublic(1);
                collectService.add(collect);
            }

            String redisKey = "video:followCount:" + videoId;
            if (iscollect) {
                redisUtil.incr(redisKey, 1);
            } else {
                // 确保Redis中的值不会小于0
                Object currentValue = redisUtil.get(redisKey);
                if (currentValue != null) {
                    int count = Integer.parseInt(currentValue.toString());
                    if (count > 0) {
                        redisUtil.decr(redisKey, 1);
                    }
                }
            }
            redisUtil.expire(redisKey, 3600);

            return Result.success(iscollect ? "收藏成功" : "取消收藏成功");
        } finally {
            // 释放分布式锁
            redisUtil.releaseLock(lockKey, lockValue);
        }
    }
    //获取用户不在一个合集的视频
    @LoginRequired
    @RequestMapping("/getExCompilationsVideo")
    public Result getExCompilationsVideo(HttpServletRequest request,Integer compilationsId) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        VideoQuery videoQuery=new VideoQuery();
        videoQuery.setUserId(userIdFromToken);
        List<Video> videoList1=videoService.findListByParam(videoQuery);
        CompilationsQuery compilationsQuery=new CompilationsQuery();
        compilationsQuery.setCompilationsId(compilationsId);
        compilationsQuery.setUserId(userIdFromToken);
        List<Compilations> compilationsList=compilationsService.findListByParam(compilationsQuery);
        List<Video> videoList2=new ArrayList<>();
        for(Compilations compilations:compilationsList){
            Video video=videoService.getVideoByVideoId(compilations.getVideoId());
            if(video!=null){
                videoList2.add(video);
            }
        }
        Set<Integer> compilationVideoIds = videoList2.stream().map(Video::getVideoId).collect(Collectors.toSet());
        List<Video> resultList = videoList1.stream()
                .filter(video -> !compilationVideoIds.contains(video.getVideoId()))
                .collect(Collectors.toList());
        return Result.success(resultList);
    }
    //在合集新加视频
    @LoginRequired
    @RequestMapping("/addVideo2CompilationsId")
    public Result addVideo2CompilationsId(HttpServletRequest request,Integer compilationsId,Integer videoId) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);
        Compilations compilations=new Compilations();
        compilations.setCompilationsId(compilationsId);
        compilations.setUserId(userIdFromToken);
        compilations.setVideoId(videoId);
        compilationsService.add(compilations);
        return Result.success();
    }
    
    // 根据标签搜索视频
    @RequestMapping("/searchByTags")
        public Result searchByTags(String tags, Integer page, Integer pageSize) {
        try {
            if (tags == null || tags.isEmpty()) {
                return Result.error(400, "标签不能为空");
            }
            
            // 解析标签，取第一个标签作为搜索关键词
            String[] tagArray = tags.split(",");
            String tag = tagArray[0].trim();
            
            VideoQuery query = new VideoQuery();
            query.setTitleFuzzy(tag); // 使用标题模糊搜索，因为标签可能存储在标题中
            query.setStatus("1"); // 只搜索已审核通过的视频
            
            if (page != null && pageSize != null) {
                query.setPageNo(page);
                query.setPageSize(pageSize);
            } else {
                query.setPageNo(1);
                query.setPageSize(10);
            }
            
            List<Video> videoList = videoService.selectVideo(query);
            return Result.success(videoList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "搜索视频失败");
        }
    }
}
