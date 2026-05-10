package com.shipin.entity.query;

import java.util.Date;


/**
 * 参数
 */
public class MomentLikeQuery extends BaseParam {


	/**
	 * 
	 */
	private Integer likeId;

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
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setLikeId(Integer likeId){
		this.likeId = likeId;
	}

	public Integer getLikeId(){
		return this.likeId;
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

}
