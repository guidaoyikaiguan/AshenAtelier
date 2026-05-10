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
public class User implements Serializable {


	/**
	 * 
	 */
	private Integer userId;

	/**
	 * 
	 */
	private String email;

	/**
	 * 
	 */
	private String nickName;

	/**
	 * 
	 */
	private String password;

	/**
	 * 
	 */
	private String avatar;

	private String mySignature;

	private Integer myCoin;

	private Integer gender;

	private String my_Announcement;

	private Integer followerCount;

@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd")
private Date lastLoginTime;

/**
 * 用户状态：1-正常，2-封禁，0-未激活
 */
private Integer state;

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;


	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return this.email;
	}

	public void setNickName(String nickName){
		this.nickName = nickName;
	}

	public String getNickName(){
		return this.nickName;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setAvatar(String avatar){
		this.avatar = avatar;
	}

	public String getAvatar(){
		return this.avatar;
	}

	public void setMySignature(String mySignature){
		this.mySignature = mySignature;
	}

	public String getMySignature(){
		return this.mySignature;
	}

	public void setMyCoin(Integer myCoin){
		this.myCoin = myCoin;
	}

	public Integer getMyCoin(){
		return this.myCoin;
	}

	public void setGender(Integer gender){
		this.gender = gender;
	}

	public Integer getGender(){
		return this.gender;
	}

	public void setMy_Announcement(String my_Announcement){
		this.my_Announcement = my_Announcement;
	}

	public String getMy_Announcement(){
		return this.my_Announcement;
	}

	public void setFollowerCount(Integer followerCount){
	this.followerCount = followerCount;
}

public Integer getFollowerCount(){
	return this.followerCount;
}

public void setState(Integer state){
	this.state = state;
}

public Integer getState(){
	return this.state;
}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	@Override
public String toString (){
	return "userId:"+(userId == null ? "空" : userId)+"，email:"+(email == null ? "空" : email)+"，nickName:"+(nickName == null ? "空" : nickName)+"，password:"+(password == null ? "空" : password)+"，avatar:"+(avatar == null ? "空" : avatar)+"，mySignature:"+(mySignature == null ? "空" : mySignature)+"，myCoin:"+(myCoin == null ? "空" : myCoin)+"，followerCount:"+(followerCount == null ? "空" : followerCount)+"，state:"+(state == null ? "空" : state)+"，createTime:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，updateTime:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
}
}
