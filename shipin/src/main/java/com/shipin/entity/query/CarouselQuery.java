package com.shipin.entity.query;

import java.util.Date;


/**
 * 轮播图表参数
 */
public class CarouselQuery extends BaseParam {


	/**
	 * 轮播图ID
	 */
	private Integer carouselId;

	/**
	 * 分类ID
	 */
	private Integer categoryId;

	/**
	 * 轮播图标题
	 */
	private String title;

	private String titleFuzzy;

	/**
	 * 轮播图图片URL（七牛云存储）
	 */
	private String cover;

	private String coverFuzzy;

	/**
	 * 跳转链接
	 */
	private String link;

	private String linkFuzzy;

	/**
	 * 排序权重
	 */
	private Integer sortOrder;

	/**
	 * 状态：1-启用，0-禁用
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


	public void setCarouselId(Integer carouselId){
		this.carouselId = carouselId;
	}

	public Integer getCarouselId(){
		return this.carouselId;
	}

	public void setCategoryId(Integer categoryId){
		this.categoryId = categoryId;
	}

	public Integer getCategoryId(){
		return this.categoryId;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public void setTitleFuzzy(String titleFuzzy){
		this.titleFuzzy = titleFuzzy;
	}

	public String getTitleFuzzy(){
		return this.titleFuzzy;
	}

	public void setCover(String cover){
		this.cover = cover;
	}

	public String getCover(){
		return this.cover;
	}

	public void setCoverFuzzy(String coverFuzzy){
		this.coverFuzzy = coverFuzzy;
	}

	public String getCoverFuzzy(){
		return this.coverFuzzy;
	}

	public void setLink(String link){
		this.link = link;
	}

	public String getLink(){
		return this.link;
	}

	public void setLinkFuzzy(String linkFuzzy){
		this.linkFuzzy = linkFuzzy;
	}

	public String getLinkFuzzy(){
		return this.linkFuzzy;
	}

	public void setSortOrder(Integer sortOrder){
		this.sortOrder = sortOrder;
	}

	public Integer getSortOrder(){
		return this.sortOrder;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
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

	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}

	public String getUpdateTime(){
		return this.updateTime;
	}

	public void setUpdateTimeStart(String updateTimeStart){
		this.updateTimeStart = updateTimeStart;
	}

	public String getUpdateTimeStart(){
		return this.updateTimeStart;
	}
	public void setUpdateTimeEnd(String updateTimeEnd){
		this.updateTimeEnd = updateTimeEnd;
	}

	public String getUpdateTimeEnd(){
		return this.updateTimeEnd;
	}

}
