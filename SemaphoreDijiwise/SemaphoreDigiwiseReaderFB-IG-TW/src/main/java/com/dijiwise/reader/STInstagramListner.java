package com.dijiwise.reader;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;

public class STInstagramListner {
	 static Logger logger = Logger.getLogger(STInstagramListner.class);
	 byte[] body;
	 String routingKey;
	ExecutorService executor;
	 com.rabbitmq.client.Connection connection1;
	 Properties prop;
	 Long counterValue;
	public STInstagramListner( byte[] body,
			 String routingKey, ExecutorService executor, 
			 com.rabbitmq.client.Connection connection1,
			 Properties prop,  Long counterValue) {
		this.body=body;
		this.routingKey=routingKey;
		this.executor=executor;
		this.connection1=connection1;
		this.prop=prop;
		
		
		
	}
		
			
			public static void processData( byte[] body,
					 String routingKey, ExecutorService executor, 
					 com.rabbitmq.client.Connection connection1,
					 Properties prop,  Long counterValue,MongoClient mongo) {

				
				List<MediaFeedData> data = null;
				String accessTokenValue = null;
				MongoOperations mongoOps = null;
				//MongoClient mongo = null;
				String PERSON_COLLECTION = "";
				Token secretToken = null;
				Channel writechannel = null;
				String FB_COMMENT_COLLECTION = "";
				DateTimeZone zone = DateTimeZone.getDefault();
				
				Person errorPerson = null;
				JSONObject obj = null;
				Person p = null;
				Person p1 = null;
				Instagram instagram=null;
				try {

					Date dateNoOfdaysAgo = new DateTime(new Date()).minusDays(
							Integer.parseInt(prop.getProperty("DAYS_OLD")))
							.toDate();
					
					final String RQUEUE_NAME = prop.getProperty("RQUEUE_NAME");
				
					final String DB_NAME = prop.getProperty("DB_NAME");
					PERSON_COLLECTION = prop.getProperty("PERSON_COLLECTION");
					final String MONGO_HOST = prop.getProperty("MONGO_HOST");
					FB_COMMENT_COLLECTION = prop
							.getProperty("FB_COMMENT_COLLECTION");
					final int MONGO_PORT = Integer.parseInt(prop
							.getProperty("MONGO_PORT"));
					Map<String, Object> args = new HashMap<String, Object>();
					args.put("x-cancel-on-ha-failover", true);
					

					String message = new String(body);
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser
							.parse(message);
					accessTokenValue = ((String) jsonObject.get("access_token") != null ? (String) jsonObject
							.get("access_token") : "");

					try {
						if (mongo != null && mongo instanceof MongoClient) {
							try {
								mongo.close();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								logger.error("mongo connection exception : "
										+ e.toString());
							}
						}
						//mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
						mongoOps = new MongoTemplate(mongo, DB_NAME);
						secretToken = new Token(accessTokenValue, null);
						/*
						 * logger.info("secretToken instragram..." +
						 * secretToken);
						 */
					} catch (Exception e) {
						logger.error("Exception: " + e.getMessage() + "--"
								+ e.getStackTrace() + " e.toString: "
								+ e.toString());
						errorPerson = new Person();

						errorPerson.setUuid((UUID.randomUUID() + ""));
						errorPerson.setParentId((String) jsonObject
								.get("parent_id"));
						errorPerson.setSocialSiteUserId((String) jsonObject
								.get("social_site_user_id"));
						errorPerson.setErrorFlag(1);
						errorPerson.setErrorMessage(e.toString());
						errorPerson.setSocialMediaType((String) jsonObject
								.get("social_media_type"));
						mongoOps.save(errorPerson, PERSON_COLLECTION);
					}
					finally{
						
					}

					try {

						 instagram = new Instagram(secretToken);
						MediaFeed mediaFeed = instagram.getUserFeeds();
						

						data = mediaFeed.getData();
						
						mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
						mongoOps = new MongoTemplate(mongo, DB_NAME);
						for (MediaFeedData i : data) {
							
							logger.info("\n STINSTAGRAM start processing instagram "
									+ i.getId() + " is "
									+ (System.currentTimeMillis()));
							DateTime createdPostDate = new DateTime(new Date(
									Long.parseLong(i.getCreatedTime()) * 1000));
							DateTime dateLimiter = new DateTime(dateNoOfdaysAgo);
							if (createdPostDate.getMillis() >= dateLimiter
									.getMillis()) {
							
								p = new Person();

								p.setUuid((UUID.randomUUID() + ""));
								p.setId(i.getId());
								p.setFullName(i.getUser().getFullName());

								p.setInsertDate(new DateTime().toDateTimeISO()
										.toString());

								p.setCaption((i.getCaption() != null ? i
										.getCaption().getText() : ""));

								
								Date local = new Date(Long.parseLong(i
										.getCreatedTime()) * 1000);
								long utc = zone.convertLocalToUTC(
										local.getTime(), false);
								p.setCreatedDate(utc);

								Date dtCreated = new Date(Long.parseLong(i
										.getCreatedTime()) * 1000);
								DateTime dttimeCreated = new DateTime(dtCreated);
								DateTime dtNewtimeCreated = new DateTime(
										dttimeCreated.getYear(),
										dttimeCreated.getMonthOfYear(),
										dttimeCreated.getDayOfMonth(), 0, 0, 0);
								p.setPhotoUrl((i.getUser()
										.getProfilePictureUrl()) != null ? (i
										.getUser().getProfilePictureUrl()) : "");

								
								p.setLikeCount((long) (i.getLikes().getCount()));
								p.setCommentCount((long) (i.getComments()
										.getCount()));
								p.setLink(i.getLink() != null ? i.getLink()
										: "");
								
								try {

									if (i.getComments() != null
											& i.getComments().getComments() != null
											& i.getComments().getComments()
													.size() > 0) {
										String uuidCommentId = UUID
												.randomUUID() + "";
										int j = 0;

										for (CommentData c : i.getComments()
												.getComments()) {

											FBcomments comment = new FBcomments();
											comment.setUuidComment(uuidCommentId);
											comment.setPostId(i.getId());
											comment.setCommentId((c.getId() != null ? c
													.getId() : ""));
											comment.setCommentMessage((c
													.getText() != null ? c
													.getText() : ""));
											comment.setCommentFrom((c
													.getCommentFrom() != null ? c
													.getCommentFrom()
													.getFullName() : ""));
											comment.setProfileImage((c
													.getCommentFrom()
													.getProfilePicture() != null ? c
													.getCommentFrom()
													.getProfilePicture() : ""));
											comment.setCommentedById((c
													.getCommentFrom().getId() != null ? c
													.getCommentFrom().getId()
													: ""));
											comment.setImage("");
											comment.setVideo("");
											j++;
											mongoOps.save(comment,
													FB_COMMENT_COLLECTION);
										}
										p.setFbComments(uuidCommentId);
										
									}
								} catch (Exception e1) {
									logger.info("insta Comments list exception..."
											+ e1.toString());
									// TODO Auto-generated catch block

								}
								
								p.setImages((i.getImages()));
								p.setVideos(i.getVideos());
								
								p.setUpdateDateTime(dttimeCreated.getMillis());
								p.setUpdatedDate(dtNewtimeCreated.getMillis());
								p.setSocialMediaType((String) jsonObject
										.get("social_media_type"));
								
								p.setChild_user_id((String) jsonObject
										.get("child_user_id"));
								p.setReadFlag(0);
								p.setSaveFlag(0);
								p.setDeleteFlag(0);
								p.setErrorFlag(0);
								p.setErrorMessage("");
								p.setNotificationFlag(0);
								
								p1 = new Person();
								try {
									if (mongoOps.findById(i.getId(), Person.class,
											PERSON_COLLECTION) != null) {
										p1 = mongoOps.findById(i.getId(),
												Person.class, PERSON_COLLECTION);
										p.setReadFlag(p1.getReadFlag());
										p.setSaveFlag(p1.getSaveFlag());
										p.setDeleteFlag(p1.getDeleteFlag());
										p.setErrorFlag(p1.getErrorFlag());
										p.setErrorMessage("");
										p.setNotificationFlag(p1.getNotificationFlag());
									}
								} catch (Exception e) {
									logger.info("Instagram flag modification exception.." 
											+ e.toString());
								}
								
								
								
								
								
								p.setParentId((String) jsonObject
										.get("parent_id"));
								p.setSocialSiteUserId((String) jsonObject
										.get("social_site_user_id"));

							//	synchronized (this) {
									try {

										if (writechannel != null
												&& writechannel.isOpen()) {
											
											writechannel.close();
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block

										logger.info("Exception in closing the channel. "
												+ e.toString());
									}
									writechannel = connection1.createChannel();
									writechannel.queueDeclare(RQUEUE_NAME,
											false, false, false, args);
									/*p1 = new Person();
									if (mongoOps.findById(i.getId(),
											Person.class, PERSON_COLLECTION) != null) {
										p1 = mongoOps
												.findById(i.getId(),
														Person.class,
														PERSON_COLLECTION);
									}
									if (p1.getUuid() != null
											&& p1.toString().length() > 0) {
										
										  logger.info("Check Same Post data : "
										  + (p1.toString()).equals(p
										  .toString()) + "@@" + i.getUser().getFullName());
										 
										if (!(p1.toString()).equals(p
												.toString())) {
											
											mongoOps.save(p, PERSON_COLLECTION);
											logger.info("After updating  post timeStamp of instagram "
													+ p.getId()+" name"+i.getUser().getFullName()
													+ " is "
													+ (System
															.currentTimeMillis()));
											obj = new JSONObject();
											obj.put("uuid", p.getUuid());
											
											obj.put("socialmediaType",
													p.getSocialMediaType());
											obj.put("child_user_id",
													p.getChild_user_id());
											obj.put("parent_id",
													p.getParentId());
											
											writechannel.basicPublish("",
													RQUEUE_NAME, null, obj
															.toString()
															.getBytes());
										}
									} else  {*/
										

										mongoOps.save(p, PERSON_COLLECTION);
										p1 = new Person();
										/*if (mongoOps.findById(i.getId(),
												Person.class, PERSON_COLLECTION) == null) {
										logger.info("After save new Instagram post timeStamp id"
												+ p.getId()+" name"+i.getUser().getFullName()
												+ " is "
												+ (System.currentTimeMillis()));*/
										obj = new JSONObject();
										obj.put("uuid", p.getUuid());
										
										obj.put("socialmediaType",
												p.getSocialMediaType());
										obj.put("child_user_id",
												p.getChild_user_id());
										obj.put("parent_id", p.getParentId());
										
										writechannel.basicPublish("",
												RQUEUE_NAME, null, obj
														.toString().getBytes());
									//	}else

									//}//end of else
									
								//}
							}		
					//		}// if condtion
						}// for loop
						writechannel.close();
					} catch (Exception e) {
						logger.error("Exception: " + e.getMessage() + "--"
								+ e.getStackTrace() + " e.tostring:"
								+ e.toString());
						e.printStackTrace();
					} finally {
						if (mongo != null && mongo instanceof MongoClient) {
							try {
								mongo.close();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								logger.error("mongo connection exception : "
										+ e.toString());
							}
						}
						if (writechannel != null && writechannel.isOpen()) {
							writechannel.close();
						}
						instagram=null;
						
					}
					
				} catch (Exception e) {
					logger.error(" Error in instragram ending  " + e.toString()
							+ " e.getMessage: " + e.getMessage()
							+ " e.toSting: " + e.toString());
				} finally {
					errorPerson = null;
					data = null;
					obj = null;
					if (mongo != null && mongo instanceof MongoClient) {
						try {
							mongo.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (writechannel != null && writechannel.isOpen()) {
						try {
							writechannel.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							logger.info("Write channel closing exception:"
									+ e.getMessage() + " e.tostring"
									+ e.toString());
						}
					}
				}
			}
		
	

}
