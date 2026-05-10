package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.shipin.entity.enums.DateTimePatternEnum;
import com.shipin.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 视频观看历史记录
 */
public class VideoHistory implements Serializable {


	/**
	 * 历史记录ID
	 */
	private Long id;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 视频ID
	 */
	private Long videoId;

	/**
	 * 观看进度（秒）
	 */
	private Integer progress;

	/**
	 * 最后观看时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastWatchTime;

	/**
	 * 观看时长（秒）
	 */
	private Integer watchDuration;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createdAt;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updatedAt;


	public void setId(Long id){
		this.id = id;
	}

	public Long getId(){
		return this.id;
	}

	public void setUserId(Long userId){
		this.userId = userId;
	}

	public Long getUserId(){
		return this.userId;
	}

	public void setVideoId(Long videoId){
		this.videoId = videoId;
	}

	public Long getVideoId(){
		return this.videoId;
	}

	public void setProgress(Integer progress){
		this.progress = progress;
	}

	public Integer getProgress(){
		return this.progress;
	}

	public void setLastWatchTime(Date lastWatchTime){
		this.lastWatchTime = lastWatchTime;
	}

	public Date getLastWatchTime(){
		return this.lastWatchTime;
	}

	public void setWatchDuration(Integer watchDuration){
		this.watchDuration = watchDuration;
	}

	public Integer getWatchDuration(){
		return this.watchDuration;
	}

	public void setCreatedAt(Date createdAt){
		this.createdAt = createdAt;
	}

	public Date getCreatedAt(){
		return this.createdAt;
	}

	public void setUpdatedAt(Date updatedAt){
		this.updatedAt = updatedAt;
	}

	public Date getUpdatedAt(){
		return this.updatedAt;
	}

	@Override
	public String toString (){
		return "历史记录ID:"+(id == null ? "空" : id)+"，用户ID:"+(userId == null ? "空" : userId)+"，视频ID:"+(videoId == null ? "空" : videoId)+"，观看进度（秒）:"+(progress == null ? "空" : progress)+"，最后观看时间:"+(lastWatchTime == null ? "空" : DateUtil.format(lastWatchTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，观看时长（秒）:"+(watchDuration == null ? "空" : watchDuration)+"，创建时间:"+(createdAt == null ? "空" : DateUtil.format(createdAt, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updatedAt == null ? "空" : DateUtil.format(updatedAt, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
