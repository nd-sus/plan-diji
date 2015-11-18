package com.dijiwise.semaphore;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.dijiwise.reader.ErrorPerson;
import com.dijiwise.reader.FBcomments;
import com.dijiwise.reader.Person;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.rabbitmq.client.Channel;
import com.restfb.types.Comment;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Post;
import com.restfb.types.Post.Likes;

public class FacebookWorker {
	// facebook handler
	final static Logger logger = Logger.getLogger(FacebookWorker.class);

	public static void processData(final byte[] body, final String routingKey, final com.rabbitmq.client.Connection connection1,
			final Properties prop, final Long counterValue, final MongoClient mongo1) {
		/* Thread to access from facebook data using restfb. */
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				logger.setLevel(Level.INFO);

				try {
					System.setProperty("DEBUG.MONGO", "false");
					// SocialNetworkService.getInstance().getThreaPoolLock();
					PersonLock.getInstance().getThreaPoolLock();

					System.out.println("facebook thread");

					logger.info("\n Facebook Thread start at :" + (System.currentTimeMillis()));
					String accessTokenValue = null;
					MongoOperations mongoOps = null;
					ArrayList<Post> data = null;
					ArrayList<Post> dataFeed = null;
					String PERSON_LOGIN_STATUS = "";
					String PERSON_FOLLOWING_FOLLOWS = "";
					String PERSON_COLLECTION = "";
					String FB_COMMENT_COLLECTION = "";
					String PERSON_COLLECTION_TEMP = "";
					String FB_COMMENT_COLLECTION_TEMP = "";

					MongoClient mongo = null;
					DateTimeZone zone = DateTimeZone.getDefault();

					Channel writechannel = null;
					ErrorPerson errorPerson = null;
					Person p2 = null;
					JSONObject obj = null;
					Person p = null;
					ErrorPerson p1 = null;
					String child_user_id = null;
					String parent_id = null;

					try {

//						PropertyConfigurator.configure("/dijiwise/log4j.properties");
						PropertyConfigurator.configure(SocialNetworkService.LOG4J_PROP);

						final String RQUEUE_NAME = prop.getProperty("RQUEUE_NAME");
						final int PORT = Integer.parseInt(prop.getProperty("PORT"));
						final String HOST = prop.getProperty("HOST");
						final String DB_NAME = prop.getProperty("DB_NAME");
						PERSON_COLLECTION = prop.getProperty("PERSON_COLLECTION");
						PERSON_LOGIN_STATUS = prop.getProperty("PERSON_LOGIN_STATUS");
						FB_COMMENT_COLLECTION = prop.getProperty("FB_COMMENT_COLLECTION");
						PERSON_COLLECTION_TEMP = prop.getProperty("PERSON_COLLECTION_TEMP");
						FB_COMMENT_COLLECTION_TEMP = prop.getProperty("FB_COMMENT_COLLECTION_TEMP");

						final String MONGO_HOST = prop.getProperty("MONGO_HOST");
						final int MONGO_PORT = Integer.parseInt(prop.getProperty("MONGO_PORT"));
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("x-cancel-on-ha-failover", true);

						String message = new String(body);
						JSONParser jsonParser = new JSONParser();
						JSONObject jsonObject = (JSONObject) jsonParser.parse(message);

						accessTokenValue = ((String) jsonObject.get("access_token") != null ? (String) jsonObject
								.get("access_token") : "");

						child_user_id = (String) jsonObject.get("child_user_id");
						parent_id = (String) jsonObject.get("parent_id");
						logger.info("\nSTFacebook Listener start of the  Thread: at :" + (System.currentTimeMillis())
								+ " childuserId: " + child_user_id + " of parent is " + parent_id);
						try {

							mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
							MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(100).build();

							mongoOps = new MongoTemplate(mongo, DB_NAME);

							GraphReaderExample gRE = new GraphReaderExample(accessTokenValue);

							dataFeed = gRE.FilteredFeedPosts();
							// dataFeed = gRE.fetchConnectionsJson1();
							data = gRE.FilteredPosts();

							System.out.println("Successfully created connection of MongoDB!!!!!!");
						} catch (Exception e) {

							e.printStackTrace();
							
							p1 = new ErrorPerson();
							errorPerson = new ErrorPerson();
							// mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
							mongoOps = new MongoTemplate(mongo, DB_NAME);
							logger.info("\nSTFACEBOOK Listener Error at :" + (System.currentTimeMillis()) + " childuserId: "
									+ child_user_id + " of parent" + parent_id);

							errorPerson.setChildId(child_user_id);
							errorPerson.setParent_id(parent_id);
							errorPerson.setErrorOccuredDate(new Date().toString());
							errorPerson.setErrorMessage(e.getMessage());
							errorPerson.setUuid((UUID.randomUUID() + ""));

							errorPerson.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));
							errorPerson.setErrorMessage(e.getMessage());

							errorPerson.setSocialMediaType((String) jsonObject.get("social_media_type"));

							errorPerson.setErrorFlag("1");

							mongoOps.save(errorPerson, PERSON_LOGIN_STATUS);

						} finally {

						}
						try {
							if (errorPerson == null) {

							
								mongoOps = new MongoTemplate(mongo, DB_NAME);
//Creating
								for (Post i : data) {

									try {
										p = new Person();
										p.setUuid((UUID.randomUUID() + ""));
										p.setId(i.getId() + "_" + child_user_id + "_" + parent_id);
										p.setName(i.getName());

										p.setInsertDate((new DateTime().toDateTimeISO().toString()));

										p.setMessage(i.getMessage());

										p.setCommentCount(0L);
										try {
											// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
											if (i.getComments() != null & i.getComments().getData() != null
													& i.getComments().getData().size() > 0) {
												String uuidCommentId = UUID.randomUUID() + "";
												long j = 0L;
												for (Comment c : i.getComments().getData()) {
													try {
														FBcomments comment = new FBcomments();
														comment.setUuidComment(uuidCommentId);

														comment.setPostId(p.getId());
														comment.setCommentId((c.getId() != null ? (c.getId() + "_" + p.getId())
																: ""));
														comment.setCommentMessage((c.getMessage() != null ? c.getMessage() : ""));
														comment.setCommentFrom((c.getFrom() != null ? c.getFrom().getName() != null ? c
																.getFrom().getName() : ""
																: ""));
														comment.setProfileImage("");
														comment.setCommentedById((c.getFrom().getId() != null ? c.getFrom()
																.getId() : ""));
														comment.setAttachmentType((c.getAttachment() != null ? c.getAttachment()
																.getType() : ""));
														comment.setAttachmentUrl((c.getAttachment() != null ? c.getAttachment()
																.getUrl() : ""));
														comment.setSocialMediaType((String) jsonObject.get("social_media_type"));
														comment.setChildUserId((String) jsonObject.get("child_user_id"));
														comment.setParent_id(parent_id);

														// adding flags to
														// Comments

														comment.setReadFlag(0);
														comment.setSaveFlag(0);
														comment.setDeleteFlag(0);
														comment.setErrorFlag(0);
														comment.setErrorMessage("");
														comment.setNotificationFlag(0);

														FBcomments dbcomment = new FBcomments();
														try {

															// if
															// (mongoOps.findById(i.getId()+"_"+child_user_id,
															// Person.class,
															// PERSON_COLLECTION)
															// != null) {
															// p2 =
															// mongoOps.findById(new
															// Long(i.getId()).toString()+"_"+child_user_id,
															// Person.class,
															// PERSON_COLLECTION);
															if (mongoOps.findById(comment.getCommentId(), FBcomments.class,
																	FB_COMMENT_COLLECTION) != null) {
																dbcomment = mongoOps.findById(comment.getCommentId(),
																		FBcomments.class, FB_COMMENT_COLLECTION);
																comment.setReadFlag(dbcomment.getReadFlag());
																comment.setSaveFlag(dbcomment.getSaveFlag());
																comment.setDeleteFlag(dbcomment.getDeleteFlag());
																comment.setErrorFlag(dbcomment.getErrorFlag());
																comment.setErrorMessage("");
																comment.setNotificationFlag(dbcomment.getNotificationFlag());
															}
														} catch (Exception e) {
															logger.error("fb comment exception.."

															+ e.toString());
														}
														// end of adding flags
														// to comment

														// mongoOps.save(comment,
														// FB_COMMENT_COLLECTION);
														mongoOps.save(comment, FB_COMMENT_COLLECTION_TEMP);
													} catch (Exception e) {
														// TODO Auto-generated
														// catch block
														logger.error("fb comment  flag  exception..");
													}
												}
												Query query5 = new Query();
												query5.addCriteria(Criteria.where("postId").is(p.getId()));
												j = (mongoOps.count(query5, FB_COMMENT_COLLECTION_TEMP));
												p.setCommentCount(j);
												p.setFbComments(uuidCommentId);
											}
										} catch (Exception e1) {
											logger.info("fb Comments list exception..." + e1.toString());
											// TODO Auto-generated catch block

										}

										Date local = i.getCreatedTime();
										long utc = zone.convertLocalToUTC(local.getTime(), false);
										p.setCreatedDate(utc);
										p.setCreatedDateApi(i.getCreatedTime().toString());
										p.setCreatedDateTimestamp(local.getTime());

										p.setPostType((i.getType() != null ? i.getType() : ""));

										p.setName((i.getName() != null ? i.getName() : ""));
										try {
											if (i.getType() == "video") {
												i.getObjectId();
											}
										} catch (Exception e2) {

										}
										try {
											p.setFacebookSource((i.getSource() != null ? i.getSource() : ""));
										} catch (Exception e1) {
											// TODO Auto-generated catch block

										}
										try {
											p.setFacebookActionList((i.getActions() != null ? i.getActions() : null));
										} catch (Exception e1) {
											// TODO Auto-generated catch block

										}
										p.setMyLiked(0);
										try {
											if (i.getLikes() != null && i.getLikes() instanceof Likes
													|| i.getLikes().getData() != null) {
												Long likeCountFromLikes = i.getLikes().getTotalCount();

												List<NamedFacebookType> likes = (i.getLikes().getData() != null ? i.getLikes()
														.getData() : null);

												if (likes != null) {
													for (NamedFacebookType e : likes) {
														String personLiked = e.getId();
														personLiked.equalsIgnoreCase((String) jsonObject
																.get("social_site_user_id"));
														p.setMyLiked(1);
													}
													long dataCount = likes.size();
													p.setLikeCount(likeCountFromLikes >= dataCount ? likeCountFromLikes
															: dataCount);

												}
											} else {
												p.setLikeCount(0L);
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											logger.error("Like count not exception...." + e.toString());

										}

										p.setPhotoUrl((i.getPicture()) != null ? (i.getPicture()) : "");

										p.setLink((i.getLink() != null ? i.getLink() : ""));

										p.setSocialMediaType((String) jsonObject.get("social_media_type"));
										p.setPostSource("home");

										p.setChild_user_id((String) jsonObject.get("child_user_id"));
										p.setReadFlag(0);
										p.setSaveFlag(0);
										p.setDeleteFlag(0);
										p.setErrorFlag(0);
										p.setErrorMessage("");
										p.setNotificationFlag(0);
										// read and delete flags for filters
										p.setDelete_users_posts(0);
										p.setDelete_users_likes(0);
										p.setDelete_users_events(0);
										p.setDelete_users_comments(0);
										p.setRead_users_posts(0);
										p.setRead_users_likes(0);
										p.setRead_users_events(0);
										p.setRead_users_comments(0);

										p2 = new Person();
										try {
											if (mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION) != null) {
												p2 = mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION);
												p.setReadFlag(p2.getReadFlag());
												p.setSaveFlag(p2.getSaveFlag());
												p.setDeleteFlag(p2.getDeleteFlag());
												p.setErrorFlag(p2.getErrorFlag());
												p.setErrorMessage("");
												p.setNotificationFlag(p2.getNotificationFlag());

												p.setDelete_users_posts(p2.getDelete_users_posts());
												p.setDelete_users_likes(p2.getDelete_users_likes());
												p.setDelete_users_events(p2.getDelete_users_events());
												p.setDelete_users_comments(p2.getDelete_users_comments());
												p.setRead_users_posts(p2.getRead_users_posts());
												p.setRead_users_likes(p2.getRead_users_likes());
												p.setRead_users_events(p2.getRead_users_events());
												p.setRead_users_comments(p2.getRead_users_comments());

											}
										} catch (Exception e) {
											logger.info("facebook flag modification exception.." + e.toString());
										}

										DateTime retriveDate = new DateTime(i.getUpdatedTime());
										DateTime sendingDate = new DateTime(retriveDate.getYear(), retriveDate.getMonthOfYear(),
												retriveDate.getDayOfMonth(), 0, 0, 0);
										p.setUpdatedDate(sendingDate.getMillis());
										p.setUpdateDateTime(retriveDate.getMillis());

										p.setCaption((i.getCaption() != null ? i.getCaption() : ""));

										p.setDescription((i.getDescription() != null ? i.getDescription() : ""));

										p.setStory((i.getStory() != null ? i.getStory() : ""));
										p.setIcon((i.getIcon() != null ? i.getIcon() : ""));

										p.setPicture((i.getPicture() != null ? i.getPicture() : ""));

										p.setFromId((i.getFrom().getId() != null ? i.getFrom().getId() : ""));

										p.setAttribution((i.getAttribution() != null ? i.getAttribution() : ""));

										p.setParentId((String) jsonObject.get("parent_id"));
										p.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));

										try {

											if (writechannel != null && writechannel.isOpen()) {

												writechannel.close();
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block

											logger.info("Exception in closing the channel. " + e.toString());
										}
										// writechannel =
										// connection1.createChannel();
										// writechannel.queueDeclare(RQUEUE_NAME,
										// false, false, false, args);
										/***
										 * p1 = new Person();
										 * 
										 * if (mongoOps.findById(i.getId(),
										 * Person.class, PERSON_COLLECTION) !=
										 * null) { p1 =
										 * mongoOps.findById(i.getId(),
										 * Person.class, PERSON_COLLECTION); }
										 * if (p1.getUuid() != null &&
										 * p1.toString().length() > 0) {
										 * logger.info("Check Same Post data : "
										 * + (p1.toString()).equals(p
										 * .toString()) + "@@" + i.getId()); if
										 * (!(p1.toString()).equals(p.toString()
										 * )) {
										 * 
										 * logger.info(
										 * "Update the post with Data: " +
										 * p.toString() + " for social type:" +
										 * p.getSocialMediaType());
										 * 
										 * mongoOps.save(p, PERSON_COLLECTION);
										 * logger.info(
										 * "After save update fb post timeStamp "
										 * + (System.currentTimeMillis())); obj
										 * = new JSONObject(); obj.put("uuid",
										 * p.getUuid());
										 * 
										 * obj.put("apikey", p.getApiKey());
										 * obj.put("deviceToken",
										 * p.getDeviceToken());
										 * obj.put("deviceType",
										 * p.getDeviceType());
										 * obj.put("registeredId",
										 * p.getRegisterId());
										 * 
										 * obj.put("socialmediaType",
										 * p.getSocialMediaType());
										 * obj.put("child_user_id",
										 * p.getChild_user_id());
										 * obj.put("parent_id",
										 * p.getParentId());
										 * 
										 * logger.info(
										 * "Json object that will be sent to response queue for id:    "
										 * + p.getId() + " is" +
										 * obj.toJSONString());
										 * 
										 * writechannel.basicPublish("",
										 * RQUEUE_NAME, null, obj
										 * .toString().getBytes()); } } else {
										 */
										//
										/***
										 * logger.info("New post with Data: " +
										 * p.toString() + " for social type:" +
										 * p.getSocialMediaType());
										 */
										//
										// mongoOps.insert(p,
										// PERSON_COLLECTION);

										// mongoOps.save(p, PERSON_COLLECTION);
										mongoOps.save(p, PERSON_COLLECTION_TEMP);
										logger.info("After save new fb post timeStamp " + (System.currentTimeMillis()));
										obj = new JSONObject();
										obj.put("uuid", p.getUuid());
										/***
										 * obj.put("apikey", p.getApiKey());
										 * obj.put("deviceToken",
										 * p.getDeviceToken());
										 * obj.put("deviceType",
										 * p.getDeviceType());
										 * obj.put("registeredId",
										 * p.getRegisterId());
										 */
										//
										obj.put("socialmediaType", p.getSocialMediaType());
										obj.put("child_user_id", p.getChild_user_id());
										obj.put("parent_id", p.getParentId());
										/***
										 * logger.info(
										 * "Json object that will be sent to response queue :    "
										 * + obj.toJSONString());
										 */
										//
										// writechannel.basicPublish("",
										// RQUEUE_NAME, null,
										// obj.toString().getBytes());
										// }else

										// }//synchronous
										/***
										 * logger.info(
										 * "###############End#############" +
										 * i.getId());
										 */
										//
									} catch (Exception e) {
										// TODO Auto-generated catch block
										logger.error("Exception processing single post" + e.toString());
									}
								}

								for (Post i : dataFeed) {
									try {

										p = new Person();
										p.setUuid((UUID.randomUUID() + ""));
										p.setId(i.getId() + "_" + child_user_id + "_" + parent_id);
										p.setName(i.getName());

										p.setInsertDate((new DateTime().toDateTimeISO().toString()));

										p.setMessage(i.getMessage());

										p.setCommentCount(0L);
										try {
											// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
											if (i.getComments() != null & i.getComments().getData() != null
													& i.getComments().getData().size() > 0) {
												String uuidCommentId = UUID.randomUUID() + "";
												long j = 0L;
												for (Comment c : i.getComments().getData()) {
													try {
														FBcomments comment = new FBcomments();
														comment.setUuidComment(uuidCommentId);
														// comment.setPostId(i.getId()
														// +"_"+child_user_id);
														comment.setPostId(p.getId());
														comment.setCommentId((c.getId() != null ? (c.getId() + "_" + p.getId())
																: ""));
														comment.setCommentMessage((c.getMessage() != null ? c.getMessage() : ""));
														comment.setCommentFrom((c.getFrom() != null ? c.getFrom().getName() != null ? c
																.getFrom().getName() : ""
																: ""));
														comment.setProfileImage("");
														comment.setCommentedById((c.getFrom().getId() != null ? c.getFrom()
																.getId() : ""));
														comment.setAttachmentType((c.getAttachment() != null ? c.getAttachment()
																.getType() : ""));
														comment.setAttachmentUrl((c.getAttachment() != null ? c.getAttachment()
																.getUrl() : ""));
														comment.setSocialMediaType((String) jsonObject.get("social_media_type"));
														comment.setChildUserId((String) jsonObject.get("child_user_id"));
														comment.setParent_id(parent_id);

														// adding flags to
														// Comments

														comment.setReadFlag(0);
														comment.setSaveFlag(0);
														comment.setDeleteFlag(0);
														comment.setErrorFlag(0);
														comment.setErrorMessage("");
														comment.setNotificationFlag(0);

														FBcomments dbcomment = new FBcomments();
														try {

															// if
															// (mongoOps.findById(i.getId()+"_"+child_user_id,
															// Person.class,
															// PERSON_COLLECTION)
															// != null) {
															// p2 =
															// mongoOps.findById(new
															// Long(i.getId()).toString()+"_"+child_user_id,
															// Person.class,
															// PERSON_COLLECTION);
															if (mongoOps.findById(comment.getCommentId(), FBcomments.class,
																	FB_COMMENT_COLLECTION) != null) {
																dbcomment = mongoOps.findById(comment.getCommentId(),
																		FBcomments.class, FB_COMMENT_COLLECTION);
																comment.setReadFlag(dbcomment.getReadFlag());
																comment.setSaveFlag(dbcomment.getSaveFlag());
																comment.setDeleteFlag(dbcomment.getDeleteFlag());
																comment.setErrorFlag(dbcomment.getErrorFlag());
																comment.setErrorMessage("");
																comment.setNotificationFlag(dbcomment.getNotificationFlag());
															}
														} catch (Exception e) {
															logger.error("fb comment  exception.." + e.toString());
														}
														// end of adding flags
														// to comment

														// mongoOps.save(comment,
														// FB_COMMENT_COLLECTION);
														mongoOps.save(comment, FB_COMMENT_COLLECTION_TEMP);
													} catch (Exception e) {
														// TODO Auto-generated
														// catch block
														logger.error("fb single comment  exception.." + e.toString());
													}
												}
												Query query5 = new Query();
												query5.addCriteria(Criteria.where("postId").is(p.getId()));
												j = (mongoOps.count(query5, FB_COMMENT_COLLECTION_TEMP));
												p.setCommentCount(j);
												p.setFbComments(uuidCommentId);
											}
										} catch (Exception e1) {
											logger.info("fb Comments list exception..." + e1.toString());
											// TODO Auto-generated catch block

										}

										Date local = i.getCreatedTime();
										long utc = zone.convertLocalToUTC(local.getTime(), false);
										p.setCreatedDate(utc);
										p.setCreatedDateApi(i.getCreatedTime().toString());
										p.setCreatedDateTimestamp(local.getTime());

										p.setPostType((i.getType() != null ? i.getType() : ""));

										p.setName((i.getName() != null ? i.getName() : ""));
										try {
											if (i.getType() == "video") {
												i.getObjectId();
											}
										} catch (Exception e2) {

										}
										try {
											p.setFacebookSource((i.getSource() != null ? i.getSource() : ""));
										} catch (Exception e1) {
											// TODO Auto-generated catch block

										}
										try {
											p.setFacebookActionList((i.getActions() != null ? i.getActions() : null));
										} catch (Exception e1) {
											// TODO Auto-generated catch block

										}
										p.setTotalCommentCount(0);

										p.setMyLiked(0);
										try {
											if (i.getLikes() != null && i.getLikes() instanceof Likes
													|| i.getLikes().getData() != null) {
												Long likeCountFromLikes = i.getLikes().getTotalCount();

												List<NamedFacebookType> likes = (i.getLikes().getData() != null ? i.getLikes()
														.getData() : null);

												if (likes != null) {
													for (NamedFacebookType e : likes) {
														String personLiked = e.getId();
														personLiked.equalsIgnoreCase((String) jsonObject
																.get("social_site_user_id"));
														p.setMyLiked(1);
													}
													long dataCount = likes.size();
													p.setLikeCount(likeCountFromLikes >= dataCount ? likeCountFromLikes
															: dataCount);

												}
											} else {
												p.setLikeCount(0L);
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											logger.error("Like count not exception...." + e.toString());

										}

										p.setPhotoUrl((i.getPicture()) != null ? (i.getPicture()) : "");

										p.setLink((i.getLink() != null ? i.getLink() : ""));

										p.setSocialMediaType((String) jsonObject.get("social_media_type"));
										p.setPostSource("feed");

										p.setChild_user_id((String) jsonObject.get("child_user_id"));
										p.setReadFlag(0);
										p.setSaveFlag(0);
										p.setDeleteFlag(0);
										p.setErrorFlag(0);
										p.setErrorMessage("");
										p.setNotificationFlag(0);

										// read and delete flags for filters
										p.setDelete_users_posts(0);
										p.setDelete_users_likes(0);
										p.setDelete_users_events(0);
										p.setDelete_users_comments(0);
										p.setRead_users_posts(0);
										p.setRead_users_likes(0);
										p.setRead_users_events(0);
										p.setRead_users_comments(0);

										p2 = new Person();
										try {
											if (mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION) != null) {
												p2 = mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION);
												p.setReadFlag(p2.getReadFlag());
												p.setSaveFlag(p2.getSaveFlag());
												p.setDeleteFlag(p2.getDeleteFlag());
												p.setErrorFlag(p2.getErrorFlag());
												p.setErrorMessage("");
												p.setNotificationFlag(p2.getNotificationFlag());

												p.setDelete_users_posts(p2.getDelete_users_posts());
												p.setDelete_users_likes(p2.getDelete_users_likes());
												p.setDelete_users_events(p2.getDelete_users_events());
												p.setDelete_users_comments(p2.getDelete_users_comments());
												p.setRead_users_posts(p2.getRead_users_posts());
												p.setRead_users_likes(p2.getRead_users_likes());
												p.setRead_users_events(p2.getRead_users_events());
												p.setRead_users_comments(p2.getRead_users_comments());

											}
										} catch (Exception e) {
											logger.info("facebook flag modification exception.." + e.toString());
										}

										DateTime retriveDate = new DateTime(i.getUpdatedTime());
										DateTime sendingDate = new DateTime(retriveDate.getYear(), retriveDate.getMonthOfYear(),
												retriveDate.getDayOfMonth(), 0, 0, 0);
										p.setUpdatedDate(sendingDate.getMillis());
										p.setUpdateDateTime(retriveDate.getMillis());

										p.setCaption((i.getCaption() != null ? i.getCaption() : ""));

										p.setDescription((i.getDescription() != null ? i.getDescription() : ""));

										p.setStory((i.getStory() != null ? i.getStory() : ""));

										p.setIcon((i.getIcon() != null ? i.getIcon() : ""));

										p.setPicture((i.getPicture() != null ? i.getPicture() : ""));

										p.setFromId((i.getFrom().getId() != null ? i.getFrom().getId() : ""));

										p.setAttribution((i.getAttribution() != null ? i.getAttribution() : ""));

										p.setParentId((String) jsonObject.get("parent_id"));
										p.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));

