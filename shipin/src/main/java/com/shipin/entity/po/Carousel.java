package com.shipin.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.shipin.entity.enums.DateTimePatternEnum;
import com.shipin.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 轮播图表
 */
public class Carousel implements Serializable {


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

	/**
	 * 轮播图图片URL（七牛云存储）
	 */
	private String cover;

	/**
	 * 跳转链接
	 */
	private String link;

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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	private String categoryName;


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

	public void setCover(String cover){
		this.cover = cover;
	}

	public String getCover(){
		return this.cover;
	}

	public void setLink(String link){
		this.link = link;
	}

	public String getLink(){
		return this.link;
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

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	@Override
	public String toString (){
		return "轮播图ID:"+(carouselId == null ? "空" : carouselId)+"，分类ID:"+(categoryId == null ? "空" : categoryId)+"，轮播图标题:"+(title == null ? "空" : title)+"，轮播图图片URL（七牛云存储）:"+(cover == null ? "空" : cover)+"，跳转链接:"+(link == null ? "空" : link)+"，排序权重:"+(sortOrder == null ? "空" : sortOrder)+"，状态：1-启用，0-禁用:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
