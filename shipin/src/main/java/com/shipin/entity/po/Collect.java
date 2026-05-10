package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 
 */
public class Collect implements Serializable {


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
	private Integer favorite;

	/**
	 * 
	 */
	private String favoriteName;

	/**
	 * 
	 */
	private Integer isPublic;

	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;


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

	public void setFavorite(Integer favorite){
		this.favorite = favorite;
	}

	public Integer getFavorite(){
		return this.favorite;
	}

	public void setFavoriteName(String favoriteName){
		this.favoriteName = favoriteName;
	}

	public String getFavoriteName(){
		return this.favoriteName;
	}

	public void setIsPublic(Integer isPublic){
		this.isPublic = isPublic;
	}

	public Integer getIsPublic(){
		return this.isPublic;
	}

	public void setCreateTime(java.util.Date createTime){
		this.createTime = createTime;
	}

	public java.util.Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "userId:"+(userId == null ? "空" : userId)+"，videoId:"+(videoId == null ? "空" : videoId)+"，favorite:"+(favorite == null ? "空" : favorite)+"，favoriteName:"+(favoriteName == null ? "空" : favoriteName)+"，isPublic:"+(isPublic == null ? "空" : isPublic)+"，createTime:"+(createTime == null ? "空" : createTime);
	}
}
