package com.dijiwise.semaphore;

import org.springframework.data.annotation.Id;

public class TwitterMediaEntities {
	public String getMediaUrl() {
		return mediaUrl;
	}
	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}
	public String getMediaType() {
		return mediaType;
	}
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	String mediaUrl;
	String mediaType;
	@Id
	String entityId;
	String StatusId;
	public String getStatusId() {
		return StatusId;
	}
	public void setStatusId(String statusId) {
		StatusId = statusId;
	}
	public String getEntityId() {
		return entityId;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

}
