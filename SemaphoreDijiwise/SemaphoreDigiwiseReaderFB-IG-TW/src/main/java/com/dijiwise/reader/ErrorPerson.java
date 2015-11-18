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
import com.restfb.types.Post.Comments;

public class ErrorPerson {

	// id will be used for storing MongoDB _id
	@Id
	String childId;
	 
	String insertDate;
	public String getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(String insertDate) {
		this.insertDate = insertDate;
	}

	public String getErrorOccuredDate() {
		return errorOccuredDate;
	}

	public void setErrorOccuredDate(String errorOccuredDate) {
		this.errorOccuredDate = errorOccuredDate;
	}

	String errorOccuredDate;
	
	

	String errorMessage;
	
	String socialSiteUserId;
	
	String socialMediaType;
	
	String errorCode;
	
	String errorFlag;
	
	String parent_id;
	
	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public String getErrorFlag() {
		return errorFlag;
	}

	public void setErrorFlag(String errorFlag) {
		this.errorFlag = errorFlag;
	}

	// String uuid;
	String uuid;
	
	public String getSocialMediaType() {
		return socialMediaType;
	}

	public void setSocialMediaType(String socialMediaType) {
		this.socialMediaType = socialMediaType;
	}

	

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getChildId() {
		return childId;
	}

	public void setChildId(String childId) {
		this.childId = childId;
	}

	

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getSocialSiteUserId() {
		return socialSiteUserId;
	}

	public void setSocialSiteUserId(String socialSiteUserId) {
		this.socialSiteUserId = socialSiteUserId;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	
	
	
	


}
