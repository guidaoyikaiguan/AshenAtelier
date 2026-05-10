package com.shipin.entity.query;



/**
 * 参数
 */
public class AdminQuery extends BaseParam {


	/**
	 * 
	 */
	private Integer adminId;

	/**
	 * 
	 */
	private String adminName;

	private String adminNameFuzzy;

	/**
	 * 
	 */
	private String password;

	private String passwordFuzzy;


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

	public void setAdminNameFuzzy(String adminNameFuzzy){
		this.adminNameFuzzy = adminNameFuzzy;
	}

	public String getAdminNameFuzzy(){
		return this.adminNameFuzzy;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setPasswordFuzzy(String passwordFuzzy){
		this.passwordFuzzy = passwordFuzzy;
	}

	public String getPasswordFuzzy(){
		return this.passwordFuzzy;
	}

}
