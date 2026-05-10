package com.shipin.entity.query;

import java.util.Date;


/**
 * 参数
 */
public class MomentCommentLikeQuery extends BaseParam {


	/**
	 * 
	 */
	private Integer userId;

	/**
	 * 
	 */
	private Integer momentCommentId;

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

	public void setMomentCommentId(Integer momentCommentId){
		this.momentCommentId = momentCommentId;
	}

	public Integer getMomentCommentId(){
		return this.momentCommentId;
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
