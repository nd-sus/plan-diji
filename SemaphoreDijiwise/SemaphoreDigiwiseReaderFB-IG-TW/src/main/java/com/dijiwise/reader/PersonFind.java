package com.dijiwise.reader;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.common.Videos;
import org.jinstagram.entity.users.feed.UserFeedData;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.data.annotation.Id;

import twitter4j.MediaEntity;
import twitter4j.Scopes;
import twitter4j.URLEntity;

import com.restfb.types.Comment;
import com.restfb.types.Post.Action;
import com.restfb.types.Post.Comments;

public class PersonFind {

	// id will be used for storing MongoDB _id

	// String uuid;
	String uuid;

	@Id
	String id;

	String name;
	String insertDate;
	


	HashMap flagMap;
	public HashMap getFlagMap() {
		return flagMap;
	}

	public void setFlagMap(HashMap flagMap) {
		this.flagMap = flagMap;
	}
	
	
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
	
	String createdDateApi;
	public String getCreatedDateApi() {
		return createdDateApi;
	}

	public void setCreatedDateApi(String createdDateApi) {
		this.createdDateApi = createdDateApi;
	}

	public long getCreatedDateTimestamp() {
		return createdDateTimestamp;
	}

	public void setCreatedDateTimestamp(long createdDateTimestamp) {
		this.createdDateTimestamp = createdDateTimestamp;
	}

	long createdDateTimestamp;
	
	// fields specfic to instagram
	String fullName;

	String caption;
	String profilePicUrl;
	Images images;
	Videos videos;
	String intragramCreatedDate;
	int myLiked;
	
	
	public int getMyLiked() {
		return myLiked;
	}

	public void setMyLiked(int myLiked) {
		this.myLiked = myLiked;
	}
	int readFlag;
	int saveFlag;
	String facebookSource;
	

	public String getFacebookSource() {
		return facebookSource;
	}

	public void setFacebookSource(String facebookSource) {
		this.facebookSource = facebookSource;
	}
List<Action>  facebookActionList;

	

	public List<Action> getFacebookActionList() {
		return facebookActionList;
	}

	public void setFacebookActionList(List<Action> facebookActionList) {
		this.facebookActionList = facebookActionList;
	}

	int totalCommentCount;
	public int getTotalCommentCount() {
		return totalCommentCount;
	}

	public void setTotalCommentCount(int totalCommentCount) {
		this.totalCommentCount = totalCommentCount;
	}
	int deleteFlag;
	int  errorFlag;
	String errorMessage;
	String fbComments;
	String twitterDiscriminator;
	
	public String getTwitterDiscriminator() {
		return twitterDiscriminator;
	}

	public void setTwitterDiscriminator(String twitterDiscriminator) {
		this.twitterDiscriminator = twitterDiscriminator;
	}

	String postSource;
	String username;
	String bio;
	String website;
	String instragramDiscriminator;
	String instagramStaticText;
	String postUserId;
	
	public String getPostUserId() {
		return postUserId;
	}

	public void setPostUserId(String postUserId) {
		this.postUserId = postUserId;
	}

	public String getInstagramStaticText() {
		return instagramStaticText;
	}

	public void setInstagramStaticText(String instagramStaticText) {
		this.instagramStaticText = instagramStaticText;
	}

	public String getInstragramDiscriminator() {
		return instragramDiscriminator;
	}

