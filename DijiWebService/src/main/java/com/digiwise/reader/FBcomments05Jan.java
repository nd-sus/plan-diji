package com.digiwise.reader;

import org.springframework.data.annotation.Id;

import com.restfb.types.Comment;
import com.restfb.types.Comment.Comments;

public class FBcomments05Jan {

@Id
String uuidComment;

public String getUuidComment() {
	return uuidComment;
}

public void setUuidComment(String uuidComment) {
	this.uuidComment = uuidComment;
}

com.restfb.types.Comment  comment;

public Comment getComment() {
	return comment;
}

public void setComment(Comment c) {
	this.comment = c;
}
}
