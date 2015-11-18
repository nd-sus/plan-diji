package com.dijiwise.semaphore;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.entity.users.feed.UserFeed;
import org.jinstagram.entity.users.feed.UserFeedData;
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
import com.rabbitmq.client.Channel;

public class InstagramWorker {
	static Logger logger = Logger.getLogger(InstagramWorker.class);
	byte[] body;
	String routingKey;
	ExecutorService executor;
	com.rabbitmq.client.Connection connection1;
	Properties prop;
	Long counterValue;
public InstagramWorker(){}
	public InstagramWorker(byte[] body, String routingKey, ExecutorService executor, com.rabbitmq.client.Connection connection1,
			Properties prop, Long counterValue) {
		this.body = body;
		this.routingKey = routingKey;
		this.executor = executor;
		this.connection1 = connection1;
		this.prop = prop;

	}

	public static void processData(final byte[] body, final String routingKey,

	final com.rabbitmq.client.Connection connection1, final Properties prop, final Long counterValue, final MongoClient mongo1) {
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					logger.setLevel(Level.INFO);
					System.setProperty("DEBUG.MONGO", "false");
					// SocialNetworkService.getInstance().getThreaPoolLock();
					PersonLock.getInstance().getThreaPoolLock();

					List<MediaFeedData> data = null;
					List<MediaFeedData> dataFeed = null;
					List<MediaFeedData> userdataFeed = null;
					String accessTokenValue = null;
					String socialSiteUserId = null;
					MongoOperations mongoOps = null;
					MongoClient mongo = null;
					String PERSON_COLLECTION = "";
					String PERSON_LOGIN_STATUS = "";
					String PERSON_FOLLOWING_FOLLOWS = "";
					Token secretToken = null;
					Channel writechannel = null;
					String FB_COMMENT_COLLECTION = "";
					String PERSON_COLLECTION_TEMP="";
					String FB_COMMENT_COLLECTION_TEMP = "";
					DateTimeZone zone = DateTimeZone.getDefault();

					ErrorPerson errorPerson = null;
					JSONObject obj = null;
					Person p = null;
					ErrorPerson p1 = null;
					Instagram instagram = null;
					String child_user_id = null;
					String parent_id=null;
					List<UserFeedData> ulist = null;
					List<UserFeedData> followList = null;
					 String RQUEUE_NAME=null;

					try {

						Date dateNoOfdaysAgo = new DateTime(new Date()).minusDays(Integer.parseInt(prop.getProperty("DAYS_OLD")))
								.toDate();

						RQUEUE_NAME = prop.getProperty("RQUEUE_NAME");

						final String DB_NAME = prop.getProperty("DB_NAME");
						PERSON_COLLECTION = prop.getProperty("PERSON_COLLECTION");
						PERSON_LOGIN_STATUS = prop.getProperty("PERSON_LOGIN_STATUS");
						final String MONGO_HOST = prop.getProperty("MONGO_HOST");
						FB_COMMENT_COLLECTION = prop.getProperty("FB_COMMENT_COLLECTION");
						PERSON_FOLLOWING_FOLLOWS = prop.getProperty("PERSON_FOLLOWING_FOLLOWS");
						PERSON_COLLECTION_TEMP=prop.getProperty("PERSON_COLLECTION_TEMP");
						FB_COMMENT_COLLECTION_TEMP =prop.getProperty("FB_COMMENT_COLLECTION_TEMP");
						final int MONGO_PORT = Integer.parseInt(prop.getProperty("MONGO_PORT"));
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("x-cancel-on-ha-failover", true);

						String message = new String(body);
						JSONParser jsonParser = new JSONParser();
						JSONObject jsonObject = (JSONObject) jsonParser.parse(message);
						
						accessTokenValue = ((String) jsonObject.get("access_token") != null ? (String) jsonObject
								.get("access_token") : "");
						

						socialSiteUserId = ((String) jsonObject.get("social_site_user_id") != null ? (String) jsonObject
								.get("social_site_user_id") : "");
						child_user_id = (String) jsonObject.get("child_user_id");
						parent_id = (String) jsonObject.get("parent_id");
						logger.info("\nSTInstagram Listener start of the  Thread: at :" + (System.currentTimeMillis())
								+ " childuserId: " + child_user_id +" of parent is "+parent_id);

						try {
							if (mongo != null && mongo instanceof MongoClient) {
								try {
									mongo.close();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									logger.error("mongo connection exception : " + e.toString());
								}
							}
							mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
							mongoOps = new MongoTemplate(mongo, DB_NAME);
							secretToken = new Token(accessTokenValue, null);

							instagram = new Instagram(secretToken);
							MediaFeed mediaFeed = instagram.getUserFeeds();
							// instagram.getRecentMediaFeed(socialSiteUserId);
							MediaFeed mediaFeedLiked = instagram.getUserLikedMediaFeed();
							MediaFeed usermediaFeed = instagram.getRecentMediaFeed(socialSiteUserId);

							UserFeed userFollowedBy = instagram.getUserFollowedByList(socialSiteUserId);

							// instagram.getUserRelationship(userId);

							ulist = userFollowedBy.getUserList();
							UserFeed userFollowing = instagram.getUserFollowList(socialSiteUserId);
							followList = userFollowing.getUserList();

							data = mediaFeed.getData();
							dataFeed = mediaFeedLiked.getData();
							userdataFeed = usermediaFeed.getData();

						} catch (Exception e) {
							p1 = new ErrorPerson();
							errorPerson = new ErrorPerson();
							logger.info("\nSTInstagram Listener Error at :" + (System.currentTimeMillis()) + " childuserId: "
									+ child_user_id +" of parent is "+parent_id);
							/*
							 * try { if (mongoOps.findById(child_user_id,
							 * ErrorPerson.class, PERSON_LOGIN_STATUS) != null)
							 * { p1 = mongoOps.findById(child_user_id,
							 * ErrorPerson.class, PERSON_LOGIN_STATUS);
							 * errorPerson.setInsertDate(p1.getInsertDate()); }
							 * } catch (Exception e1) {
							 * logger.info("facebook flag modification exception.."
							 * + e1.toString()); }
							 */
							mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
							mongoOps = new MongoTemplate(mongo, DB_NAME);

							errorPerson.setChildId(child_user_id);
							errorPerson.setParent_id(parent_id);
							errorPerson.setErrorOccuredDate(new Date().toString());
							errorPerson.setErrorMessage(e.getMessage());
							errorPerson.setUuid((UUID.randomUUID() + ""));

							errorPerson.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));
							errorPerson.setErrorMessage(e.getMessage());

							errorPerson.setSocialMediaType((String) jsonObject.get("social_media_type"));
							if(e.getMessage().contains("rate")|e.getMessage().contains("Rate")){
								errorPerson.setErrorFlag("0");
								}
								else{
									errorPerson.setErrorFlag("1");	
								}

							mongoOps.save(errorPerson, PERSON_LOGIN_STATUS);
							
							
						} finally {

						}