	public void setInstragramDiscriminator(String instragramDiscriminator) {
		this.instragramDiscriminator = instragramDiscriminator;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	
	/*List<Comment> fbComments;
	 
	
	public List<Comment> getFbComments() {
		return fbComments;
	}

	public void setFbComments(List<Comment> fbComments) {
		this.fbComments = fbComments;
	}*/
	
	public String getPostSource() {
		return postSource;
	}

	public void setPostSource(String postSource) {
		this.postSource = postSource;
	}

	//MediaEntity[] twitterMedias;
	//URLEntity[] twitterUrls;
	
	Scopes twitterScopes;
	Boolean twitterIsFavoritedByMe;
	Boolean twitterIsPossiblySensitive;
	Boolean twitterIsRetwettedByMe;
	
	public Scopes getTwitterScopes() {
		return twitterScopes;
	}

	public void setTwitterScopes(Scopes twitterScopes) {
		this.twitterScopes = twitterScopes;
	}

	
	public Boolean getTwitterIsRetwettedByMe() {
		return twitterIsRetwettedByMe;
	}

	public void setTwitterIsRetwettedByMe(Boolean twitterIsRetwettedByMe) {
		this.twitterIsRetwettedByMe = twitterIsRetwettedByMe;
	}

	

	public Boolean getTwitterIsFavoritedByMe() {
		return twitterIsFavoritedByMe;
	}

	public void setTwitterIsFavoritedByMe(Boolean twitterIsFavoritedByMe) {
		this.twitterIsFavoritedByMe = twitterIsFavoritedByMe;
	}

	public Boolean getTwitterIsPossiblySensitive() {
		return twitterIsPossiblySensitive;
	}

	public void setTwitterIsPossiblySensitive(Boolean twitterIsPossiblySensitive) {
		this.twitterIsPossiblySensitive = twitterIsPossiblySensitive;
	}

	

	
	
	
	List<UserFeedData> followedOrFollowing;
	

	public List<UserFeedData> getFollowedOrFollowing() {
		return followedOrFollowing;
	}

	public void setFollowedOrFollowing(List<UserFeedData> followedOrFollowing) {
		this.followedOrFollowing = followedOrFollowing;
	}

	/*public URLEntity[] getTwitterUrls() {
		return twitterUrls;
	}

	public void setTwitterUrls(URLEntity[] twitterUrls) {
		this.twitterUrls = twitterUrls;
	}

	public MediaEntity[] getTwitterMedias() {
		return twitterMedias;
	}

	public void setTwitterMedias(MediaEntity[] twitterMedias) {
		this.twitterMedias = twitterMedias;
	}*/

	String googleImageAttachment;
	String googleEmbedType;
	public String getGoogleEmbedType() {
		return googleEmbedType;
	}

	public void setGoogleEmbedType(String googleEmbedType) {
		this.googleEmbedType = googleEmbedType;
	}

	public String getGoogleImageAttachment() {
		return googleImageAttachment;
	}

	public void setGoogleImageAttachment(String googleImageAttachment) {
		this.googleImageAttachment = googleImageAttachment;
	}

	public String getGoogleVideoAttachment() {
		return googleVideoAttachment;
	}

	public void setGoogleVideoAttachment(String googleVideoAttachment) {
		this.googleVideoAttachment = googleVideoAttachment;
	}

	public String getGoogleAlbumAttachment() {
		return googleAlbumAttachment;
	}

	public void setGoogleAlbumAttachment(String googleAlbumAttachment) {
		this.googleAlbumAttachment = googleAlbumAttachment;
	}

	String googleVideoAttachment;
	String googleAlbumAttachment;
	
	public String getGoogleActivityUrl() {
		return googleActivityUrl;
	}

	public void setGoogleActivityUrl(String googleActivityUrl) {
		this.googleActivityUrl = googleActivityUrl;
	}

	public String getGoogleCrossPost() {
		return googleCrossPost;
	}

	public void setGoogleCrossPost(String googleCrossPost) {
		this.googleCrossPost = googleCrossPost;
	}

	public String getGoogleOriginalContent() {
		return googleOriginalContent;
	}

	public void setGoogleOriginalContent(String googleOriginalContent) {
		this.googleOriginalContent = googleOriginalContent;
	}

	public String getGoogleContent() {
		return googleContent;
	}

	public void setGoogleContent(String googleContent) {
		this.googleContent = googleContent;
	}

	String googleActivityUrl;
	String googleCrossPost;
	String googleOriginalContent;
	String googleContent;
	
	

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
	
	public int getTwitterUserFavouriteCount() {
		return twitterUserFavouriteCount;
	}

	public void setTwitterUserFavouriteCount(int twitterUserFavouriteCount) {
		this.twitterUserFavouriteCount = twitterUserFavouriteCount;
	}

	public int getTwitterUserFollowersCount() {
		return twitterUserFollowersCount;
	}

	public void setTwitterUserFollowersCount(int twitterUserFollowersCount) {
		this.twitterUserFollowersCount = twitterUserFollowersCount;
	}

	public int getTwitterUserFriendsCount() {
		return twitterUserFriendsCount;
	}

	public void setTwitterUserFriendsCount(int twitterUserFriendsCount) {
		this.twitterUserFriendsCount = twitterUserFriendsCount;
	}

	public Boolean getTwitterUserIsVerifiedCelebrity() {
		return twitterUserIsVerifiedCelebrity;
	}

	public void setTwitterUserIsVerifiedCelebrity(
			Boolean twitterUserIsVerifiedCelebrity) {
		this.twitterUserIsVerifiedCelebrity = twitterUserIsVerifiedCelebrity;
	}

	int twitterUserFavouriteCount;
	int twitterUserFollowersCount;
	int twitterUserFriendsCount;
	Boolean twitterUserIsVerifiedCelebrity;
	
	
	
	
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

	

	public PersonFind() {
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
//delete flags for filters
	int delete_users_posts;
	public int getDelete_users_posts() {
		return delete_users_posts;
	}

	public void setDelete_users_posts(int delete_users_posts) {
		this.delete_users_posts = delete_users_posts;
	}

	public int getDelete_users_events() {
		return delete_users_events;
	}

	public void setDelete_users_events(int delete_users_events) {
		this.delete_users_events = delete_users_events;
	}

	public int getDelete_users_likes() {
		return delete_users_likes;
	}

	public void setDelete_users_likes(int delete_users_likes) {
		this.delete_users_likes = delete_users_likes;
	}

	public int getDelete_users_comments() {
		return delete_users_comments;
	}

	public void setDelete_users_comments(int delete_users_comments) {
		this.delete_users_comments = delete_users_comments;
	}

	public int getDelete_instagram_posts() {
		return delete_instagram_posts;
	}

	public void setDelete_instagram_posts(int delete_instagram_posts) {
		this.delete_instagram_posts = delete_instagram_posts;
	}

	public int getDelete_activity_on_user() {
		return delete_activity_on_user;
	}

	public void setDelete_activity_on_user(int delete_activity_on_user) {
		this.delete_activity_on_user = delete_activity_on_user;
	}

	public int getDelete_instagram_comments() {
		return delete_instagram_comments;
	}

	public void setDelete_instagram_comments(int delete_instagram_comments) {
		this.delete_instagram_comments = delete_instagram_comments;
	}

	public int getDelete_instagram_user_likes() {
		return delete_instagram_user_likes;
	}

	public void setDelete_instagram_user_likes(int delete_instagram_user_likes) {
		this.delete_instagram_user_likes = delete_instagram_user_likes;
	}

	public int getDelete_user_tweets() {
		return delete_user_tweets;
	}

	public void setDelete_user_tweets(int delete_user_tweets) {
		this.delete_user_tweets = delete_user_tweets;
	}

	public int getDelete_users_favorites() {
		return delete_users_favorites;
	}

	public void setDelete_users_favorites(int delete_users_favorites) {
		this.delete_users_favorites = delete_users_favorites;
	}

	public int getDelete_new_followers() {
		return delete_new_followers;
	}

	public void setDelete_new_followers(int delete_new_followers) {
		this.delete_new_followers = delete_new_followers;
	}

	public int getDelete_new_followings() {
		return delete_new_followings;
	}

	public void setDelete_new_followings(int delete_new_followings) {
		this.delete_new_followings = delete_new_followings;
	}

	public int getRead_users_posts() {
		return read_users_posts;
	}

	public void setRead_users_posts(int read_users_posts) {
		this.read_users_posts = read_users_posts;
	}

	public int getRead_users_events() {
		return read_users_events;
	}

	public void setRead_users_events(int read_users_events) {
		this.read_users_events = read_users_events;
	}

	public int getRead_users_likes() {
		return read_users_likes;
	}

	public void setRead_users_likes(int read_users_likes) {
		this.read_users_likes = read_users_likes;
	}

	public int getRead_users_comments() {
		return read_users_comments;
	}

	public void setRead_users_comments(int read_users_comments) {
		this.read_users_comments = read_users_comments;
	}

	public int getRead_instagram_posts() {
		return read_instagram_posts;
	}

	public void setRead_instagram_posts(int read_instagram_posts) {
		this.read_instagram_posts = read_instagram_posts;
	}

	public int getRead_activity_on_user() {
		return read_activity_on_user;
	}

	public void setRead_activity_on_user(int read_activity_on_user) {
		this.read_activity_on_user = read_activity_on_user;
	}

	public int getRead_instagram_comments() {
		return read_instagram_comments;
	}

	public void setRead_instagram_comments(int read_instagram_comments) {
		this.read_instagram_comments = read_instagram_comments;
	}

	public int getRead_instagram_user_likes() {
		return read_instagram_user_likes;
	}

	public void setRead_instagram_user_likes(int read_instagram_user_likes) {
		this.read_instagram_user_likes = read_instagram_user_likes;
	}

	public int getRead_user_tweets() {
		return read_user_tweets;
	}

	public void setRead_user_tweets(int read_user_tweets) {
		this.read_user_tweets = read_user_tweets;
	}

	public int getRead_users_favorites() {
		return read_users_favorites;
	}

	public void setRead_users_favorites(int read_users_favorites) {
		this.read_users_favorites = read_users_favorites;
	}

	public int getRead_new_followers() {
		return read_new_followers;
	}

	public void setRead_new_followers(int read_new_followers) {
		this.read_new_followers = read_new_followers;
	}

	public int getRead_new_followings() {
		return read_new_followings;
	}

	public void setRead_new_followings(int read_new_followings) {
		this.read_new_followings = read_new_followings;
	}


	int delete_users_events;
	int delete_users_likes;
	int delete_users_comments;
	int delete_instagram_posts;
	int delete_activity_on_user;
	int delete_instagram_comments;
	int delete_instagram_user_likes;
	int delete_user_tweets;
	int delete_users_favorites;
	int delete_new_followers;
	int delete_new_followings;
	
	//read flags for filters
	int read_users_posts;
	int read_users_events;
	int read_users_likes;
	int read_users_comments;
	int read_instagram_posts;
	int read_activity_on_user;
	int read_instagram_comments;
	int read_instagram_user_likes;
	int read_user_tweets;
	int read_users_favorites;
	int read_new_followers;
	int read_new_followings;
	
	


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
				+ this.photoUrl + " videourl: " + this.videoUrl +
				" readFlag: " + this.readFlag + " tweets: " + this.read_user_tweets
				+ " likecount: " + this.likeCount + " photourl: "
				+ this.photoUrl + " videourl: " + this.videoUrl ;
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
	/*Person( String id, int saveFlag, int readFlag, int deleteFlag, int notificationFlag) {
		
		this.id = id;
		this.saveFlag=saveFlag;
		this.readFlag=readFlag;
		this.deleteFlag=deleteFlag;
		this.notificationFlag=notificationFlag;
		
		
	}*/

}