										try {

											if (writechannel != null && writechannel.isOpen()) {

												writechannel.close();
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block

											logger.info("Exception in closing the channel. " + e.toString());
										}
										// writechannel =
										// connection1.createChannel();
										// writechannel.queueDeclare(RQUEUE_NAME,
										// false, false, false, args);

										// mongoOps.save(p, PERSON_COLLECTION);
										mongoOps.save(p, PERSON_COLLECTION_TEMP);
										logger.info("After save new fb post timeStamp " + (System.currentTimeMillis()));
										obj = new JSONObject();
										obj.put("uuid", p.getUuid());
										/***
										 * obj.put("apikey", p.getApiKey());
										 * obj.put("deviceToken",
										 * p.getDeviceToken());
										 * obj.put("deviceType",
										 * p.getDeviceType());
										 * obj.put("registeredId",
										 * p.getRegisterId());
										 */
										//
										obj.put("socialmediaType", p.getSocialMediaType());
										obj.put("child_user_id", p.getChild_user_id());
										obj.put("parent_id", p.getParentId());
										/***
										 * logger.info(
										 * "Json object that will be sent to response queue :    "
										 * + obj.toJSONString());
										 */
										//
										// writechannel.basicPublish("",
										// RQUEUE_NAME, null,
										// obj.toString().getBytes());
										try {

											if (writechannel != null && writechannel.isOpen()) {

												writechannel.close();
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block

											logger.info("Exception in closing the channel. " + e.toString());
										}

										// }else

										// }//synchronous
										/***
										 * logger.info(
										 * "###############End#############" +
										 * i.getId());
										 */
										//
									} catch (Exception e) {
										// TODO Auto-generated catch block
										logger.info("Exception in Processing a single user homefeed data " + e.toString());
									}
								}

