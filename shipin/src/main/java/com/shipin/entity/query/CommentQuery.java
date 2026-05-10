package com.shipin.entity.query;

import java.util.Date;


/**
 * 参数
 */
public class CommentQuery extends BaseParam {


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
	private Integer videoId;

	/**
	 * 
	 */
	private String content;

	private String contentFuzzy;

	/**
	 * 
	 */
	private String commentTime;

	private String commentTimeStart;

	private String commentTimeEnd;

	/**
	 * 
	 */
	private Integer parentId;

	/**
	 * 
	 */
	private Integer likeCount;

	/**
	 * 是否置顶
	 */
	private Integer ifTop;


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

	public void setVideoId(Integer videoId){
		this.videoId = videoId;
	}

	public Integer getVideoId(){
		return this.videoId;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setContentFuzzy(String contentFuzzy){
		this.contentFuzzy = contentFuzzy;
	}

	public String getContentFuzzy(){
		return this.contentFuzzy;
	}

	public void setCommentTime(String commentTime){
		this.commentTime = commentTime;
	}

	public String getCommentTime(){
		return this.commentTime;
	}

	public void setCommentTimeStart(String commentTimeStart){
		this.commentTimeStart = commentTimeStart;
	}

	public String getCommentTimeStart(){
		return this.commentTimeStart;
	}
	public void setCommentTimeEnd(String commentTimeEnd){
		this.commentTimeEnd = commentTimeEnd;
	}

	public String getCommentTimeEnd(){
		return this.commentTimeEnd;
	}

	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}

	public Integer getParentId(){
		return this.parentId;
	}

	public void setLikeCount(Integer likeCount){
		this.likeCount = likeCount;
	}

	public Integer getLikeCount(){
		return this.likeCount;
	}

	public void setIfTop(Integer ifTop){
		this.ifTop = ifTop;
	}

	public Integer getIfTop(){
		return this.ifTop;
	}

}
