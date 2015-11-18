package com.digiwise.reader;

import java.util.Date;
import java.util.List;

import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.common.Videos;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.data.annotation.Id;

import com.restfb.types.Comment;
import com.restfb.types.Post.Comments;

public class Person05Jan {

	// id will be used for storing MongoDB _id

	// String uuid;
	String uuid;

	@Id
	String id;

	String name;
	String insertDate;
	

	

	String message;
	long createdDate;
	int notificationFlag;
	
	String postType;
	String createdBy;
	Long commentCount;
	Long likeCount;
	String photoUrl;
	String link;
	String videoUrl;
	String socialMediaType;
	String deviceType;
	String deviceToken;
	String registerIdsocial_site_user_id;
	String apiKey;
	String child_user_id;
	String description;
	String story;
	String icon;
	String picture;
	String fromId;
	String attribution;
	String postName;
	Long updatedDate;
	Long updatedDateTime;
	// fields specfic to instagram
	String fullName;

	String caption;
	String profilePicUrl;
	Images images;
	Videos videos;
	String intragramCreatedDate;
	
	
	int readFlag;
	int saveFlag;
	

	int deleteFlag;
	int  errorFlag;
	String errorMessage;
	String fbComments;
	/*List<Comment> fbComments;
	
	public List<Comment> getFbComments() {
		return fbComments;
	}

	public void setFbComments(List<Comment> fbComments) {
		this.fbComments = fbComments;
	}*/


	public String getFbComments() {
		return fbComments;
	}

	public void setFbComments(String fbComments) {
		this.fbComments = fbComments;
	}

	public String getInsertDate() {
		return insertDate;
	}

	public int getErrorFlag() {
		return errorFlag;
	}

	public void setErrorFlag(int errorFlag) {
		this.errorFlag = errorFlag;
	}

	public int getNotificationFlag() {
		return notificationFlag;
	}

	public void setNotificationFlag(int notificationFlag) {
		this.notificationFlag = notificationFlag;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	
	/*String updatedDate;
	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}
	

	public String getUpdatedDate() {
		return updatedDate;
	}*/
	
	
	
	public Long getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Long updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Long getUpdateDateTime() {
		return updatedDateTime;
	}

	public void setUpdateDateTime(Long updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}

	DateTime updateDateTime;

	public void setInsertDate(String insertDate) {
		this.insertDate = insertDate;
	}

	String socialSiteUserId;
	String parentId;
	public String getSocialSiteUserId() {
		return socialSiteUserId;
	}

	public void setSocialSiteUserId(String socialSiteUserId) {
		this.socialSiteUserId = socialSiteUserId;
	}
	
	
	
	
	
	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}



	// Fields specific to srikanth



	

	

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	public int getReadFlag() {
		return readFlag;
	}

	public void setReadFlag(int readFlag) {
		this.readFlag = readFlag;
	}



	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPostName() {
		return postName;
	}

	public void setPostName(String postName) {
		this.postName = postName;
	}

	

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getProfilePicUrl() {
		return profilePicUrl;
	}

	public void setProfilePicUrl(String profilePicUrl) {
		this.profilePicUrl = profilePicUrl;
	}

	public Images getImages() {
		return images;
	}

	public void setImages(Images images) {
		this.images = images;
	}

	public Videos getVideos() {
		return videos;
	}

	public void setVideos(Videos videos) {
		this.videos = videos;
	}

	public String getIntragramCreatedDate() {
		return intragramCreatedDate;
	}

	public void setIntragramCreatedDate(String intragramCreatedDate) {
		this.intragramCreatedDate = intragramCreatedDate;
	}



	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getSocialMediaType() {
		return socialMediaType;
	}

