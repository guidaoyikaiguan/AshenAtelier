package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import com.shipin.entity.enums.DateTimePatternEnum;
import com.shipin.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 
 */
public class Danmaku implements Serializable {


	/**
	 * 
	 */
	private Integer id;

	/**
	 * 
	 */
	private Integer videoId;

	/**
	 * 
	 */
	private Integer userId;

	/**
	 * 
	 */
	private String username;

	/**
	 * 
	 */
	private String content;

	/**
	 * 
	 */
	private BigDecimal time;

	/**
	 * 
	 */
	private String color;

	/**
	 * 
	 */
	private Integer type;

	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setVideoId(Integer videoId){
		this.videoId = videoId;
	}

	public Integer getVideoId(){
		return this.videoId;
	}

	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getUsername(){
		return this.username;
	}

	public void setContent(String content){
		this.content = content;
	}

	public String getContent(){
		return this.content;
	}

	public void setTime(BigDecimal time){
		this.time = time;
	}

	public BigDecimal getTime(){
		return this.time;
	}

	public void setColor(String color){
		this.color = color;
	}

	public String getColor(){
		return this.color;
	}

	public void setType(Integer type){
		this.type = type;
	}

	public Integer getType(){
		return this.type;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "id:"+(id == null ? "空" : id)+"，videoId:"+(videoId == null ? "空" : videoId)+"，userId:"+(userId == null ? "空" : userId)+"，username:"+(username == null ? "空" : username)+"，content:"+(content == null ? "空" : content)+"，time:"+(time == null ? "空" : time)+"，color:"+(color == null ? "空" : color)+"，type:"+(type == null ? "空" : type)+"，createTime:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
