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
public class MomentCommentLike implements Serializable {


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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date likeTime;


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

	public void setLikeTime(Date likeTime){
		this.likeTime = likeTime;
	}

	public Date getLikeTime(){
		return this.likeTime;
	}

	@Override
	public String toString (){
		return "userId:"+(userId == null ? "空" : userId)+"，momentCommentId:"+(momentCommentId == null ? "空" : momentCommentId)+"，likeTime:"+(likeTime == null ? "空" : DateUtil.format(likeTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
