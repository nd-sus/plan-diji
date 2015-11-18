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

public class BQInstagramListner  implements Runnable{
	 static Logger logger = Logger.getLogger(BQInstagramListner.class);
	 byte[] body;
	 String routingKey;
	ExecutorService executor;
	 com.rabbitmq.client.Connection connection1;
	 Properties prop;
	 Long counterValue;
	public BQInstagramListner( byte[] body,
			 String routingKey, ExecutorService executor, 
			 com.rabbitmq.client.Connection connection1,
			 Properties prop,  Long counterValue) {
		this.body=body;
		this.routingKey=routingKey;
		this.executor=executor;
		this.connection1=connection1;
		this.prop=prop;
		
		
		
	}
		
			@SuppressWarnings({ "unchecked" })
			@Override
			public void run() {

				
				List<MediaFeedData> data = null;
				String accessTokenValue = null;
				MongoOperations mongoOps = null;
				MongoClient mongo = null;
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
					/* logger.info("dateAgo instragram..." + dateNoOfdaysAgo); */
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
					/*
					 * com.rabbitmq.client.ConnectionFactory factory1 = new
					 * com.rabbitmq.client.ConnectionFactory();
					 * factory1.setNetworkRecoveryInterval(5000);
					 * factory1.setHost(HOST); factory1.setPort(PORT);
					 * com.rabbitmq.client.Connection connection1 = factory1
					 * .newConnection();
					 */

					String message = new String(body);
					JSONParser jsonParser = new JSONParser();
					JSONObject jsonObject = (JSONObject) jsonParser
							.parse(message);
					accessTokenValue = ((String) jsonObject.get("access_token") != null ? (String) jsonObject
							.get("access_token") : "");

					try {
						mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
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

					try {

						 instagram = new Instagram(secretToken);
						MediaFeed mediaFeed = instagram.getUserFeeds();
						/* logger.info("mediaFeed.getData instragram..."); */

						data = mediaFeed.getData();
						/*
						 * logger.info("Thread  Got data  w.r.t accesstoken :" +
						 * (String) jsonObject.get("access_token") +
						 * "\n Thread Name:" + this.getName());
						 * logger.info("insta testing...");
						 */
						mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
						mongoOps = new MongoTemplate(mongo, DB_NAME);
						for (MediaFeedData i : data) {
							/*
							 * DateTime createdPostDate = new DateTime(new Date(
							 * i.getCreatedTime()));
							 */
							logger.info("start processing instagram "
									+ i.getId() + " is "
									+ (System.currentTimeMillis()));
							DateTime createdPostDate = new DateTime(new Date(
									Long.parseLong(i.getCreatedTime()) * 1000));
							DateTime dateLimiter = new DateTime(dateNoOfdaysAgo);
							if (createdPostDate.getMillis() >= dateLimiter
									.getMillis()) {
								/*
								 * logger.info("###############Start#############a"
								 * + i.getId());
								 */
								/*
								 * logger.info(
								 * "Thread started reading post with id : " +
								 * i.getId()+"  MediaFeedData: "+i.toString()+
								 * " commentsList"
								 * +i.getComments().getComments().size()); for
								 * (CommentData cd
								 * :i.getComments().getComments() )
								 * logger.info("comment data: "+ cd.toString());
								 */
								p = new Person();

								p.setUuid((UUID.randomUUID() + ""));
								p.setId(i.getId());
								p.setFullName(i.getUser().getFullName());

								p.setInsertDate(new DateTime().toDateTimeISO()
										.toString());

								/*
								 * DateTime insertDate = new DateTime()
								 * .toDateTimeISO();
								 * p.setInsertDate(insertDate.getYear() + "-" +
								 * insertDate.getMonthOfYear() + "-" +
								 * insertDate.getDayOfMonth());
								 */
								// p.setInsertDate(insertDate.getDayOfMonth()+"-"+insertDate.getMonthOfYear()+"-"+insertDate.getYear());
								p.setCaption((i.getCaption() != null ? i
										.getCaption().getText() : ""));

								/*
								 * p.setCreatedDate((i.getCreatedTime() != "" ?
								 * (new DateTime( new Date(Long.parseLong(i
								 * .getCreatedTime()) * 1000))
								 * .toDateTimeISO().getMillis()) : 0L));
								 */

								// final DateTime dateTime = new DateTime(new
								// DateTime(i.getCreatedTime()).toDate().getTime(),
								// DateTimeZone.forID("UTC"));
								// p.setCreatedDate(dateTime.getMillis());
								/*
								 * logger.info(
								 * "Thread started reading post with id : \n\n curenttime "
								 * +i.getCreatedTime()+"  trying to insert "+new
								 * DateTime(new Date(
								 * Long.parseLong(i.getCreatedTime()) *
								 * 1000)).withZone
								 * (DateTimeZone.UTC).getMillis());
								 * p.setCreatedDate(new DateTime(new Date(
								 * Long.parseLong(i.getCreatedTime()) *
								 * 1000)).withZone
								 * (DateTimeZone.UTC).getMillis());
								 */
								Date local = new Date(Long.parseLong(i
										.getCreatedTime()) * 1000);
								long utc = zone.convertLocalToUTC(
										local.getTime(), false);
								p.setCreatedDate(utc);

								/*
								 * if (i.getCreatedTime() != null) { DateTime
								 * createdDate = new DateTime( new
								 * Date(Long.parseLong(i .getCreatedTime()) *
								 * 1000));
								 * 
								 * p.setCreatedDate(createdDate
								 * .toDateTimeISO().getYear() + "-" +
								 * createdDate.toDateTimeISO() .getMonthOfYear()
								 * + "-" + createdDate.toDateTimeISO()
								 * .getDayOfMonth());
								 * 
								 * }
								 */

								/* logger.info("Check Same Post data : "); */
								// i.getMessage(),
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

								// (i.getName() != null ? i.getName() : ""),
								p.setLikeCount((long) (i.getLikes().getCount()));
								p.setCommentCount((long) (i.getComments()
										.getCount()));
								p.setLink(i.getLink() != null ? i.getLink()
										: "");
								// p.setCommentCount(0L);
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
										// p.setCommentCount((long)i.getComments().getComments().size());

										// p.setCommentCount((long)j);
									}
								} catch (Exception e1) {
									logger.info("insta Comments list exception..."
											+ e1.toString());
									// TODO Auto-generated catch block

								}
								// i.getPicture(),
								p.setImages((i.getImages()));
								p.setVideos(i.getVideos());
								// (i.getLink() != null ? i.getLink() : ""),
								// p.setUpdatedDate(p.getCreatedDate());
								p.setUpdateDateTime(dttimeCreated.getMillis());
								p.setUpdatedDate(dtNewtimeCreated.getMillis());
								p.setSocialMediaType((String) jsonObject
										.get("social_media_type"));
								/*
								 * p.setDeviceType((String) jsonObject
								 * .get("device_type"));
								 * p.setDeviceToken((String) jsonObject
								 * .get("device_token"));
								 * p.setRegisterId((String) jsonObject
								 * .get("registration_id"));
								 * p.setApiKey((String)
								 * jsonObject.get("apikey"));
								 */
								p.setChild_user_id((String) jsonObject
										.get("child_user_id"));
								p.setReadFlag(0);
								p.setSaveFlag(0);
								p.setDeleteFlag(0);
								p.setErrorFlag(0);
								p.setErrorMessage("");
								p.setNotificationFlag(0);
								p.setParentId((String) jsonObject
										.get("parent_id"));
								p.setSocialSiteUserId((String) jsonObject
										.get("social_site_user_id"));

								synchronized (this) {
									try {

										if (writechannel != null
												&& writechannel.isOpen()) {
											/*
											 * logger.info(
											 * "write channel getting closed");
											 */
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
									p1 = new Person();
									if (mongoOps.findById(i.getId(),
											Person.class, PERSON_COLLECTION) != null) {
										p1 = mongoOps
												.findById(i.getId(),
														Person.class,
														PERSON_COLLECTION);
									}
									if (p1.getUuid() != null
											&& p1.toString().length() > 0) {
										/*
										 * logger.info("Check Same Post data : "
										 * + (p1.toString()).equals(p
										 * .toString()) + "@@" + i.getId());
										 */
										if (!(p1.toString()).equals(p
												.toString())) {
											/*
											 * logger.info(
											 * "Update the post with Data: " +
											 * p.toString() +
											 * " for social type:" +
											 * p.getSocialMediaType());
											 */
											mongoOps.save(p, PERSON_COLLECTION);
											logger.info("After updating  post timeStamp of instagram "
													+ p.getId()
													+ " is "
													+ (System
															.currentTimeMillis()));
											obj = new JSONObject();
											obj.put("uuid", p.getUuid());
											/*
											 * obj.put("apikey", p.getApiKey());
											 * obj.put("deviceToken",
											 * p.getDeviceToken());
											 * obj.put("deviceType",
											 * p.getDeviceType());
											 * obj.put("registeredId",
											 * p.getRegisterId());
											 */
											obj.put("socialmediaType",
													p.getSocialMediaType());
											obj.put("child_user_id",
													p.getChild_user_id());
											obj.put("parent_id",
													p.getParentId());
											/*
											 * logger.info(
											 * "Json object that will be sent to response queue for id:    "
											 * + p.getId() + " is" +
											 * obj.toJSONString());
											 */
											writechannel.basicPublish("",
													RQUEUE_NAME, null, obj
															.toString()
															.getBytes());
										}
									} else {
										/*
										 * logger.info("New post with Data: " +
										 * p.toString() + " for social type:" +
										 * p.getSocialMediaType());
										 */

										mongoOps.save(p, PERSON_COLLECTION);
										logger.info("After save new Instagram post timeStamp id"
												+ p.getId()
												+ " is "
												+ (System.currentTimeMillis()));
										obj = new JSONObject();
										obj.put("uuid", p.getUuid());
										/*
										 * obj.put("apikey", p.getApiKey());
										 * obj.put("deviceToken",
										 * p.getDeviceToken());
										 * obj.put("deviceType",
										 * p.getDeviceType());
										 * obj.put("registeredId",
										 * p.getRegisterId());
										 */
										obj.put("socialmediaType",
												p.getSocialMediaType());
										obj.put("child_user_id",
												p.getChild_user_id());
										obj.put("parent_id", p.getParentId());
										/*
										 * logger.info(
										 * "Json object that will be sent to response queue :    "
										 * + obj.toJSONString());
										 */
										writechannel.basicPublish("",
												RQUEUE_NAME, null, obj
														.toString().getBytes());

									}
									writechannel.close();
								}
								/*
								 * logger.info("###############End#############"
								 * + i.getId());
								 */
							}// if condtion
						}// for loop

					} catch (Exception e) {
						logger.error("Exception: " + e.getMessage() + "--"
								+ e.getStackTrace() + " e.tostring:"
								+ e.toString());
						e.printStackTrace();
					} finally {
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