	public void setSocialMediaType(String socialMediaType) {
		this.socialMediaType = socialMediaType;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStory() {
		return story;
	}

	public void setStory(String story) {
		this.story = story;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getAttribution() {
		return attribution;
	}

	public void setAttribution(String attribution) {
		this.attribution = attribution;
	}
	public int getSaveFlag() {
		return saveFlag;
	}

	public void setSaveFlag(int saveFlag) {
		this.saveFlag = saveFlag;
	}

	public int getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(int deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	

	public Person05Jan() {
	}

/*	public Person(String uuid, String i, String n, String insertDate,
			String message, String createdDate, String postType,
			String createdBy, Long commentCount, Long likeCount,
			String photoUrl, String videoUrl, String socialMediaType,
			String deviceType, String deviceToken, String registerId,
			String apiKey, String child_user_id, Date updatedDate,
			String postCaption, String postDescription, String postStory,
			String postIcon, String postPicture, String postFromId,
			String postAttribution, String postName) {
		this.uuid = uuid;
		this.id = i;
		this.name = n;
		this.insertDate = insertDate;
		this.message = message;
		this.createdDate = createdDate;
		this.postType = postType;
		this.createdBy = createdBy;
		this.commentCount = commentCount;
		this.likeCount = likeCount;
		this.photoUrl = photoUrl;
		this.videoUrl = videoUrl;
		this.socialMediaType = socialMediaType;
		this.deviceType = deviceType;
		this.deviceToken = deviceToken;
		this.registerId = registerId;
		this.apiKey = apiKey;
		this.child_user_id = child_user_id;
		this.updatedDate = updatedDate;
		this.postCaption = postCaption;
		this.postDescription = postDescription;
		this.postStory = postStory;
		this.postIcon = postIcon;
		this.postPicture = postPicture;
		this.postFromId = postFromId;
		this.postAttribution = postAttribution;
		this.postName = postName;

	}
*/
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	

	public String getPostType() {
		return this.postType;
	}

	public void setPostType(String postType) {
		this.postType = postType;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getPhotoUrl() {
		return this.photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getVideoUrl() {
		return this.videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public Long getCommentCount() {
		return this.commentCount;
	}

	public void setCommentCount(Long commentCount) {
		this.commentCount = commentCount;
	}

	public Long getLikeCount() {
		return this.likeCount;
	}

	public void setLikeCount(Long likeCount) {
		this.likeCount = likeCount;
	}

	/*
	 * public String getUuid() { return this.uuid; }
	 * 
	 * public void setUuid(String uuid) { this.uuid = uuid; }
	 */

	public String getRegisterId() {
		return registerIdsocial_site_user_id;
	}

	public void setRegisterId(String registerId) {
		this.registerIdsocial_site_user_id = registerId;
	}

	public String getChild_user_id() {
		return child_user_id;
	}

	public void setChild_user_id(String child_user_id) {
		this.child_user_id = child_user_id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}



	/*
	 * String uuid, String i, String n, Date insertDate, String message, Date
	 * createdDate, String postType, String createdBy, Long commentCount, Long
	 * likeCount, String photoUrl, String videoUrl, String socialMediaType,
	 * String deviceType, String deviceToken, String registerId, String apiKey
	 */
	@Override
	public String toString() {
		return " id:" + this.id + "name: " + this.name + " message:"
				+ this.message + " createddate: " + this.createdDate
				+ " posttype: " + this.postType + " createdby:"
				+ this.createdBy + " commentcount: " + this.commentCount
				+ " likecount: " + this.likeCount + " photourl: "
				+ this.photoUrl + " videourl: " + this.videoUrl;
	}

	public String toString1() {
		return " id:" + this.id + "name: " + this.name + " message:"
				+ this.message + " createddate: " + this.createdDate
				+ " posttype: " + this.postType + " createdby:"
				+ this.createdBy + " commentcount: " + this.commentCount
				+ " likecount: " + this.likeCount + " photourl: "
				+ this.photoUrl + " videourl: " + this.videoUrl
				+ " socialmediatype: " + this.socialMediaType
				+ " devicedtype: " + this.deviceType + " devicetoken:"
				+ this.deviceToken + " registeredid" + this.registerIdsocial_site_user_id
				+ " apikey: " + this.apiKey + " child_user_id:"
				+ this.child_user_id;
	}

	// instagram constructor
	/*Person(String uuid, String id, String fullName, String insertDate,
			String caption, String createdDate, String profilePicUrl,
			int instagramlikeCount, int instagramCommentCount, Images images,
			Videos videos, String socialMediaType, String deviceType,
			String deviceToken, String registerId, String apiKey,
			String child_user_id) {
		this.uuid = uuid;
		this.id = id;
		this.name = fullName;
		this.insertDate = insertDate;
		this.caption = caption;
		this.createdDate = createdDate;
		// this.intragramCreatedDate = intragramCreatedDate;
		this.profilePicUrl = profilePicUrl;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
		this.images = images;
		this.videos = videos;
		this.socialMediaType = socialMediaType;
		this.deviceType = deviceType;
		this.deviceToken = deviceToken;
		this.registerId = registerId;
		this.apiKey = apiKey;
		this.child_user_id = child_user_id;

	}
*/
}