								if (p1 == null)

								{
									errorPerson = new ErrorPerson();
									errorPerson.setErrorOccuredDate(new Date().toString());
									errorPerson.setChildId(child_user_id);
									errorPerson.setParent_id(parent_id);
									errorPerson.setUuid((UUID.randomUUID() + ""));
									errorPerson.setInsertDate(new Date().toString());

									errorPerson.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));

									errorPerson.setSocialMediaType((String) jsonObject.get("social_media_type"));

									errorPerson.setErrorFlag("0");
									mongoOps.save(errorPerson, PERSON_LOGIN_STATUS);

								}

							} // end of if
						} catch (Exception e) {
							logger.error("UnknownHostException: e.toSting: " + e.toString());

						} finally {
							if (writechannel != null && writechannel.isOpen()) {
								writechannel.close();
							}
							try {
								writechannel = connection1.createChannel();
								writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
								writechannel.basicPublish("", RQUEUE_NAME, null, jsonObject.toString().getBytes());
								logger.info("FB message placed for  child  :" + child_user_id + "with parent " + parent_id);
							} catch (Exception e4) {
								// TODO Auto-generated catch block
								e4.printStackTrace();
							}
							if (writechannel != null && writechannel.isOpen()) {
								writechannel.close();
							}
							mongoOps = null;
							p = null;
							p1 = null;
						}
						logger.info("End of the fb Thread: at :" + (System.currentTimeMillis()));
					} catch (Exception e) {
						logger.error("Exception:" + e.getMessage());
						e.printStackTrace();
					} finally {

						if (mongo != null && mongo instanceof MongoClient) {
							try {
								mongo.close();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								logger.error("mongo connection exception : " + e.toString());
							}
						}
						if (writechannel != null && writechannel.isOpen()) {
							try {
								writechannel.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								logger.info("Write channel closing exception:" + e.getMessage() + " e.tostring" + e.toString());

							}
						}

					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.info("Ending of outermost try:" + e.getMessage() + " e.tostring" + e.toString());

				} finally {

					// SocialNetworkService.getInstance().releaseThreaPoolLock();
					PersonLock.getInstance().releaseThreaPoolLock();
					/*
					 * try { Thread.currentThread().stop(); } catch (Exception
					 * e) { // TODO Auto-generated catch block
					 * 
					 * }
					 */

				}
			}

		});

		t1.start();

	}
}
