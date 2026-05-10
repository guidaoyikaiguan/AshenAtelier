package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 
 */
public class Usersee implements Serializable {


	/**
	 * 用户id
	 */
	private Integer userId;

	/**
	 * 视频id
	 */
	private Integer videoId;

	/**
	 * 是否点赞
	 */
	private String iflike;

	/**
	 * 是否收藏
	 */
	private String ifcollect;

	/**
	 * 投币
	 */
	private Integer insertcoins;


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

	public void setInsertcoins(Integer insertcoins){
		this.insertcoins = insertcoins;
	}

	public Integer getInsertcoins(){
		return this.insertcoins;
	}

	@Override
	public String toString (){
		return "用户id:"+(userId == null ? "空" : userId)+"，视频id:"+(videoId == null ? "空" : videoId)+"，是否点赞:"+(iflike == null ? "空" : iflike)+"，是否收藏:"+(ifcollect == null ? "空" : ifcollect)+"，投币:"+(insertcoins == null ? "空" : insertcoins);
	}
}
