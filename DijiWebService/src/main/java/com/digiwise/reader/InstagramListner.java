package com.digiwise.reader;

import hello.DijiWiseResultsArray;
import hello.Greeting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jinstagram.Instagram;
import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.entity.users.feed.UserFeed;
import org.jinstagram.entity.users.feed.UserFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.digiwise.reader.FBcomments;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;

public class InstagramListner {

	// instagram handler
	final static Logger logger = Logger.getLogger(InstagramListner.class);

	public static DijiWiseResultsArray instraGramMain(String parent_id,
			String child_user_id, String access_token,
			String social_media_type, String social_site_user_id,
			DijiWiseResultsArray array) throws InstagramException {

		System.out.println("start of the instagram webservice call: " + new Date());
		
		List<MediaFeedData> mediaList = null;
		List<MediaFeedData> dataFeed = null;
		List<MediaFeedData>  userdataFeed= null;
		String accessTokenValue = null;
		MongoOperations mongoOps = null;
		MongoClient mongo = null;
		String PERSON_COLLECTION = "";
		String FB_COMMENT_COLLECTION="";
		String TWITTER_ENTITY_COLLECTION = "";
		String PERSON_LOGIN_STATUS = "";
		String PERSON_FOLLOWING_FOLLOWS = "";
		Token secretToken = null;
		Channel writechannel = null;
		DateTimeZone zone = DateTimeZone.getDefault();
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-mm-dd hh-mm-ss");
		JSONObject obj = new JSONObject();
		ArrayList arrayGreetings = new ArrayList();
		Person p1 = null;
		ErrorPerson errorPerson = null;
		ErrorPerson p2 = null;
		try {

			Date dateNoOfdaysAgo = new DateTime(new Date()).minusDays(10).toDate();

			logger.info("dateAgo instragram..." + dateNoOfdaysAgo);

			final String DB_NAME = "dijiwisecdndb";
			PERSON_COLLECTION = "dijiwisesocialcollection";
			FB_COMMENT_COLLECTION="dijiwiseComment";
			PERSON_FOLLOWING_FOLLOWS ="dijiwiseFollowFollowing" ;
			PERSON_LOGIN_STATUS = "dijiwiseerrorstatus";
			
			//final String MONGO_HOST = "173.244.67.213";
			final String MONGO_HOST = JavaListener.MONGO_HOST;
			final int MONGO_PORT = Integer.parseInt("27017");
			
			accessTokenValue = access_token;

			try {
				mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
				mongoOps = new MongoTemplate(mongo, DB_NAME);
				secretToken = new Token(accessTokenValue, null);
				logger.info("secretToken instragram..." + secretToken);
			} catch (Exception e) {
				logger.error("Exception: " + e.getMessage() + "--"+ e.getStackTrace() + " e.toString: " + e.toString());
				errorPerson = new ErrorPerson();
				p2= new ErrorPerson();

				errorPerson.setChildId(child_user_id);
				errorPerson.setErrorOccuredDate(new Date().toString());
				errorPerson.setErrorMessage(e.getMessage());
				errorPerson.setUuid((UUID.randomUUID() + ""));

				errorPerson.setSocialSiteUserId(social_site_user_id);
				errorPerson.setErrorMessage(e.getMessage());

				errorPerson.setSocialMediaType(social_media_type);
				errorPerson.setErrorFlag("1");

				mongoOps.save(errorPerson, PERSON_LOGIN_STATUS);
			}

			try {
				if (errorPerson == null) {
				try {
					// Thread.sleep(1000); // 1000 milliseconds is one
					// second.
					Instagram instagram = new Instagram(secretToken);
					System.out.println("Retrive instagram MediaFeed Data : " + new Date());
					MediaFeed mediaFeed = instagram.getUserFeeds();
					System.out.println("End of Retrive instagram MediaFeed Data : " + new Date());
					logger.info("mediaFeed.getData instragram...");
					
					System.out.println("Retrive instagram MediaFeed Liked Data : " + new Date());
					MediaFeed mediaFeedLiked = instagram.getUserLikedMediaFeed();
					System.out.println("End of Retrive instagram MediaFeed Liked Data : " + new Date());
					
					System.out.println("Retrive instagram UserFeed Recent MediaFeed Data : " + new Date());
					MediaFeed userFeed=instagram.getRecentMediaFeed(social_site_user_id);
					System.out.println("End of Retrive instagram UserFeed Recent MediaFeed Data : " + new Date());
					System.out.println("socialSiteUserId::::::::::::::::::"+ social_site_user_id);

					System.out.println("Retrive instagram userFollowedBy Data : " + new Date());
					UserFeed userFollowedBy = instagram.getUserFollowedByList(social_site_user_id);
					System.out.println("End of Retrive instagram userFollowedBy Data : " + new Date());
					
					// instagram.getUserRelationship(userId);

					List<UserFeedData> ulist = userFollowedBy.getUserList();
					System.out.println("Retrive instagram userFollowing Data : " + new Date());
					UserFeed userFollowing = instagram.getUserFollowList(social_site_user_id);
					System.out.println("End Retrive instagram userFollowing Data : " + new Date());
					List<UserFeedData> followList = userFollowing.getUserList();
					mediaList = mediaFeed.getData();
					dataFeed = mediaFeedLiked.getData();
					userdataFeed=userFeed.getData();
					logger.info("Thread  Got data  w.r.t accesstoken :"+ access_token + "\n Thread Name:");
					logger.info("insta testing...");
					mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
					mongoOps = new MongoTemplate(mongo, DB_NAME);

					int countFeed = 0;
					try {
						System.out.println("Start to Save MediaList Data : " + new Date());
						saveFeedData(mediaList, dateNoOfdaysAgo, zone, parent_id,
									 child_user_id, access_token, social_media_type,
									 social_site_user_id, array, mongoOps,
									 PERSON_COLLECTION,FB_COMMENT_COLLECTION, obj, countFeed, arrayGreetings,"OtherFeed");
						System.out.println("End to Save MediaList Data : " + new Date());
					} catch (Exception e0) {
						logger.error("error in Otherfeed instagram..."+e0.getMessage());
					}
					try {
					System.out.println("Start to Save UserLikedMediaFeed Data : " + new Date());
					saveFeedData(dataFeed, dateNoOfdaysAgo, zone, parent_id,
  								 child_user_id, access_token, social_media_type,
								 social_site_user_id, array, mongoOps,
								 PERSON_COLLECTION,FB_COMMENT_COLLECTION, obj, countFeed, arrayGreetings,"UserLikedMediaFeed");
					System.out.println("End to Save UserLikedMediaFeed Data : " + new Date());
					} catch (Exception e1) {
						logger.error("error in UserLikedMediaFeed instagram..."+e1.getMessage());
					}
					try {
					System.out.println("Start to Save UserFeed Recent MediaFeed Data : " + new Date());
					saveFeedData(userdataFeed, dateNoOfdaysAgo, zone, parent_id,
								 child_user_id, access_token, social_media_type,
								 social_site_user_id, array, mongoOps,
								 PERSON_COLLECTION,FB_COMMENT_COLLECTION, obj, countFeed, arrayGreetings,"userfeed");
					System.out.println("End to Save UserFeed Recent MediaFeed Data : " + new Date());
					} catch (Exception e2) {
						logger.error("error in userfeed instagram..."+e2.getMessage());
					}
					
					/*saveFollowedBy(ulist, dateNoOfdaysAgo, zone, mongo,
							mongoOps, obj, FB_COMMENT_COLLECTION,
							PERSON_FOLLOWING_FOLLOWS,  social_site_user_id, arrayGreetings, "you");
					saveFollowedBy(followList, dateNoOfdaysAgo, zone, mongo,
							mongoOps, obj, FB_COMMENT_COLLECTION,
							PERSON_FOLLOWING_FOLLOWS, social_site_user_id, arrayGreetings,"following");*/
					try {
					System.out.println("Start to Save someone is following you : " + new Date());
					saveFollowFollowers(ulist, dateNoOfdaysAgo, zone, mongo,
										mongoOps, obj, FB_COMMENT_COLLECTION,
										PERSON_COLLECTION,social_site_user_id, arrayGreetings,
										child_user_id,parent_id, "you"," is following you.");
					System.out.println("End to Save someone is following you : " + new Date());
					} catch (Exception e3) {
						logger.error("error in following feed  instagram..."+e3.getMessage());
					}
					try {
					System.out.println("Start to Save you are following someone : " + new Date());
					saveFollowFollowers(followList, dateNoOfdaysAgo, zone, mongo,
										mongoOps, obj, FB_COMMENT_COLLECTION,
										PERSON_COLLECTION, social_site_user_id, arrayGreetings,
										child_user_id, parent_id, "following"," You are following ");
					System.out.println("End to Save you are following someone : " + new Date());
					} catch (Exception e4) {
						logger.error("error in followers feed instagram..."+e4.getMessage());
					}
					
					
				} catch (Exception e) {
					logger.error("Exception in instragram main body  " + e.getMessage() + "--"+ " e.tostring:" + e.toString());
					e.printStackTrace();
				}
				if (p2 == null)
				{
					errorPerson = new ErrorPerson();
					errorPerson.setErrorOccuredDate(new Date().toString());
					errorPerson.setChildId(child_user_id);
					
					errorPerson.setUuid((UUID.randomUUID() + ""));
					errorPerson.setInsertDate(new Date().toString());

					errorPerson.setSocialSiteUserId(social_site_user_id);

					errorPerson.setSocialMediaType(social_media_type);

					errorPerson.setErrorFlag("0");
					mongoOps.save(errorPerson, PERSON_LOGIN_STATUS);

				}
			}
		} catch (Exception e) {
				logger.error("Exception: " + e.getMessage() + "--"+ e.getStackTrace() + " e.tostring:" + e.toString());
				
			} finally {

			}
			logger.info("Thread  ending ");
		} catch (Exception e) {
			logger.error(" Error in instragram ending  " + e.toString()+ " e.getMessage: " + e.getMessage() + " e.toSting: "+ e.toString());
		} finally {
			if (mongo != null && mongo instanceof MongoClient) {
				try {
					mongo.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		array.setList(arrayGreetings);
		return array;
	}

	public static void saveFeedData(List<MediaFeedData> mediaList,
			Date dateNoOfdaysAgo, DateTimeZone zone, String parent_id,
			String child_user_id, String access_token,
			String social_media_type, String social_site_user_id,
			DijiWiseResultsArray array, MongoOperations mongoOps,
			String PERSON_COLLECTION,String FB_COMMENT_COLLECTION, JSONObject obj, int countFeed,
			ArrayList arrayGreetings, String postSource) {

		for (MediaFeedData i : mediaList) {

			DateTime createdPostDate = new DateTime(new Date(Long.parseLong(i.getCreatedTime()) * 1000));
			DateTime dateLimiter = new DateTime(dateNoOfdaysAgo);
			if (createdPostDate.getMillis() >= dateLimiter.getMillis()) {
				logger.info("###############Start#############" + i.getId());
				logger.info("Thread started reading post with id : "+ i.getId());
				Person p = new Person();

				p.setUuid((UUID.randomUUID() + ""));
				p.setId(i.getId()+"_"+child_user_id+"_"+parent_id);
				// Changes by Sushant - 09-11-2015
			    //p.setFullName(i.getUser().getFullName());
			    String fullName = i.getUser().getFullName();
			    String userName = i.getUser().getUserName();
			    if (fullName.equals("") || fullName == null) {
			     p.setFullName(userName);
			    } else {
			     p.setFullName(fullName);
			    }

				p.setInsertDate(new DateTime().toDateTimeISO().toString());

				p.setCaption((i.getCaption() != null ? i.getCaption().getText(): ""));

				Date local = new Date(Long.parseLong(i.getCreatedTime()) * 1000);
				long utc = zone.convertLocalToUTC(local.getTime(), false);
				p.setCreatedDate(utc);
				p.setCreatedDateApi(local.toString());
				p.setCreatedDateTimestamp(local.getTime());

				logger.info("Check Same Post data : ");
				
				Date dtCreated = new Date(Long.parseLong(i.getCreatedTime()) * 1000);
				DateTime dttimeCreated = new DateTime(dtCreated);
				DateTime dtNewtimeCreated = new DateTime(dttimeCreated.getYear(), dttimeCreated.getMonthOfYear(), dttimeCreated.getDayOfMonth(), 0, 0, 0);
				p.setPhotoUrl((i.getUser().getProfilePictureUrl()) != null ? (i.getUser().getProfilePictureUrl()) : "");

				// (i.getName() != null ? i.getName() : ""),
				p.setLikeCount((long) (i.getLikes().getCount()));
				/*try {
					if((i.getComments().getCount())>0)
					p.setCommentCount((long) (i.getComments().getCount()));
					else
						p.setCommentCount(0L);
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					
				}*/
				// i.getPicture(),
				p.setImages((i.getImages()));
				p.setVideos(i.getVideos());
				// (i.getLink() != null ? i.getLink() : ""),
				// p.setUpdatedDate(p.getCreatedDate());
				p.setUpdateDateTime(dttimeCreated.getMillis());
				p.setUpdatedDate(dtNewtimeCreated.getMillis());
				p.setSocialMediaType(social_media_type);
				p.setPostSource(postSource);
				p.setPostUserId(i.getUser().getId());
				p.setCommentCount(0L);
				try {

					if (i.getComments() != null & i.getComments().getComments() != null & i.getComments().getComments().size() > 0) {
						String uuidCommentId = UUID.randomUUID() + "";
						long j = 0L;

						for (CommentData c : i.getComments().getComments()) {

							try {
								FBcomments comment = new FBcomments();
								comment.setUuidComment(uuidCommentId);
								comment.setPostId(p.getId());
								comment.setCommentId((c.getId() != null ? ( c.getId() +"_"+ p.getId()) : ""));
								comment.setCommentMessage((c.getText() != null ? c.getText() : ""));
								comment.setCommentFrom((c.getCommentFrom() != null ? c.getCommentFrom().getFullName() : ""));
								comment.setProfileImage((c.getCommentFrom().getProfilePicture() != null ? c.getCommentFrom().getProfilePicture() : ""));
								comment.setCommentedById((c.getCommentFrom().getId() != null ? c.getCommentFrom().getId() : ""));
								comment.setImage("");
								comment.setVideo("");
								comment.setSocialMediaType(social_media_type);
								comment.setChildUserId(child_user_id);
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
									logger.info("Twitter flag modification exception.."+ e.toString());
								}
								//end  of adding flags to comment
							
								mongoOps.save(comment, FB_COMMENT_COLLECTION);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								logger.info("insta Comments list exception..." + e.toString());
							}
						}
						Query query5 = new Query();
						query5.addCriteria(Criteria.where("postId").is(p.getId()));
						j=(mongoOps.count(query5, FB_COMMENT_COLLECTION));
						p.setCommentCount(j);
						p.setFbComments(uuidCommentId);
					}
				} catch (Exception e1) {
					logger.info("insta Comments list exception..." + e1.toString());
				}

				p.setChild_user_id(child_user_id);
				p.setTotalCommentCount(0);
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

				Person p1 = new Person();
				try {
					if (mongoOps.findById(p.getId(), Person.class,PERSON_COLLECTION) != null) {
						p1 = mongoOps.findById(p.getId(), Person.class,	PERSON_COLLECTION);
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
					logger.info("Instagram flag modification exception.."+ e.toString());
				}
				p.setParentId(parent_id);
				p.setSocialSiteUserId(social_site_user_id);

				
				logger.info("New post with Data: " + p.toString()+ " for social type:" + p.getSocialMediaType());

				mongoOps.save(p, PERSON_COLLECTION);
				obj = new JSONObject();
				obj.put("id", p.getId());
				obj.put("uuid", p.getUuid());
				/*
				 * obj.put("apikey", p.getApiKey()); obj.put("deviceToken",
				 * p.getDeviceToken()); obj.put("deviceType",
				 * p.getDeviceType()); obj.put("registeredId",
				 * p.getRegisterId());
				 */
				obj.put("socialmediaType", p.getSocialMediaType());
				obj.put("child_user_id", p.getChild_user_id());
				obj.put("parent_id", p.getParentId());
				logger.info("Json object that will be sent to response queue :    "+ obj.toJSONString());
				countFeed++;
				Greeting result = new Greeting();
				result.setId((String) obj.get("id"));
				result.setUuid((String) obj.get("uuid"));
				result.setChild_user_id((String) obj.get("child_user_id"));
				result.setParent_id((String) obj.get("parent_id"));
				result.setSocialmediaType((String) obj.get("socialmediaType"));
				arrayGreetings.add(result);

				// }//else

				logger.info("###############End#############" + i.getId());

			}// if condtion

		}// for loop
	}
	
	public static void saveFollowFollowers(List<UserFeedData> data,
			Date dateNoOfdaysAgo, DateTimeZone zone, MongoClient mongo,
			MongoOperations mongoOps, JSONObject jsonObject,
			String FB_COMMENT_COLLECTION, String PERSON_COLLECTION,
			String socialSiteUserId,ArrayList arrayGreetings, String child_user_id,String parent_id,String suffix,String content) throws IOException {

		Person errorPerson = null;
		JSONObject obj = null;
		Person p = null;
		Person p1 = null;
		Channel writechannel = null;

		for (UserFeedData i : data) {

				try {
					p = new Person();

					p.setUuid((UUID.randomUUID() + ""));
					p.setId(socialSiteUserId+"_"+i.getId()+"_"+child_user_id+"_"+parent_id+"_"+suffix);
					
					// Changes by Sushant - 09-11-2015
				    // p.setFullName(i.getUserName());
				    String fullName = i.getFullName();
				    String userName = i.getUserName();
				    if (fullName.equals("") || fullName == null) {
				     p.setFullName(userName);
				    } else {
				     p.setFullName(fullName);
				    }

					p.setInsertDate(new DateTime().toDateTimeISO().toString());

					Date local = new Date();
					long utc = zone.convertLocalToUTC(local.getTime(), false);
					p.setCreatedDate(utc);
					p.setCreatedDateApi(local.toString());
					p.setCreatedDateTimestamp(local.getTime());
					p.setPhotoUrl((i.getProfilePictureUrl()) != null ? (i.getProfilePictureUrl()) : "");
					
					DateTime dttimeCreated = new DateTime(local);
					DateTime dtNewtimeCreated = new DateTime(dttimeCreated.getYear(), dttimeCreated.getMonthOfYear(), dttimeCreated.getDayOfMonth(), 0, 0, 0);

					p.setUpdateDateTime(dttimeCreated.getMillis());
					p.setUpdatedDate(dtNewtimeCreated.getMillis());
					p.setSocialMediaType((String) jsonObject.get("social_media_type"));
					//p.setBio(i.getBio());
					p.setCaption(i.getWebsite());
					// Changes by Sushant
					// p.setUsername(i.getFullName());
					p.setUsername(i.getUserName());
					p.setWebsite(i.getWebsite());
					p.setInstragramDiscriminator(suffix);
					p.setTotalCommentCount(0);
					String fullcontent=null;
					if(suffix=="you"){
						fullcontent=(i.getUserName())+content;
					}
					else {
						fullcontent=content+(i.getUserName());
					}
					p.setInstagramStaticText(fullcontent);

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
						logger.info("Instagram follow/following flag modification exception.."+ e.toString());
					}

					p.setParentId((String) jsonObject.get("parent_id"));
					p.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));

					mongoOps.save(p, PERSON_COLLECTION);
					obj = new JSONObject();
					obj.put("id", p.getId());
					obj.put("uuid", p.getUuid());
					/*
					 * obj.put("apikey", p.getApiKey()); obj.put("deviceToken",
					 * p.getDeviceToken()); obj.put("deviceType",
					 * p.getDeviceType()); obj.put("registeredId",
					 * p.getRegisterId());
					 */
					
					Greeting result = new Greeting();
					result.setId((String) obj.get("id"));
					result.setUuid((String) obj.get("uuid"));
					
					arrayGreetings.add(result);


					// }// if condtion
				} catch (Exception e) {
					
				}
		}// for loop
		
	}

}
