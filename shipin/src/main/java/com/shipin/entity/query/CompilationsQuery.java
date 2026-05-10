package com.shipin.entity.query;



/**
 * 参数
 */
public class CompilationsQuery extends BaseParam {


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

	private String compilationsNameFuzzy;


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

	public void setCompilationsNameFuzzy(String compilationsNameFuzzy){
		this.compilationsNameFuzzy = compilationsNameFuzzy;
	}

	public String getCompilationsNameFuzzy(){
		return this.compilationsNameFuzzy;
	}

}
