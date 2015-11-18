package com.dijiwise.reader;

import org.springframework.data.annotation.Id;


//newcomment
public class FBcomments {


String uuidComment;
@Id
String commentId;
String commentMessage;
String commentFrom;
String profileImage;
String attachmentType;
String commentedById;
String socialMediaType;
//adding flags to comments
int readFlag;
int saveFlag;
int deleteFlag;
int  errorFlag;
int notificationFlag;
String errorMessage;
String parent_id;
public String getParent_id() {
	return parent_id;
}

public void setParent_id(String parent_id) {
	this.parent_id = parent_id;
}

public int getReadFlag() {
	return readFlag;
}

public void setReadFlag(int readFlag) {
	this.readFlag = readFlag;
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

public String getErrorMessage() {
	return errorMessage;
}

public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
}

//end of adding flags to comments


public String getSocialMediaType() {
	return socialMediaType;
}

public void setSocialMediaType(String socialMediaType) {
	this.socialMediaType = socialMediaType;
}

public String getChildUserId() {
	return childUserId;
}

public void setChildUserId(String childUserId) {
	this.childUserId = childUserId;
}

String childUserId;



public String getCommentedById() {
	return commentedById;
}

public void setCommentedById(String commentedById) {
	this.commentedById = commentedById;
}

String postId;
public String getPostId() {
	return postId;
}

public void setPostId(String postId) {
	this.postId = postId;
}

public String getAttachmentType() {
	return attachmentType;
}

public void setAttachmentType(String attachmentType) {
	this.attachmentType = attachmentType;
}

public String getAttachmentUrl() {
	return attachmentUrl;
}

public void setAttachmentUrl(String attachmentUrl) {
	this.attachmentUrl = attachmentUrl;
}

String attachmentUrl;
public String getProfileImage() {
	return profileImage;
}

public void setProfileImage(String profileImage) {
	this.profileImage = profileImage;
}

public String getVideo() {
	return video;
}

public void setVideo(String video) {
	this.video = video;
}

public String getImage() {
	return image;
}

public void setImage(String image) {
	this.image = image;
}

String video;
String image;
public String getCommentId() {
	return commentId;
}

public void setCommentId(String commentId) {
	this.commentId = commentId;
}

public String getCommentMessage() {
	return commentMessage;
}

public void setCommentMessage(String commentMessage) {
	this.commentMessage = commentMessage;
}

public String getCommentFrom() {
	return commentFrom;
}

public void setCommentFrom(String commentFrom) {
	this.commentFrom = commentFrom;
}



public String getUuidComment() {
	return uuidComment;
}

public void setUuidComment(String uuidComment) {
	this.uuidComment = uuidComment;
}




}
