package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.shipin.entity.enums.DateTimePatternEnum;
import com.shipin.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.List;


/**
 * 
 */
public class Moment implements Serializable {


	/**
	 * 
	 */
	private Integer momentId;

	/**
	 * 
	 */
	private Integer userId;

	/**
	 * 
	 */
	private String content;

	/**
	 * 
	 */
	private Integer imageCount;

	/**
	 * 
	 */
	private Integer likeCount;

	/**
	 * 
	 */
	private Integer commentCount;

	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	private List<MomentImage> images;

	private String userName;

	private String avatar;

	public void setMomentId(Integer momentId){
		this.momentId = momentId;
	}

	public Integer getMomentId(){
		return this.momentId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setImageCount(Integer imageCount){
		this.imageCount = imageCount;
	}

	public Integer getImageCount(){
		return this.imageCount;
	}

	public void setLikeCount(Integer likeCount){
		this.likeCount = likeCount;
	}

	public Integer getLikeCount(){
		return this.likeCount;
	}

	public void setCommentCount(Integer commentCount){
		this.commentCount = commentCount;
	}

	public Integer getCommentCount(){
		return this.commentCount;
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

	public void setImages(List<MomentImage> images) {
		this.images = images;
	}

	public List<MomentImage> getImages() {
		return images;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	@Override
	public String toString (){
		return "momentId:"+(momentId == null ? "空" : momentId)+"，userId:"+(userId == null ? "空" : userId)+"，content:"+(content == null ? "空" : content)+"，imageCount:"+(imageCount == null ? "空" : imageCount)+"，likeCount:"+(likeCount == null ? "空" : likeCount)+"，commentCount:"+(commentCount == null ? "空" : commentCount)+"，createTime:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，updateTime:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