						try {
							if (errorPerson == null) {

								mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
								mongoOps = new MongoTemplate(mongo, DB_NAME);

								try {
									saveFeedData(data, dateNoOfdaysAgo, zone, mongo, mongoOps, jsonObject, FB_COMMENT_COLLECTION,
											PERSON_COLLECTION,PERSON_COLLECTION_TEMP,FB_COMMENT_COLLECTION_TEMP, RQUEUE_NAME, connection1, args, child_user_id,parent_id, "OtherFeed");
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									logger.error("\n Error at otherFeed childuserId: " + child_user_id + " " + e1.getMessage());
								}
								try {
									saveFeedData(dataFeed, dateNoOfdaysAgo, zone, mongo, mongoOps, jsonObject,
											FB_COMMENT_COLLECTION, PERSON_COLLECTION,PERSON_COLLECTION_TEMP,FB_COMMENT_COLLECTION_TEMP, RQUEUE_NAME, connection1, args,
											child_user_id,parent_id, "UserLikedMediaFeed");
								} catch (Exception e2) {
									// TODO Auto-generated catch block
									logger.error("\n Error at otherFeed childuserId: " + child_user_id + " " + e2.getMessage());
								}
								try {
									saveFeedData(userdataFeed, dateNoOfdaysAgo, zone, mongo, mongoOps, jsonObject,
											FB_COMMENT_COLLECTION, PERSON_COLLECTION,PERSON_COLLECTION_TEMP,FB_COMMENT_COLLECTION_TEMP, RQUEUE_NAME, connection1, args,
											child_user_id,parent_id, "userfeed");
								} catch (Exception e3) {
									// TODO Auto-generated catch block
									logger.error("\n Error at otherFeed childuserId: " + child_user_id + " " + e3.getMessage());
								}

								/*
								 * saveFollowedBy(ulist, dateNoOfdaysAgo, zone,
								 * mongo, mongoOps, jsonObject,
								 * FB_COMMENT_COLLECTION,
								 * PERSON_FOLLOWING_FOLLOWS, RQUEUE_NAME,
								 * connection1, args, socialSiteUserId, "you");
								 * saveFollowedBy(followList, dateNoOfdaysAgo,
								 * zone, mongo, mongoOps, jsonObject,
								 * FB_COMMENT_COLLECTION,
								 * PERSON_FOLLOWING_FOLLOWS, RQUEUE_NAME,
								 * connection1, args, socialSiteUserId,
								 * "following");
								 */
								try {
									saveFollowFollowers(ulist, dateNoOfdaysAgo, zone, mongo, mongoOps, jsonObject,
											FB_COMMENT_COLLECTION, PERSON_COLLECTION,PERSON_COLLECTION_TEMP,FB_COMMENT_COLLECTION_TEMP, RQUEUE_NAME, connection1, args,
											socialSiteUserId, child_user_id,parent_id, "you", " is following you.");
								} catch (Exception e4) {
									// TODO Auto-generated catch block
									logger.error("\n Error at otherFeed childuserId: " + child_user_id + " " + e4.getMessage());
								}
								try {
									saveFollowFollowers(followList, dateNoOfdaysAgo, zone, mongo, mongoOps, jsonObject,
											FB_COMMENT_COLLECTION, PERSON_COLLECTION,PERSON_COLLECTION_TEMP,FB_COMMENT_COLLECTION_TEMP, RQUEUE_NAME, connection1, args,
											socialSiteUserId, child_user_id,parent_id, "following", " You are following ");
								} catch (Exception e5) {
									// TODO Auto-generated catch block
									logger.error("\n Error at otherFeed childuserId: " + child_user_id + " " + e5.getMessage());
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

							}// end of if
						} catch (Exception e) {
							logger.error("Exception: " + e.getMessage() + "--" + e.getStackTrace() + " e.tostring:"
									+ e.toString());
							e.printStackTrace();
						} finally {
							if (writechannel != null && writechannel.isOpen()) {
								writechannel.close();
							}
							writechannel = connection1.createChannel();
							writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
							writechannel.basicPublish("", RQUEUE_NAME, null, jsonObject.toString().getBytes());
							logger.info("IG message placed for  child  :" +child_user_id+ "with parent "+parent_id );
							if (mongo != null && mongo instanceof MongoClient) {
								try {
									mongo.close();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									logger.error("mongo connection exception : " + e.toString());
								}
							}
							if (writechannel != null && writechannel.isOpen()) {
								writechannel.close();
							}
							instagram = null;

						}

					} catch (Exception e) {
						logger.error(" Error in instragram ending  " + e.toString() + " e.getMessage: " + e.getMessage()
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
					/*try {
						Thread.currentThread().stop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						
					}*/
				}
			}

		});

		t1.start();

	}

	public static void saveFollowFollowers(List<UserFeedData> data, Date dateNoOfdaysAgo, DateTimeZone zone, MongoClient mongo,
			MongoOperations mongoOps, JSONObject jsonObject, String FB_COMMENT_COLLECTION, String PERSON_COLLECTION,String PERSON_COLLECTION_TEMP,String FB_COMMENT_COLLECTION_TEMP,
			String RQUEUE_NAME, com.rabbitmq.client.Connection connection1, Map<String, Object> args, String child_user_id,String parent_id,
			String socialSiteUserId, String suffix, String content) {

		Person errorPerson = null;
		JSONObject obj = null;
		Person p = null;
		Person p1 = null;
		Channel writechannel = null;

		for (UserFeedData i : data) {
			try {
				p = new Person();

				p.setUuid((UUID.randomUUID() + ""));
				p.setId(socialSiteUserId + "_" + i.getId() + "_" + child_user_id + "_" +parent_id+"_"+ suffix);
				p.setFullName(i.getUserName());

				p.setInsertDate(new DateTime().toDateTimeISO().toString());

				Date local = new Date();
				long utc = zone.convertLocalToUTC(local.getTime(), false);
				p.setCreatedDate(utc);
				p.setCreatedDateApi(local.toString());
				p.setCreatedDateTimestamp(local.getTime());
			
				p.setPhotoUrl((i.getProfilePictureUrl()) != null ? (i.getProfilePictureUrl()) : "");

				DateTime dttimeCreated = new DateTime(local);
				DateTime dtNewtimeCreated = new DateTime(dttimeCreated.getYear(), dttimeCreated.getMonthOfYear(),
						dttimeCreated.getDayOfMonth(), 0, 0, 0);
				

				p.setUpdateDateTime(dttimeCreated.getMillis());
				p.setUpdatedDate(dtNewtimeCreated.getMillis());
				p.setSocialMediaType((String) jsonObject.get("social_media_type"));
				p.setBio(i.getBio());
				p.setCaption(i.getWebsite());
				p.setUsername(i.getFullName());
				p.setWebsite(i.getWebsite());
				p.setInstragramDiscriminator(suffix);

				p.setChild_user_id((String) jsonObject.get("child_user_id"));
				String fullcontent = null;
				if (suffix == "you") {
					fullcontent = (i.getUserName()) + content;

				} else
					fullcontent = content + (i.getUserName());
				p.setInstagramStaticText(fullcontent);

				p.setReadFlag(0);
				p.setSaveFlag(0);
				p.setDeleteFlag(0);
				p.setErrorFlag(0);
				p.setErrorMessage("");
				p.setNotificationFlag(0);
				
				//setting read and delete flag for filters
				p.setDelete_activity_on_user(0);
				p.setDelete_instagram_comments(0);
				p.setDelete_instagram_posts(0);
				p.setDelete_instagram_user_likes(0);
				p.setRead_activity_on_user(0);
				p.setRead_instagram_comments(0);
				p.setRead_instagram_posts(0);
				p.setRead_instagram_user_likes(0);

				p1 = new Person();
				try {
					if (mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION) != null) {
						p1 = mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION);
						p.setReadFlag(p1.getReadFlag());
						p.setSaveFlag(p1.getSaveFlag());
						p.setDeleteFlag(p1.getDeleteFlag());
						p.setErrorFlag(p1.getErrorFlag());
						p.setErrorMessage("");
						p.setNotificationFlag(p1.getNotificationFlag());
						
						p.setDelete_activity_on_user(p1.getDelete_activity_on_user());
						p.setDelete_instagram_comments(p1.getDelete_instagram_comments());
						p.setDelete_instagram_posts(p1.getDelete_instagram_posts());
						p.setDelete_instagram_user_likes(p1.getDelete_instagram_user_likes());
						p.setRead_activity_on_user(p1.getRead_activity_on_user());
						p.setRead_instagram_comments(p1.getRead_instagram_comments());
						p.setRead_instagram_posts(p1.getRead_instagram_posts());
						p.setRead_instagram_user_likes(p1.getRead_instagram_user_likes());
						
					}
				} catch (Exception e) {
					logger.info("Instagram follow/following flag modification exception.." + e.toString());
				}

				p.setParentId((String) jsonObject.get("parent_id"));
				p.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));

				// synchronized (this) {
				try {

					if (writechannel != null && writechannel.isOpen()) {

						writechannel.close();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block

					logger.info("Exception in closing the channel. " + e.toString());
				}
				//writechannel = connection1.createChannel();
				//writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
				
				//mongoOps.save(p, PERSON_COLLECTION);
				mongoOps.save(p,PERSON_COLLECTION_TEMP);
				p1 = new Person();

				obj = new JSONObject();
				obj.put("uuid", p.getUuid());

				obj.put("socialmediaType", p.getSocialMediaType());
				obj.put("child_user_id", p.getChild_user_id());
				obj.put("parent_id", p.getParentId());

			//	writechannel.basicPublish("", RQUEUE_NAME, null, obj.toString().getBytes());

				// }// if condtion
			} catch (Exception e5) {
				// TODO Auto-generated catch block
				logger.info("Exception in single saveFollowFollowers. " + e5.toString());
				e5.printStackTrace();
			} finally {
				try {
					writechannel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.info("Exception in saveFollowFollowers in closing the writechannel. " + e.toString());
				}
			}
		}// for loop

	}

	public static void saveFeedData(List<MediaFeedData> data, Date dateNoOfdaysAgo, DateTimeZone zone, MongoClient mongo,
			MongoOperations mongoOps, JSONObject jsonObject, String FB_COMMENT_COLLECTION, String PERSON_COLLECTION,String PERSON_COLLECTION_TEMP,String FB_COMMENT_COLLECTION_TEMP,
			String RQUEUE_NAME, com.rabbitmq.client.Connection connection1, Map<String, Object> args, String child_user_id,String parent_id,
			String postSource) {

		Person errorPerson = null;
		JSONObject obj = null;
		Person p = null;
		Person p1 = null;
		Channel writechannel = null;

		for (MediaFeedData i : data) {
			try {
				logger.info("\n instagram worker start processing instagram " + i.getId() + "_" + child_user_id + " of parent is "+parent_id +" is "
						+ (System.currentTimeMillis()));
				DateTime createdPostDate = new DateTime(new Date(Long.parseLong(i.getCreatedTime()) * 1000));
				DateTime dateLimiter = new DateTime(dateNoOfdaysAgo);
				if (createdPostDate.getMillis() >= dateLimiter.getMillis()) {

					p = new Person();
					p.setPostUserId(i.getUser().getId());

					p.setUuid((UUID.randomUUID() + ""));
					p.setId(i.getId() + "_" + child_user_id+"_"+parent_id);
					p.setFullName(i.getUser().getFullName());

					p.setInsertDate(new DateTime().toDateTimeISO().toString());

					p.setCaption((i.getCaption() != null ? i.getCaption().getText() : ""));

					Date local = new Date(Long.parseLong(i.getCreatedTime()) * 1000);
					long utc = zone.convertLocalToUTC(local.getTime(), false);
					p.setCreatedDate(utc);
					p.setCreatedDateApi(local.toString());
					p.setCreatedDateTimestamp(local.getTime());

					Date dtCreated = new Date(Long.parseLong(i.getCreatedTime()) * 1000);
					DateTime dttimeCreated = new DateTime(dtCreated);
					DateTime dtNewtimeCreated = new DateTime(dttimeCreated.getYear(), dttimeCreated.getMonthOfYear(),
							dttimeCreated.getDayOfMonth(), 0, 0, 0);
					p.setPhotoUrl((i.getUser().getProfilePictureUrl()) != null ? (i.getUser().getProfilePictureUrl()) : "");

					p.setLikeCount((long) (i.getLikes().getCount()));
					/*
					 * try { if((i.getComments().getCount())>0)
					 * p.setCommentCount((long) (i.getComments().getCount()));
					 * else p.setCommentCount(0L); } catch (Exception e5) { //
					 * TODO Auto-generated catch block
					 * 
					 * }
					 */
					p.setLink(i.getLink() != null ? i.getLink() : "");
					p.setCommentCount(0L);

					try {

						if (i.getComments() != null & i.getComments().getComments() != null
								& i.getComments().getComments().size() > 0) {
							String uuidCommentId = UUID.randomUUID() + "";
							long j = 0L;

							for (CommentData c : i.getComments().getComments()) {

								FBcomments comment = new FBcomments();
								comment.setUuidComment(uuidCommentId);
								// comment.setPostId(i.getId()
								// +"_"+child_user_id);
								comment.setPostId(p.getId());
								comment.setCommentId((c.getId() != null ? ( c.getId() +"_"+ p.getId()): ""));
								comment.setCommentMessage((c.getText() != null ? c.getText() : ""));
								comment.setCommentFrom((c.getCommentFrom() != null ? c.getCommentFrom().getFullName() : ""));
								comment.setProfileImage((c.getCommentFrom().getProfilePicture() != null ? c.getCommentFrom()
										.getProfilePicture() : ""));
								comment.setCommentedById((c.getCommentFrom().getId() != null ? c.getCommentFrom().getId() : ""));
								comment.setImage("");
								comment.setVideo("");
								comment.setSocialMediaType((String) jsonObject.get("social_media_type"));
								comment.setChildUserId((String) jsonObject.get("child_user_id"));
								comment.setParent_id(parent_id);

								//adding flags to Comments

								comment.setReadFlag(0);
								comment.setSaveFlag(0);
								comment.setDeleteFlag(0);
								comment.setErrorFlag(0);
								comment.setErrorMessage("");
								comment.setNotificationFlag(0);

								FBcomments dbcomment = new FBcomments();
								try {
									
									//if (mongoOps.findById(i.getId()+"_"+child_user_id, Person.class, PERSON_COLLECTION) != null) {
									//	p2 = mongoOps.findById(new Long(i.getId()).toString()+"_"+child_user_id, Person.class, PERSON_COLLECTION);
									if (mongoOps.findById(comment.getCommentId(),  FBcomments.class,  FB_COMMENT_COLLECTION) != null) {
										dbcomment = mongoOps.findById(comment.getCommentId(), FBcomments.class,  FB_COMMENT_COLLECTION);
										comment.setReadFlag(dbcomment.getReadFlag());
										comment.setSaveFlag(dbcomment.getSaveFlag());
										comment.setDeleteFlag(dbcomment.getDeleteFlag());
										comment.setErrorFlag(dbcomment.getErrorFlag());
										comment.setErrorMessage("");
										comment.setNotificationFlag(dbcomment.getNotificationFlag());
									}
								} catch (Exception e) {
									logger.info("Instagrm flag modification exception.."

									+ e.toString());
								}
								//end  of adding flags to comment
								
								//mongoOps.save(comment, FB_COMMENT_COLLECTION);
								mongoOps.save(comment, FB_COMMENT_COLLECTION_TEMP);
							}
							Query query5 = new Query();
							query5.addCriteria(Criteria.where("postId").is(p.getId()));
							j=(mongoOps.count(query5, FB_COMMENT_COLLECTION_TEMP));
							p.setCommentCount(j);
							p.setFbComments(uuidCommentId);

						}
					} catch (Exception e1) {
						logger.info("insta Comments list exception..." + e1.toString());
						// TODO Auto-generated catch block

					}

					p.setImages((i.getImages()));
					p.setVideos(i.getVideos());

					p.setUpdateDateTime(dttimeCreated.getMillis());
					p.setUpdatedDate(dtNewtimeCreated.getMillis());
					p.setSocialMediaType((String) jsonObject.get("social_media_type"));
					p.setPostSource(postSource);
					p.setPostUserId(i.getUser().getId());

					p.setChild_user_id((String) jsonObject.get("child_user_id"));
					p.setReadFlag(0);
					p.setSaveFlag(0);
					p.setDeleteFlag(0);
					p.setErrorFlag(0);
					p.setErrorMessage("");
					p.setNotificationFlag(0);
					//setting read and delete flag for filters
					p.setDelete_activity_on_user(0);
					p.setDelete_instagram_comments(0);
					p.setDelete_instagram_posts(0);
					p.setDelete_instagram_user_likes(0);
					p.setRead_activity_on_user(0);
					p.setRead_instagram_comments(0);
					p.setRead_instagram_posts(0);
					p.setRead_instagram_user_likes(0);

					p1 = new Person();
					try {
						if (mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION) != null) {
							p1 = mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION);
							p.setReadFlag(p1.getReadFlag());
							p.setSaveFlag(p1.getSaveFlag());
							p.setDeleteFlag(p1.getDeleteFlag());
							p.setErrorFlag(p1.getErrorFlag());
							p.setErrorMessage("");
							p.setNotificationFlag(p1.getNotificationFlag());
							
							p.setDelete_activity_on_user(p1.getDelete_activity_on_user());
							p.setDelete_instagram_comments(p1.getDelete_instagram_comments());
							p.setDelete_instagram_posts(p1.getDelete_instagram_posts());
							p.setDelete_instagram_user_likes(p1.getDelete_instagram_user_likes());
							p.setRead_activity_on_user(p1.getRead_activity_on_user());
							p.setRead_instagram_comments(p1.getRead_instagram_comments());
							p.setRead_instagram_posts(p1.getRead_instagram_posts());
							p.setRead_instagram_user_likes(p1.getRead_instagram_user_likes());
							
						}
					} catch (Exception e) {
						logger.info("Instagram flag modification exception.." + e.toString());
					}

					p.setParentId((String) jsonObject.get("parent_id"));
					p.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));
					p.setTotalCommentCount(0);
					// synchronized (this) {
					try {

						if (writechannel != null && writechannel.isOpen()) {

							writechannel.close();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block

						logger.info("Exception in closing the channel. " + e.toString());
					}
					//writechannel = connection1.createChannel();
					//writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
					//mongoOps.save(p, PERSON_COLLECTION);
					mongoOps.save(p, PERSON_COLLECTION_TEMP);
					p1 = new Person();

					obj = new JSONObject();
					obj.put("uuid", p.getUuid());

					obj.put("socialmediaType", p.getSocialMediaType());
					obj.put("child_user_id", p.getChild_user_id());
					obj.put("parent_id", p.getParentId());

				//	writechannel.basicPublish("", RQUEUE_NAME, null, obj.toString().getBytes());
					try {

						if (writechannel != null && writechannel.isOpen()) {

							writechannel.close();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block

						logger.info("Exception in closing the channel. " + e.toString());
					}

				}
				// }// if condtion
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.info("Exception in instagram Worker. " + e.toString());
			}
		}// for loop

	}

}
