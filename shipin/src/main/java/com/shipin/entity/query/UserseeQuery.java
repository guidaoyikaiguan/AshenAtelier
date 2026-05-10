package com.shipin.entity.query;



/**
 * 参数
 */
public class UserseeQuery extends BaseParam {


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

	private String iflikeFuzzy;

	/**
	 * 是否收藏
	 */
	private String ifcollect;

	private String ifcollectFuzzy;

	/**
	 * 投币
	 */
	private Integer insertcoins;

	private String insertcoinsFuzzy;


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

	public void setIflikeFuzzy(String iflikeFuzzy){
		this.iflikeFuzzy = iflikeFuzzy;
	}

	public String getIflikeFuzzy(){
		return this.iflikeFuzzy;
	}

	public void setIfcollect(String ifcollect){
		this.ifcollect = ifcollect;
	}

	public String getIfcollect(){
		return this.ifcollect;
	}

	public void setIfcollectFuzzy(String ifcollectFuzzy){
		this.ifcollectFuzzy = ifcollectFuzzy;
	}

	public String getIfcollectFuzzy(){
		return this.ifcollectFuzzy;
	}

	public void setInsertcoins(Integer insertcoins){
		this.insertcoins = insertcoins;
	}

	public Integer getInsertcoins(){
		return this.insertcoins;
	}

	public void setInsertcoinsFuzzy(String insertcoinsFuzzy){
		this.insertcoinsFuzzy = insertcoinsFuzzy;
	}

	public String getInsertcoinsFuzzy(){
		return this.insertcoinsFuzzy;
	}

}
