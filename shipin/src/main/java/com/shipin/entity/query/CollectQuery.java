package com.shipin.entity.query;



/**
 * 参数
 */
public class CollectQuery extends BaseParam {


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
	private Integer favorite;

	private String favoriteFuzzy;

	/**
	 * 
	 */
	private String favoriteName;

	private String favoriteNameFuzzy;

	/**
	 * 
	 */
	private Integer isPublic;


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

	public void setFavorite(Integer favorite){
		this.favorite = favorite;
	}

	public Integer getFavorite(){
		return this.favorite;
	}

	public void setFavoriteFuzzy(String favoriteFuzzy){
		this.favoriteFuzzy = favoriteFuzzy;
	}

	public String getFavoriteFuzzy(){
		return this.favoriteFuzzy;
	}

	public void setFavoriteName(String favoriteName){
		this.favoriteName = favoriteName;
	}

	public String getFavoriteName(){
		return this.favoriteName;
	}

	public void setFavoriteNameFuzzy(String favoriteNameFuzzy){
		this.favoriteNameFuzzy = favoriteNameFuzzy;
	}

	public String getFavoriteNameFuzzy(){
		return this.favoriteNameFuzzy;
	}

	public void setIsPublic(Integer isPublic){
		this.isPublic = isPublic;
	}

	public Integer getIsPublic(){
		return this.isPublic;
	}

}
