package com.dijiwise.semaphore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.common.Videos;
import org.jinstagram.entity.users.feed.UserFeedData;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.data.annotation.Id;

import twitter4j.DirectMessage;
import twitter4j.MediaEntity;
import twitter4j.ResponseList;
import twitter4j.URLEntity;
import twitter4j.User;

import com.restfb.types.Comment;
import com.restfb.types.Post.Comments;

public class PersonFollowersFollowing {

	// id will be used for storing MongoDB _id

	// String uuid;
	String uuid;

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<User> getTwitterFollowing() {
		return twitterFollowing;
	}
	public void setTwitterFollowing(List<User> twitterFollowing) {
		this.twitterFollowing = twitterFollowing;
	}
	@Id
	String id;
	List<User> twitterFollowing;
	List<UserFeedData> instafollowfollowing;
	
	String socialSiteUserId;
	String readFlag;
	String childUserId;
	public String getChildUserId() {
		return childUserId;
	}
	public void setChildUserId(String childUserId) {
		this.childUserId = childUserId;
	}
	public String getReadFlag() {
		return readFlag;
	}
	public void setReadFlag(String readFlag) {
		this.readFlag = readFlag;
	}
	public String getSaveFlag() {
		return saveFlag;
	}
	public void setSaveFlag(String saveFlag) {
		this.saveFlag = saveFlag;
	}

	String saveFlag;
	ResponseList<DirectMessage> directMessages;

	public ResponseList<DirectMessage> getDirectMessages() {
		return directMessages;
	}
	public void setDirectMessages(ResponseList<DirectMessage> directMessages) {
		this.directMessages = directMessages;
	}
	public String getSocialSiteUserId() {
		return socialSiteUserId;
	}
	public void setSocialSiteUserId(String socialSiteUserId) {
		this.socialSiteUserId = socialSiteUserId;
	}
	public List<UserFeedData> getInstafollowfollowing() {
		return instafollowfollowing;
	}
	public void setInstafollowfollowing(List<UserFeedData> instafollowfollowing) {
		this.instafollowfollowing = instafollowfollowing;
	}

}

