package com.shipin.entity.query;

import java.util.Date;


/**
 * 参数
 */
public class MomentImageQuery extends BaseParam {


	/**
	 * 
	 */
	private Integer imageId;

	/**
	 * 
	 */
	private Integer momentId;

	/**
	 * 
	 */
	private String imageUrl;

	private String imageUrlFuzzy;

	/**
	 * 
	 */
	private Integer sortOrder;

	/**
	 * 
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setImageId(Integer imageId){
		this.imageId = imageId;
	}

	public Integer getImageId(){
		return this.imageId;
	}

	public void setMomentId(Integer momentId){
		this.momentId = momentId;
	}

	public Integer getMomentId(){
		return this.momentId;
	}

	public void setImageUrl(String imageUrl){
		this.imageUrl = imageUrl;
	}

	public String getImageUrl(){
		return this.imageUrl;
	}

	public void setImageUrlFuzzy(String imageUrlFuzzy){
		this.imageUrlFuzzy = imageUrlFuzzy;
	}

	public String getImageUrlFuzzy(){
		return this.imageUrlFuzzy;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

}
