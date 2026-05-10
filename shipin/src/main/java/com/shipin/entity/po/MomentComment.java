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
public class MomentComment implements Serializable {


	/**
	 * 
	 */
	private Integer commentId;

	/**
	 * 
	 */
	private Integer userId;

	/**
	 * 
	 */
	private Integer momentId;

	/**
	 * 
	 */
	private Integer parentId;

	/**
	 * 
	 */
	private String content;

	/**
	 * 
	 */
	private Integer likeCount;

	/**
	 * 子评论列表
	 */
	private List<MomentComment> subComments;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 用户昵称
	 */
	private String nickName;

	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 是否被当前用户点赞
	 */
	private boolean isLiked;
	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;



	public void setCommentId(Integer commentId){
		this.commentId = commentId;
	}

	public Integer getCommentId(){
		return this.commentId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setMomentId(Integer momentId){
		this.momentId = momentId;
	}

	public Integer getMomentId(){
		return this.momentId;
	}

	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}

	public Integer getParentId(){
		return this.parentId;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setLikeCount(Integer likeCount){
		this.likeCount = likeCount;
	}

	public Integer getLikeCount(){
		return this.likeCount;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}


	/**
	 * 获取子评论列表
	 */
	public List<MomentComment> getSubComments() {
		return subComments;
	}

	/**
	 * 设置子评论列表
	 */
	public void setSubComments(List<MomentComment> subComments) {
		this.subComments = subComments;
	}

	/**
	 * 获取用户名
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * 设置用户名
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 获取用户昵称
	 */
	public String getNickName() {
		return nickName;
	}

	/**
	 * 设置用户昵称
	 */
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	/**
	 * 获取用户头像
	 */
	public String getUserAvatar() {
		return userAvatar;
	}

	/**
	 * 设置用户头像
	 */
	public void setUserAvatar(String userAvatar) {
		this.userAvatar = userAvatar;
	}

	/**
	 * 获取是否被当前用户点赞
	 */
	public boolean isLiked() {
		return isLiked;
	}

	/**
	 * 设置是否被当前用户点赞
	 */
	public void setLiked(boolean liked) {
		isLiked = liked;
	}
	@Override
	public String toString (){
		return "commentId:"+(commentId == null ? "空" : commentId)+"，userId:"+(userId == null ? "空" : userId)+"，momentId:"+(momentId == null ? "空" : momentId)+"，parentId:"+(parentId == null ? "空" : parentId)+"，content:"+(content == null ? "空" : content)+"，likeCount:"+(likeCount == null ? "空" : likeCount)+"，createTime:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
