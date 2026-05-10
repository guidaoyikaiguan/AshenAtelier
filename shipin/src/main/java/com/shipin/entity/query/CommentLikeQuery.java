package com.shipin.entity.query;

import java.util.Date;


/**
 * 参数
 */
public class CommentLikeQuery extends BaseParam {


	/**
	 * 
	 */
	private Integer userId;

	/**
	 * 
	 */
	private Integer commentId;

	/**
	 * 
	 */
	private String likeTime;

	private String likeTimeStart;

	private String likeTimeEnd;


	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setCommentId(Integer commentId){
		this.commentId = commentId;
	}

	public Integer getCommentId(){
		return this.commentId;
	}

	public void setLikeTime(String likeTime){
		this.likeTime = likeTime;
	}

	public String getLikeTime(){
		return this.likeTime;
	}

	public void setLikeTimeStart(String likeTimeStart){
		this.likeTimeStart = likeTimeStart;
	}

	public String getLikeTimeStart(){
		return this.likeTimeStart;
	}
	public void setLikeTimeEnd(String likeTimeEnd){
		this.likeTimeEnd = likeTimeEnd;
	}

	public String getLikeTimeEnd(){
		return this.likeTimeEnd;
	}

}
