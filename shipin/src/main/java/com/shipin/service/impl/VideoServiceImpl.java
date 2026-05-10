package com.shipin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.shipin.mappers.FansMapper;
import com.shipin.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shipin.entity.enums.PageSize;
import com.shipin.entity.query.VideoQuery;
import com.shipin.entity.po.Video;
import com.shipin.entity.vo.PaginationResultVO;
import com.shipin.entity.query.SimplePage;
import com.shipin.mappers.VideoMapper;
import com.shipin.service.VideoService;
import com.shipin.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 *  业务接口实现
 */
@Service("videoService")
public class VideoServiceImpl implements VideoService {

	@Value("${app.storage.root:D:/shipin}")
	private String storageRoot;

	@Resource
	private VideoMapper<Video, VideoQuery> videoMapper;

	@Resource
	private FansMapper fansMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<Video> findListByParam(VideoQuery param) {
		return this.videoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(VideoQuery param) {
		return this.videoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<Video> findListByPage(VideoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<Video> list = this.findListByParam(param);
		PaginationResultVO<Video> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(Video bean) {
		return this.videoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<Video> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<Video> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.videoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(Video bean, VideoQuery param) {
		StringTools.checkParam(param);
		return this.videoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(VideoQuery param) {
		StringTools.checkParam(param);
		return this.videoMapper.deleteByParam(param);
	}

	/**
	 * 根据VideoId获取对象
	 */
	@Override
	public Video getVideoByVideoId(Integer videoId) {
		return this.videoMapper.selectByVideoId(videoId);
	}

	/**
	 * 根据VideoId修改
	 */
	@Override
	public Integer updateVideoByVideoId(Video bean, Integer videoId) {
		return this.videoMapper.updateByVideoId(bean, videoId);
	}

	/**
	 * 根据VideoId删除
	 */
	@Override
	public Integer deleteVideoByVideoId(Integer videoId) {
		return this.videoMapper.deleteByVideoId(videoId);
	}

	@Override
	public List<Video> selectVideo(VideoQuery query) {
		int count = this.findCountByParam(query);
		int pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();

		SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
		query.setSimplePage(page);
		List<Video> list = videoMapper.selectVideo(query);
		PaginationResultVO<Video> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result.getList();
	}

	@Override
	public Video selectSeeVideo(VideoQuery query) {
		Video video1 = videoMapper.selectByVideoId(query.getVideoId());
		Integer followerCount = fansMapper.getFollowerCount(video1.getUserId());
		Video video = videoMapper.selectSeeVideo(query);
		if (video != null) {
			video.setFans(followerCount);
			return video;
		}
		return null;
	}

    @Resource
    private com.shipin.utils.RedisUtil redisUtil;

    @Override
    @org.springframework.scheduling.annotation.Async
    @Transactional
    public java.util.concurrent.CompletableFuture<Boolean> processVideoAsync(String videoPath, Integer videoId) {
        // 分布式锁：确保同一视频不会被多个线程同时处理
        String lockKey = "lock:process_video:" + videoId;
        String lockValue = java.util.UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁，过期时间300秒（5分钟），确保视频处理有足够时间
            boolean locked = redisUtil.acquireLock(lockKey, lockValue, 300000);
            if (!locked) {
                System.err.println("视频正在处理中，videoId: " + videoId);
                return java.util.concurrent.CompletableFuture.completedFuture(false);
            }
            
            System.out.println("开始异步处理视频: " + videoPath + ", videoId: " + videoId);
            
            // 1. 生成视频缩略图（如果还没有封面）
            Video video = videoMapper.selectByVideoId(videoId);
            if (video == null) {
                System.err.println("视频不存在，videoId: " + videoId);
                return java.util.concurrent.CompletableFuture.completedFuture(false);
            }
            
            // 检查视频状态，如果已经处理完成，直接返回
            if ("1".equals(video.getStatus())) {
                System.out.println("视频已处理完成，跳过处理: " + videoId);
                return java.util.concurrent.CompletableFuture.completedFuture(true);
            }
            
            boolean needUpdate = false;
            
            // 生成封面
            if (video.getCoverUrl() == null || video.getCoverUrl().isEmpty()) {
                String videoUrl = video.getVideoUrl();
                if (videoUrl != null && !videoUrl.isEmpty()) {
                    String coverDir = videoUrl.substring(0, videoUrl.lastIndexOf("/"));
                    coverDir = coverDir.replace("/video", "/cover");
                    String coverPath = storageRoot + coverDir + "/";
                    java.io.File coverDirFile = new java.io.File(coverPath);
                    if (!coverDirFile.exists()) {
                        coverDirFile.mkdirs();
                    }
                    String coverFileName = java.util.UUID.randomUUID().toString() + ".jpg";
                    String coverFilePath = coverPath + coverFileName;
                    boolean extracted = com.shipin.utils.VideoUtil.extractFirstFrame(videoPath, coverFilePath);
                    if (extracted) {
                        String coverRelativePath = coverDir + "/" + coverFileName;
                        video.setCoverUrl(coverRelativePath);
                        needUpdate = true;
                        System.out.println("视频封面生成成功: " + coverFilePath);
                    }
                }
            }
            
            // 2. 提取视频信息（时长、分辨率等）
            org.bytedeco.javacv.FFmpegFrameGrabber grabber = null;
            int width = 0;
            int height = 0;
            try {
                grabber = new org.bytedeco.javacv.FFmpegFrameGrabber(videoPath);
                grabber.start();
                
                // 获取视频时长（秒）
                double duration = grabber.getLengthInTime() / 1000000.0;
                int durationMinutes = (int) (duration / 60);
                int durationSeconds = (int) (duration % 60);
                String durationStr = String.format("%d:%02d", durationMinutes, durationSeconds);
                
                // 获取视频分辨率
                width = grabber.getImageWidth();
                height = grabber.getImageHeight();
                String resolution = width + "x" + height;
                
                // 更新视频信息
                video.setDuration(durationStr);
                video.setResolution(resolution);
                needUpdate = true;
                
                System.out.println("视频信息提取成功 - 时长: " + durationStr + ", 分辨率: " + resolution);
                
                grabber.stop();
            } catch (Exception e) {
                System.err.println("提取视频信息失败: " + e.getMessage());
            } finally {
                if (grabber != null) {
                    try {
                        grabber.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            // 3. 视频转码为720p
            String transcodedVideoPath = videoPath;
            int targetWidth = 1280;
            int targetHeight = 720;
            
            // 检查是否需要转码（如果视频已经是720p或更小，则不需要转码）
            if (width > targetWidth || height > targetHeight) {
                System.out.println("开始视频转码为720p: " + videoPath);
                
                // 生成转码后的视频文件名
                String videoDir = new java.io.File(videoPath).getParent();
                String transcodedFileName = "720p_" + new java.io.File(videoPath).getName();
                transcodedVideoPath = videoDir + java.io.File.separator + transcodedFileName;
                
                // 执行视频转码
                boolean transcoded = transcodeTo720p(videoPath, transcodedVideoPath, targetWidth, targetHeight);
                
                if (transcoded) {
                    System.out.println("视频转码成功: " + transcodedVideoPath);
                    
                    // 删除原视频文件（可选）
                    // new java.io.File(videoPath).delete();
                    
                    // 更新视频URL为转码后的视频路径
                    String originalVideoUrl = video.getVideoUrl();
                    if (originalVideoUrl != null && !originalVideoUrl.isEmpty()) {
                        String transcodedVideoUrl = originalVideoUrl.substring(0, originalVideoUrl.lastIndexOf("/") + 1) + transcodedFileName;
                        video.setVideoUrl(transcodedVideoUrl);
                        needUpdate = true;
                    }
                } else {
                    System.err.println("视频转码失败，使用原视频: " + videoPath);
                }
            } else {
                System.out.println("视频分辨率已满足要求，无需转码: " + width + "x" + height);
            }
            
            // 4. 更新视频状态和所有修改的字段
            video.setStatus("0"); // 0：保持待处理状态
            video.setUpdateTime(new java.util.Date());
            videoMapper.updateByVideoId(video, videoId);
            
            System.out.println("视频处理完成: " + videoPath);
            
            // 处理成功
            return java.util.concurrent.CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            System.err.println("视频处理失败: " + e.getMessage());
            e.printStackTrace();
            
            // 更新视频状态为处理失败
            try {
                Video updateStatus = new Video();
                updateStatus.setVideoId(videoId);
                updateStatus.setStatus("2"); // 2：处理失败
                updateStatus.setUpdateTime(new java.util.Date());
                videoMapper.updateByVideoId(updateStatus, videoId);
            } catch (Exception ex) {
                System.err.println("更新视频状态失败: " + ex.getMessage());
            }
            
            // 处理失败
            return java.util.concurrent.CompletableFuture.completedFuture(false);
        } finally {
            // 释放分布式锁
            redisUtil.releaseLock(lockKey, lockValue);
        }
    }


    /**
     * 视频转码为720p
     * @param inputPath 输入视频路径
     * @param outputPath 输出视频路径
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 是否转码成功
     */
    private boolean transcodeTo720p(String inputPath, String outputPath, int targetWidth, int targetHeight) {
        org.bytedeco.javacv.FFmpegFrameGrabber grabber = null;
        org.bytedeco.javacv.FFmpegFrameRecorder recorder = null;
        
        try {
            // 创建抓取器
            grabber = new org.bytedeco.javacv.FFmpegFrameGrabber(inputPath);
            grabber.start();
            
            // 获取原视频信息
            int originalWidth = grabber.getImageWidth();
            int originalHeight = grabber.getImageHeight();
            double frameRate = grabber.getVideoFrameRate();
            int audioChannels = grabber.getAudioChannels();
            
            // 计算保持宽高比的尺寸
            double aspectRatio = (double) originalWidth / originalHeight;
            int scaledWidth = targetWidth;
            int scaledHeight = targetHeight;
            
            if (aspectRatio > (double) targetWidth / targetHeight) {
                scaledHeight = (int) (targetWidth / aspectRatio);
            } else {
                scaledWidth = (int) (targetHeight * aspectRatio);
            }
            
            // 创建录制器
            recorder = new org.bytedeco.javacv.FFmpegFrameRecorder(outputPath, scaledWidth, scaledHeight, audioChannels);
            
            // 设置视频编码参数
            recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(frameRate > 0 ? frameRate : 30.0);
            recorder.setVideoBitrate(2500000); // 2.5Mbps比特率
            recorder.setVideoQuality(0.0); // 最高质量
            
            // 设置音频编码参数
            if (audioChannels > 0) {
                recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC);
                recorder.setSampleRate(grabber.getSampleRate());
                recorder.setAudioBitrate(128000); // 128kbps音频比特率
            }
            
            // 设置H264编码参数
            recorder.setVideoOption("preset", "medium");
            recorder.setVideoOption("crf", "23");
            
            recorder.start();
            
            // 逐帧处理
            org.bytedeco.javacv.Frame frame;
            while ((frame = grabber.grab()) != null) {
                recorder.record(frame);
            }
            
            recorder.stop();
            grabber.stop();
            
            System.out.println("视频转码完成: " + outputPath + ", 分辨率: " + scaledWidth + "x" + scaledHeight);
            return true;
            
        } catch (Exception e) {
            System.err.println("视频转码失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public Integer selectUserLikes(Integer userIdFromToken) {
        return videoMapper.selectUserLikes(userIdFromToken);
    }

    @Override
    public Integer selectUserPlays(Integer userIdFromToken) {
        return videoMapper.selectUserPlays(userIdFromToken);
    }

	@Override
	public List<Video> selectByTags(String tag, VideoQuery query) {
		return videoMapper.selectByTags(tag, query);
	}
}