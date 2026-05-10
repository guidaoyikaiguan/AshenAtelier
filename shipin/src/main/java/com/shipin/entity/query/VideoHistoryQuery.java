package com.shipin.entity.query;

import java.util.Date;


/**
 * 视频观看历史记录参数
 */
public class VideoHistoryQuery extends BaseParam {


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
	private String lastWatchTime;

	private String lastWatchTimeStart;

	private String lastWatchTimeEnd;

	/**
	 * 观看时长（秒）
	 */
	private Integer watchDuration;

	/**
	 * 创建时间
	 */
	private String createdAt;

	private String createdAtStart;

	private String createdAtEnd;

	/**
	 * 更新时间
	 */
	private String updatedAt;

	private String updatedAtStart;

	private String updatedAtEnd;


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

	public void setLastWatchTime(String lastWatchTime){
		this.lastWatchTime = lastWatchTime;
	}

	public String getLastWatchTime(){
		return this.lastWatchTime;
	}

	public void setLastWatchTimeStart(String lastWatchTimeStart){
		this.lastWatchTimeStart = lastWatchTimeStart;
	}

	public String getLastWatchTimeStart(){
		return this.lastWatchTimeStart;
	}
	public void setLastWatchTimeEnd(String lastWatchTimeEnd){
		this.lastWatchTimeEnd = lastWatchTimeEnd;
	}

	public String getLastWatchTimeEnd(){
		return this.lastWatchTimeEnd;
	}

	public void setWatchDuration(Integer watchDuration){
		this.watchDuration = watchDuration;
	}

	public Integer getWatchDuration(){
		return this.watchDuration;
	}

	public void setCreatedAt(String createdAt){
		this.createdAt = createdAt;
	}

	public String getCreatedAt(){
		return this.createdAt;
	}

	public void setCreatedAtStart(String createdAtStart){
		this.createdAtStart = createdAtStart;
	}

	public String getCreatedAtStart(){
		return this.createdAtStart;
	}
	public void setCreatedAtEnd(String createdAtEnd){
		this.createdAtEnd = createdAtEnd;
	}

	public String getCreatedAtEnd(){
		return this.createdAtEnd;
	}

	public void setUpdatedAt(String updatedAt){
		this.updatedAt = updatedAt;
	}

	public String getUpdatedAt(){
		return this.updatedAt;
	}

	public void setUpdatedAtStart(String updatedAtStart){
		this.updatedAtStart = updatedAtStart;
	}

	public String getUpdatedAtStart(){
		return this.updatedAtStart;
	}
	public void setUpdatedAtEnd(String updatedAtEnd){
		this.updatedAtEnd = updatedAtEnd;
	}

	public String getUpdatedAtEnd(){
		return this.updatedAtEnd;
	}

}
