package com.shipin.entity.query;

import java.util.Date;


/**
 * 参数
 */
public class VideoQuery extends BaseParam {


	/**
	 * 视频ID
	 */
	private Integer videoId;

	/**
	 * 上传用户ID
	 */
	private Integer userId;

	/**
	 * 视频分类ID
	 */
	private Integer categroyId;

	/**
	 * 视频标题
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 视频描述
	 */
	private String description;

	private String descriptionFuzzy;

	/**
	 * 视频存储路径
	 */
	private String videoUrl;

	private String videoUrlFuzzy;

	/**
	 * 视频封面图路径
	 */
	private String coverUrl;

	private String coverUrlFuzzy;

	/**
	 * 视频时长（秒）
	 */
	private String duration;

	private String durationFuzzy;

	/**
	 * 播放量
	 */
	private String playCount;

	private String playCountFuzzy;

	/**
	 * 点赞数
	 */
	private String likeCount;

	private String likeCountFuzzy;

	/**
	 * 评论数
	 */
	private String commentCount;

	private String commentCountFuzzy;

	/**
	 * 状态（0：待审核，1：已通过，2：已拒绝）
	 */
	private String status;

	private String statusFuzzy;

	/**
	 * 上传时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;

	/**
	 * 硬币数
	 */
	private String coinsInserted;

	private String coinsInsertedFuzzy;

	/**
	 * 收藏数
	 */
	private String followCount;

	private String followCountFuzzy;


	public void setVideoId(Integer videoId){
		this.videoId = videoId;
	}

	public Integer getVideoId(){
		return this.videoId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setCategroyId(Integer categroyId){
		this.categroyId = categroyId;
	}

	public Integer getCategroyId(){
		return this.categroyId;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setTitleFuzzy(String titleFuzzy){
		this.titleFuzzy = titleFuzzy;
	}

	public String getTitleFuzzy(){
		return this.titleFuzzy;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setDescriptionFuzzy(String descriptionFuzzy){
		this.descriptionFuzzy = descriptionFuzzy;
	}

	public String getDescriptionFuzzy(){
		return this.descriptionFuzzy;
	}

	public void setVideoUrl(String videoUrl){
		this.videoUrl = videoUrl;
	}

	public String getVideoUrl(){
		return this.videoUrl;
	}

	public void setVideoUrlFuzzy(String videoUrlFuzzy){
		this.videoUrlFuzzy = videoUrlFuzzy;
	}

	public String getVideoUrlFuzzy(){
		return this.videoUrlFuzzy;
	}

	public void setCoverUrl(String coverUrl){
		this.coverUrl = coverUrl;
	}

	public String getCoverUrl(){
		return this.coverUrl;
	}

	public void setCoverUrlFuzzy(String coverUrlFuzzy){
		this.coverUrlFuzzy = coverUrlFuzzy;
	}

	public String getCoverUrlFuzzy(){
		return this.coverUrlFuzzy;
	}

	public void setDuration(String duration){
		this.duration = duration;
	}

	public String getDuration(){
		return this.duration;
	}

	public void setDurationFuzzy(String durationFuzzy){
		this.durationFuzzy = durationFuzzy;
	}

	public String getDurationFuzzy(){
		return this.durationFuzzy;
	}

	public void setPlayCount(String playCount){
		this.playCount = playCount;
	}

	public String getPlayCount(){
		return this.playCount;
	}

	public void setPlayCountFuzzy(String playCountFuzzy){
		this.playCountFuzzy = playCountFuzzy;
	}

	public String getPlayCountFuzzy(){
		return this.playCountFuzzy;
	}

	public void setLikeCount(String likeCount){
		this.likeCount = likeCount;
	}

	public String getLikeCount(){
		return this.likeCount;
	}

	public void setLikeCountFuzzy(String likeCountFuzzy){
		this.likeCountFuzzy = likeCountFuzzy;
	}

	public String getLikeCountFuzzy(){
		return this.likeCountFuzzy;
	}

	public void setCommentCount(String commentCount){
		this.commentCount = commentCount;
	}

	public String getCommentCount(){
		return this.commentCount;
	}

	public void setCommentCountFuzzy(String commentCountFuzzy){
		this.commentCountFuzzy = commentCountFuzzy;
	}

	public String getCommentCountFuzzy(){
		return this.commentCountFuzzy;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return this.status;
	}

	public void setStatusFuzzy(String statusFuzzy){
		this.statusFuzzy = statusFuzzy;
	}

	public String getStatusFuzzy(){
		return this.statusFuzzy;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}

	public String getUpdateTime(){
		return this.updateTime;
	}

	public void setUpdateTimeStart(String updateTimeStart){
		this.updateTimeStart = updateTimeStart;
	}

	public String getUpdateTimeStart(){
		return this.updateTimeStart;
	}
	public void setUpdateTimeEnd(String updateTimeEnd){
		this.updateTimeEnd = updateTimeEnd;
	}

	public String getUpdateTimeEnd(){
		return this.updateTimeEnd;
	}

	public void setCoinsInserted(String coinsInserted){
		this.coinsInserted = coinsInserted;
	}

	public String getCoinsInserted(){
		return this.coinsInserted;
	}

	public void setCoinsInsertedFuzzy(String coinsInsertedFuzzy){
		this.coinsInsertedFuzzy = coinsInsertedFuzzy;
	}

	public String getCoinsInsertedFuzzy(){
		return this.coinsInsertedFuzzy;
	}

	public void setFollowCount(String followCount){
		this.followCount = followCount;
	}

	public String getFollowCount(){
		return this.followCount;
	}

	public void setFollowCountFuzzy(String followCountFuzzy){
		this.followCountFuzzy = followCountFuzzy;
	}

	public String getFollowCountFuzzy(){
		return this.followCountFuzzy;
	}

}
