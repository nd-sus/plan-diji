package com.dijiwise.semaphore;



import org.springframework.data.annotation.Id;



public class FBcomments {


String uuidComment;
@Id
String commentId;
String commentMessage;
String commentFrom;
String profileImage;
String attachmentType;
String commentedById;



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



