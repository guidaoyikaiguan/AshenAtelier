package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 
 */
public class Compilations implements Serializable {


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
	private Integer compilationsId;

	/**
	 * 
	 */
	private String compilationsName;


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

	public void setCompilationsId(Integer compilationsId){
		this.compilationsId = compilationsId;
	}

	public Integer getCompilationsId(){
		return this.compilationsId;
	}

	public void setCompilationsName(String compilationsName){
		this.compilationsName = compilationsName;
	}

	public String getCompilationsName(){
		return this.compilationsName;
	}

	@Override
	public String toString (){
		return "userId:"+(userId == null ? "空" : userId)+"，videoId:"+(videoId == null ? "空" : videoId)+"，compilationsId:"+(compilationsId == null ? "空" : compilationsId)+"，compilationsName:"+(compilationsName == null ? "空" : compilationsName);
	}
}
