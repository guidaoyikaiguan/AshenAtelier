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
public class MomentLike implements Serializable {


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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


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

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "likeId:"+(likeId == null ? "空" : likeId)+"，userId:"+(userId == null ? "空" : userId)+"，momentId:"+(momentId == null ? "空" : momentId)+"，createTime:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
