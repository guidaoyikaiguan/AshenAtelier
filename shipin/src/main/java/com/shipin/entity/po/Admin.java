package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 
 */
public class Admin implements Serializable {


	/**
	 * 
	 */
	private Integer adminId;

	/**
	 * 
	 */
	private String adminName;

	/**
	 * 
	 */
	private String password;


	public void setAdminId(Integer adminId){
		this.adminId = adminId;
	}

	public Integer getAdminId(){
		return this.adminId;
	}

	public void setAdminName(String adminName){
		this.adminName = adminName;
	}

	public String getAdminName(){
		return this.adminName;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	@Override
	public String toString (){
		return "adminId:"+(adminId == null ? "空" : adminId)+"，adminName:"+(adminName == null ? "空" : adminName)+"，password:"+(password == null ? "空" : password);
	}
}
