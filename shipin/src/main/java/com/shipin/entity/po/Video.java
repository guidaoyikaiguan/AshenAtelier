package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.shipin.entity.enums.DateTimePatternEnum;
import com.shipin.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 
 */
public class Video implements Serializable {


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

	/**
	 * 视频描述
	 */
	private String description;

	/**
	 * 视频存储路径
	 */
	private String videoUrl;

	/**
	 * 视频封面图路径
	 */
	private String coverUrl;

	/**
	 * 视频时长（秒）
	 */
	private String duration;

	/**
	 * 视频分辨率
	 */
	private String resolution;

	/**
	 * 播放量
	 */
	private String playCount;

	/**
	 * 点赞数
	 */
	private String likeCount;

	/**
	 * 评论数
	 */
	private String commentCount;

	/**
	 * 状态（0：待审核，1：已通过，2：已拒绝）
	 */
	private String status;

	/**
	 * 上传时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	/**
	 * 上传用户昵称
	 */
	private String nickName;

	/**
	 * 用户点赞状态
	 */
	private String iflike;

	/**
	 * 用户收藏状态
	 */
	private String ifcollect;

	/**
	 * 当前用户投币数
	 */
	private Integer userCoinsInserted;

	/**
	 * 视频标签
	 */
	private String tags;

	private String[] tagsArray;

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public Integer getFans() {
		return fans;
	}

	public void setFans(Integer fans) {
		this.fans = fans;
	}

	private String avatar;

	private Integer fans;

	private String coinsInserted;

	private String followCount;

	public String getCoinsInserted() {
		return coinsInserted;
	}

	public void setCoinsInserted(String coinsInserted) {
		this.coinsInserted = coinsInserted;
	}

	public String getFollowCount() {
		return followCount;
	}

	public void setFollowCount(String followCount) {
		this.followCount = followCount;
	}

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

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
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

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return this.description;
	}

	public void setVideoUrl(String videoUrl){
		this.videoUrl = videoUrl;
	}

	public String getVideoUrl(){
		return this.videoUrl;
	}

	public void setCoverUrl(String coverUrl){
		this.coverUrl = coverUrl;
	}

	public String getCoverUrl(){
		return this.coverUrl;
	}

	public void setDuration(String duration){
		this.duration = duration;
	}

	public String getDuration(){
		return this.duration;
	}

	public void setPlayCount(String playCount){
		this.playCount = playCount;
	}

	public String getPlayCount(){
		return this.playCount;
	}

	public void setLikeCount(String likeCount){
		this.likeCount = likeCount;
	}

	public String getLikeCount(){
		return this.likeCount;
	}

	public void setCommentCount(String commentCount){
		this.commentCount = commentCount;
	}

	public String getCommentCount(){
		return this.commentCount;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return this.status;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	public void setNickName(String nickName){
		this.nickName = nickName;
	}

	public String getNickName(){
		return this.nickName;
	}

	public void setIflike(String iflike){
		this.iflike = iflike;
	}

	public String getIflike(){
		return this.iflike;
	}

	public void setIfcollect(String ifcollect){
		this.ifcollect = ifcollect;
	}

	public String getIfcollect(){
		return this.ifcollect;
	}

	public void setUserCoinsInserted(Integer userCoinsInserted){
		this.userCoinsInserted = userCoinsInserted;
	}

	public Integer getUserCoinsInserted(){
		return this.userCoinsInserted;
	}

	// 临时字段，用于接收前端的数组格式标签
	public void setTagsArray(String[] tagsArray) {
		this.tagsArray = tagsArray;
		if (tagsArray != null && tagsArray.length > 0) {
			this.tags = String.join(",", tagsArray);
		}
	}

	public String[] getTagsArray() {
		return tagsArray;
	}

	@Override
	public String toString (){
		return "视频ID:"+(videoId == null ? "空" : videoId)+"，上传用户ID:"+(userId == null ? "空" : userId)+"，上传用户昵称:"+(nickName == null ? "空" : nickName)+"，视频分类ID:"+(categroyId == null ? "空" : categroyId)+"，视频标题:"+(title == null ? "空" : title)+"，视频描述:"+(description == null ? "空" : description)+"，视频标签:"+(tags == null ? "空" : tags)+"，视频存储路径:"+(videoUrl == null ? "空" : videoUrl)+"，视频封面图路径:"+(coverUrl == null ? "空" : coverUrl)+"，视频时长（秒）:"+(duration == null ? "空" : duration)+"，播放量:"+(playCount == null ? "空" : playCount)+"，点赞数:"+(likeCount == null ? "空" : likeCount)+"，评论数:"+(commentCount == null ? "空" : commentCount)+"，投币数:"+(coinsInserted == null ? "空" : coinsInserted)+"，收藏数:"+(followCount == null ? "空" : followCount)+"，状态（0：待审核，1：已通过，2：已拒绝）:"+(status == null ? "空" : status)+"，上传时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
