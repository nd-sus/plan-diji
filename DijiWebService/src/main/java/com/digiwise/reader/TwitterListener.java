package com.digiwise.reader;

import hello.DijiWiseResultsArray;
import hello.Greeting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import twitter4j.DirectMessage;
import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import com.mongodb.MongoClient;

public class TwitterListener {
	// facebook handler
	final static Logger logger = Logger.getLogger(TwitterListener.class);

	public static DijiWiseResultsArray getTwitterTweets(String parent_id, String child_user_id, String access_token,
			String oauth_token_secret, String social_media_type, String social_site_user_id, DijiWiseResultsArray array) {

		logger.info("start of the twitter webservice call: " + (System.currentTimeMillis()));

		MongoOperations mongoOps = null;
		List<Status> favorites = null;
		List<Status> userTimeLine = null;
		ResponseList<DirectMessage> directMessages = null;

		String PERSON_COLLECTION = "";
		String FB_COMMENT_COLLECTION = "";
		String PERSON_FOLLOWING_FOLLOWS = "";
		String TWITTER_ENTITY_COLLECTION = "";
		String PERSON_LOGIN_STATUS = "";
		com.rabbitmq.client.ConnectionFactory factory1 = null;
		MongoClient mongo = null;
		DateTimeZone zone = DateTimeZone.getDefault();

		String OAuthConsumerKey = null;
		String OAuthConsumerSecret = null;
		String OAuthAccessTokenValue = null;
		String OAuthAccessTokenSecret = null;
		Twitter twitter = null;

		JSONObject obj = null;
		Person p = null;
		ErrorPerson  p1 = null;
		String accessTokenValue = null;
		ErrorPerson errorPerson = null;
		Person p2 = null;
		ArrayList<Greeting> arrayGreetings = new ArrayList();

		try {

			final String DB_NAME = "dijiwisecdndb";
			PERSON_COLLECTION = "dijiwisesocialcollection";
			FB_COMMENT_COLLECTION = "dijiwiseComment";
			PERSON_FOLLOWING_FOLLOWS = "dijiwiseFollowFollowing";
			PERSON_LOGIN_STATUS = "dijiwiseerrorstatus";

			
			//final String MONGO_HOST = "173.244.67.213";
			final String MONGO_HOST = JavaListener.MONGO_HOST;
			final int MONGO_PORT = Integer.parseInt("27017");

			OAuthConsumerKey = "arW0EywAp2gQn0xn2SNzpGEZ0";
			OAuthConsumerSecret = "OG9EcoIpPeltoLR4Ia6h01QWca73GGlySAC42E6s6fwRn8vCC5";
			OAuthAccessTokenValue = access_token;
			OAuthAccessTokenSecret = oauth_token_secret;
			

			try {
				if (mongo != null && mongo instanceof MongoClient) {
					try {
						mongo.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("mongo connection exception : " + e.toString());
					}
				}
				
				
				//Your Twitter App's Consumer Key
		        String consumerKey = "arW0EywAp2gQn0xn2SNzpGEZ0";
		 
		        //Your Twitter App's Consumer Secret
		        String consumerSecret = "OG9EcoIpPeltoLR4Ia6h01QWca73GGlySAC42E6s6fwRn8vCC5";
		 
		        //Your Twitter Access Token
		       // String accessToken = "XXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		        String accessToken =access_token;
		        //Your Twitter Access Token Secret
		        //String accessTokenSecret = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		        String accessTokenSecret = oauth_token_secret;
		        //Instantiate a re-usable and thread-safe factory
		        TwitterFactory twitterFactory = new TwitterFactory();
		 
		        //Instantiate a new Twitter instance
		         twitter = twitterFactory.getInstance();
		 
		        //setup OAuth Consumer Credentials
		        twitter.setOAuthConsumer(consumerKey, consumerSecret);
		 
		        //setup OAuth Access Token
		        twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));
		        favorites = twitter.getFavorites();//getHomeTimeline();
				userTimeLine = twitter.getUserTimeline();
				// directMessages=twitter.getDirectMessages();

			} catch (Exception e) {
				logger.error("Exception: " + e.getMessage() + "--" + e.getStackTrace() + " e.tostring: " + e.toString());
				errorPerson = new ErrorPerson();
				p1 = new ErrorPerson();

				errorPerson.setChildId(child_user_id);
				errorPerson.setErrorOccuredDate(new Date().toString());
				errorPerson.setErrorMessage(e.getMessage());
				errorPerson.setUuid((UUID.randomUUID() + ""));

				errorPerson.setSocialSiteUserId(social_site_user_id);
				errorPerson.setErrorMessage(e.getMessage());

				errorPerson.setSocialMediaType(social_media_type);
				if(e.getMessage().contains("429:Returned in API")){
					errorPerson.setErrorFlag("0");
					}
					else{
						errorPerson.setErrorFlag("1");	
					}
				logger.info("\n\n\n\nErrorneous twitter accesstoken :" + access_token + "\n Thread Name:");
				mongoOps.save(errorPerson, PERSON_LOGIN_STATUS);
			}
			try {
				if (errorPerson == null) {
					logger.info("Thread  Got data  w.r.t accesstoken :" + access_token + "\n Thread Name:");
					int countFeed = 0;

					try {
						long cursor = -1;
						long cursorFollowing = -1;
						PagableResponseList<User> followers, following;
						ArrayList<User> followersFinalList = new ArrayList();
						ArrayList<User> followingFinalList = new ArrayList();

						do {
							followers = twitter.getFollowersList(twitter.getScreenName(), cursor);
							

							followersFinalList.addAll(followers);
							System.out.println("jdsklfjskdfjsdklfjskdjfksadjfsdajf" + followersFinalList.size());

						} while ((cursor = followers.getNextCursor()) != 0);
						savefollowFollowing(followersFinalList, PERSON_COLLECTION, mongoOps, zone, social_site_user_id,
								social_media_type, child_user_id, parent_id, arrayGreetings, "followers");

						

						do {
							following = twitter.getFriendsList(twitter.getScreenName(), cursorFollowing);
							
							followingFinalList.addAll(following);
							System.out.println("jdsklfjskdfjsdklfjskdjfksadjfsdajf" + followersFinalList.size());

						} while ((cursorFollowing = following.getNextCursor()) != 0);

						savefollowFollowing(followingFinalList, PERSON_COLLECTION, mongoOps, zone, social_site_user_id,
								social_media_type, child_user_id, parent_id, arrayGreetings, "following");
					} catch (Exception e2) {
						// TODO Auto-generated catch block
						logger.error("STTWITTER follwers/following exception  for " + twitter.getScreenName());
					}

					

					for (Status i : favorites) {
						logger.info("Webservice Twitter processing " + i.getId() + " is " + (System.currentTimeMillis()));

						p = new Person();
						p.setUuid((UUID.randomUUID() + ""));
						p.setId(i.getId() + "_" + child_user_id+"_"+parent_id);

						p.setInsertDate((new DateTime().toDateTimeISO().toString()));
						try {
							// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
							if (i.getURLEntities() != null) {

								p.setTwitterUrls(i.getURLEntities());
							}

						} catch (Exception e1) {
							logger.info("fb Comments list exception... 1 " + e1.toString());
							// TODO Auto-generated catch block

						}

						try {
							// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
							if (i.getMediaEntities() != null) {

								p.setTwitterMedias(i.getMediaEntities());

							}
						} catch (Exception e1) {
							logger.info("fb Comments list exception... 2 " + e1.toString());
							// TODO Auto-generated catch block

						}

						p.setMessage(i.getText());
						p.setCommentCount(0L);
						try {
							//TODO Senthil - Commented 
//							if ((twitter.getRetweets(i.getId()) != null) && !(twitter.getRetweets(i.getId()).isEmpty())) {
//								String uuidCommentId = UUID.randomUUID() + "";
//								long j = 0L;
//								for (Status c : twitter.getRetweets(i.getId())) {
//
//									try {
//										FBcomments comment = new FBcomments();
//
//										// comment.setPostId(i.getId()
//										// +"_"+child_user_id);
//										comment.setPostId(p.getId());
//										comment.setCommentId((c.getId() > 0 ? ( c.getId() +"_"+ p.getId()) : ""));
//										comment.setCommentMessage((c.getText() != null ? c.getText() : ""));
//										comment.setCommentFrom((c.getUser().getName() != null ? c.getUser().getName() != null ? c
//												.getUser().getName() : "" : ""));
//										comment.setProfileImage((c.getUser().getMiniProfileImageURL() != null ? c.getUser()
//												.getMiniProfileImageURL() : ""));
//										comment.setSocialMediaType(social_media_type);
//										comment.setChildUserId(child_user_id);
//										comment.setParent_id(parent_id);
//										
//										//adding flags to Comments
//
//										comment.setReadFlag(0);
//										comment.setSaveFlag(0);
//										comment.setDeleteFlag(0);
//										comment.setErrorFlag(0);
//										comment.setErrorMessage("");
//										comment.setNotificationFlag(0);
//
//										FBcomments dbcomment = new FBcomments();
//										try {
//											
//											//if (mongoOps.findById(i.getId()+"_"+child_user_id, Person.class, PERSON_COLLECTION) != null) {
//											//	p2 = mongoOps.findById(new Long(i.getId()).toString()+"_"+child_user_id, Person.class, PERSON_COLLECTION);
//											if (mongoOps.findById(comment.getCommentId(),  FBcomments.class,  FB_COMMENT_COLLECTION) != null) {
//												dbcomment = mongoOps.findById(comment.getCommentId(), FBcomments.class,  FB_COMMENT_COLLECTION);
//												comment.setReadFlag(dbcomment.getReadFlag());
//												comment.setSaveFlag(dbcomment.getSaveFlag());
//												comment.setDeleteFlag(dbcomment.getDeleteFlag());
//												comment.setErrorFlag(dbcomment.getErrorFlag());
//												comment.setErrorMessage("");
//												comment.setNotificationFlag(dbcomment.getNotificationFlag());
//											}
//										} catch (Exception e) {
//											logger.info("Twitter comments modification exception.."
//
//											+ e.toString());
//										}
//										//end  of adding flags to comment
//
//										mongoOps.save(comment, FB_COMMENT_COLLECTION);
//									} catch (Exception e) {
//										logger.info("Twitter comments modification exception.."
//
//											+ e.toString());
//									}
//									
//								}
//
//								Query query5 = new Query();
//								query5.addCriteria(Criteria.where("postId").is(p.getId()));
//								j=(mongoOps.count(query5, FB_COMMENT_COLLECTION));
//								p.setCommentCount(j);
//								p.setFbComments(uuidCommentId);
//
//								
//							}
						} catch (Exception e1) {
							logger.info("fb Comments list exception... 3 " + e1.toString());
							// TODO Auto-generated catch block

						}

						p.setTwitterScopes(i.getScopes());
						p.setTwitterIsFavoritedByMe(i.isFavorited());
						p.setTwitterIsRetwettedByMe(i.isRetweetedByMe());
						p.setTwitterIsPossiblySensitive(i.isPossiblySensitive());

						Date local = i.getCreatedAt();
						long utc = zone.convertLocalToUTC(local.getTime(), false);
						p.setCreatedDate(utc);
						p.setCreatedDateApi(i.getCreatedAt().toString());
						p.setCreatedDateTimestamp(local.getTime());

						DateTime retriveDate = new DateTime(i.getCreatedAt());
						DateTime sendingDate = new DateTime(retriveDate.getYear(), retriveDate.getMonthOfYear(),
								retriveDate.getDayOfMonth(), 0, 0, 0);
						p.setUpdatedDate(sendingDate.getMillis());
						p.setUpdateDateTime(retriveDate.getMillis());
						p.setName((i.getUser().getName() != null ? i.getUser().getName() : ""));

						try {
							if (i.getFavoriteCount() > 0) {

								p.setLikeCount((long) i.getFavoriteCount());

							} else {
								p.setLikeCount(0L);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							logger.error("Like count not exception...." + e.toString());

						}

						p.setPhotoUrl((i.getUser().getMiniProfileImageURL()) != null ? (i.getUser().getMiniProfileImageURL())
								: "");

						p.setSocialMediaType(social_media_type);

						p.setChild_user_id(child_user_id);
						
						p.setPostSource("hometimeline");

						p.setReadFlag(0);
						p.setSaveFlag(0);
						p.setDeleteFlag(0);
						p.setErrorFlag(0);
						p.setErrorMessage("");
						p.setNotificationFlag(0);
						//adding read and delte flag for filters
						p.setDelete_users_favorites(0);
						p.setDelete_user_tweets(0);
						p.setDelete_new_followers(0);
						p.setDelete_new_followings(0);
						p.setRead_users_favorites(0);
						p.setRead_user_tweets(0);
						p.setRead_new_followers(0);
						p.setRead_new_followings(0);

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
								

								p.setDelete_users_favorites(p2.getDelete_users_favorites());
								p.setDelete_user_tweets(p2.getDelete_user_tweets());
								p.setDelete_new_followers(p2.getDelete_new_followers());
								p.setDelete_new_followings(p2.getDelete_new_followings());
								p.setRead_users_favorites(p2.getRead_users_favorites());
								p.setRead_user_tweets(p2.getRead_user_tweets());
								p.setRead_new_followers(p2.getRead_new_followers());
								p.setRead_new_followings(p2.getRead_new_followings());
								
								
							}
						} catch (Exception e) {
							logger.info("Webservice Twitter flag modification exception.."

							+ e.toString());
						}

						p.setParentId(parent_id);
						p.setSocialSiteUserId(social_site_user_id);

						mongoOps.save(p, PERSON_COLLECTION);
						logger.info("After save  twitter post timeStamp  " + p.getId() + " is" + (System.currentTimeMillis()));
						obj = new JSONObject();
						obj.put("id", (p.getId() + ""));
						obj.put("uuid", p.getUuid());

						obj.put("socialmediaType", p.getSocialMediaType());
						obj.put("child_user_id", p.getChild_user_id());
						obj.put("parent_id", p.getParentId());

						countFeed++;
						Greeting result = new Greeting();
						result.setId((String) obj.get("id"));
						result.setUuid((String) obj.get("uuid"));
						result.setChild_user_id((String) obj.get("child_user_id"));
						result.setParent_id((String) obj.get("parent_id"));
						result.setSocialmediaType((String) obj.get("socialmediaType"));
						System.out.println("$$$$$$$$$$$$countFeed$$$$$$$$$" + countFeed);
						arrayGreetings.add(result);
						System.out.println("\n arrayGreetings**********" + arrayGreetings.size());

						logger.info("###############End#############" + i.getId());

					}

					// user time line

					for (Status i : userTimeLine) {
						logger.info("Webservice Twitter processing " + i.getId() + " is " + (System.currentTimeMillis()));

						p = new Person();
						p.setUuid((UUID.randomUUID() + ""));
						p.setId(i.getId() + "_" + child_user_id+"_"+parent_id);

						p.setInsertDate((new DateTime().toDateTimeISO().toString()));
						try {
							// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
							if (i.getURLEntities() != null) {

								p.setTwitterUrls(i.getURLEntities());
							}

						} catch (Exception e1) {
							logger.info("fb Comments list exception... 4 " + e1.toString());
							// TODO Auto-generated catch block

						}

						try {
							// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
							if (i.getMediaEntities() != null) {

								p.setTwitterMedias(i.getMediaEntities());

							}
						} catch (Exception e1) {
							logger.info("fb Comments list exception... 5 " + e1.toString());
							// TODO Auto-generated catch block

						}

						p.setMessage(i.getText());
						p.setCommentCount(0L);
						try {
							// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
							//TODO Senthil - Commented
//							if ((twitter.getRetweets(i.getId()) != null) && !(twitter.getRetweets(i.getId()).isEmpty())) {
//								String uuidCommentId = UUID.randomUUID() + "";
//								long j = 0L;
//								for (Status c : twitter.getRetweets(i.getId())) {
//
//									try {
//										FBcomments comment = new FBcomments();
//
//										// comment.setPostId(i.getId()
//										// +"_"+child_user_id);
//										comment.setPostId(p.getId());
//										comment.setCommentId((c.getId() > 0 ?( c.getId() +"_"+ p.getId()) : ""));
//										comment.setCommentMessage((c.getText() != null ? c.getText() : ""));
//										comment.setCommentFrom((c.getUser().getName() != null ? c.getUser().getName() != null ? c
//												.getUser().getName() : "" : ""));
//										comment.setProfileImage((c.getUser().getMiniProfileImageURL() != null ? c.getUser()
//												.getMiniProfileImageURL() : ""));
//
//										comment.setSocialMediaType(social_media_type);
//										comment.setChildUserId(child_user_id);
//										comment.setParent_id(parent_id);
//										
//										//adding flags to Comments
//
//										comment.setReadFlag(0);
//										comment.setSaveFlag(0);
//										comment.setDeleteFlag(0);
//										comment.setErrorFlag(0);
//										comment.setErrorMessage("");
//										comment.setNotificationFlag(0);
//
//										FBcomments dbcomment = new FBcomments();
//										try {
//											
//											//if (mongoOps.findById(i.getId()+"_"+child_user_id, Person.class, PERSON_COLLECTION) != null) {
//											//	p2 = mongoOps.findById(new Long(i.getId()).toString()+"_"+child_user_id, Person.class, PERSON_COLLECTION);
//											if (mongoOps.findById(comment.getCommentId(),  FBcomments.class,  FB_COMMENT_COLLECTION) != null) {
//												dbcomment = mongoOps.findById(comment.getCommentId(), FBcomments.class,  FB_COMMENT_COLLECTION);
//												comment.setReadFlag(dbcomment.getReadFlag());
//												comment.setSaveFlag(dbcomment.getSaveFlag());
//												comment.setDeleteFlag(dbcomment.getDeleteFlag());
//												comment.setErrorFlag(dbcomment.getErrorFlag());
//												comment.setErrorMessage("");
//												comment.setNotificationFlag(dbcomment.getNotificationFlag());
//											}
//										} catch (Exception e) {
//											logger.info("Twitter comments modification exception.."
//
//											+ e.toString());
//										}
//										//end  of adding flags to comment
//
//										mongoOps.save(comment, FB_COMMENT_COLLECTION);
//									} catch (Exception e) {
//										logger.info("Twitter comments modification exception.."
//
//											+ e.toString());
//									}
//									
//								}
//
//								Query query5 = new Query();
//								query5.addCriteria(Criteria.where("postId").is(p.getId()));
//								j=(mongoOps.count(query5, FB_COMMENT_COLLECTION));
//								p.setCommentCount(j);
//								p.setFbComments(uuidCommentId);
//
//								
//							}
						} catch (Exception e1) {
							logger.info("fb Comments list exception... 6 " + e1.toString());
							// TODO Auto-generated catch block

						}

						p.setTwitterScopes(i.getScopes());
						p.setTwitterIsFavoritedByMe(i.isFavorited());
						
						p.setTwitterIsPossiblySensitive(i.isPossiblySensitive());
						p.setTwitterIsRetwettedByMe(i.isRetweetedByMe());

						Date local = i.getCreatedAt();
						long utc = zone.convertLocalToUTC(local.getTime(), false);
						p.setCreatedDate(utc);
						p.setCreatedDateApi(i.getCreatedAt().toString());
						p.setCreatedDateTimestamp(local.getTime());

						DateTime retriveDate = new DateTime(i.getCreatedAt());
						DateTime sendingDate = new DateTime(retriveDate.getYear(), retriveDate.getMonthOfYear(),
								retriveDate.getDayOfMonth(), 0, 0, 0);
						p.setUpdatedDate(sendingDate.getMillis());
						p.setUpdateDateTime(retriveDate.getMillis());
						p.setName((i.getUser().getName() != null ? i.getUser().getName() : ""));

						try {
							if (i.getFavoriteCount() > 0) {

								p.setLikeCount((long) i.getFavoriteCount());

							} else {
								p.setLikeCount(0L);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							logger.error("Like count not exception...." + e.toString());

						}

						p.setPhotoUrl((i.getUser().getMiniProfileImageURL()) != null ? (i.getUser().getMiniProfileImageURL())
								: "");

						p.setSocialMediaType(social_media_type);

						p.setChild_user_id(child_user_id);
						
						p.setPostSource("usertimeline");

						p.setReadFlag(0);
						p.setSaveFlag(0);
						p.setDeleteFlag(0);
						p.setErrorFlag(0);
						p.setErrorMessage("");
						p.setNotificationFlag(0);
						//adding read and delte flag for filters
						p.setDelete_users_favorites(0);
						p.setDelete_user_tweets(0);
						p.setDelete_new_followers(0);
						p.setDelete_new_followings(0);
						p.setRead_users_favorites(0);
						p.setRead_user_tweets(0);
						p.setRead_new_followers(0);
						p.setRead_new_followings(0);

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
								

								p.setDelete_users_favorites(p2.getDelete_users_favorites());
								p.setDelete_user_tweets(p2.getDelete_user_tweets());
								p.setDelete_new_followers(p2.getDelete_new_followers());
								p.setDelete_new_followings(p2.getDelete_new_followings());
								p.setRead_users_favorites(p2.getRead_users_favorites());
								p.setRead_user_tweets(p2.getRead_user_tweets());
								p.setRead_new_followers(p2.getRead_new_followers());
								p.setRead_new_followings(p2.getRead_new_followings());
								
							}
						} catch (Exception e) {
							logger.info("Webservice Twitter flag modification exception.."

							+ e.toString());
						}

						p.setParentId(parent_id);
						p.setSocialSiteUserId(social_site_user_id);

						mongoOps.save(p, PERSON_COLLECTION);
						logger.info("After save  twitter post timeStamp  " + p.getId() + " is" + (System.currentTimeMillis()));
						obj = new JSONObject();
						obj.put("id", (p.getId() + ""));
						obj.put("uuid", p.getUuid());

						obj.put("socialmediaType", p.getSocialMediaType());
						obj.put("child_user_id", p.getChild_user_id());
						obj.put("parent_id", p.getParentId());

						countFeed++;
						Greeting result = new Greeting();
						result.setId((String) obj.get("id"));
						result.setUuid((String) obj.get("uuid"));
						result.setChild_user_id((String) obj.get("child_user_id"));
						result.setParent_id((String) obj.get("parent_id"));
						result.setSocialmediaType((String) obj.get("socialmediaType"));
						System.out.println("$$$$$$$$$$$$countFeed$$$$$$$$$" + countFeed);
						arrayGreetings.add(result);
						System.out.println("\n arrayGreetings**********" + arrayGreetings.size());

						logger.info("###############End#############" + i.getId());

					}

				}// end of if
				if (p1 == null)

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
			} finally {

			}
			logger.info("Thread  ending ");
		} catch (Exception e) {
			logger.error("Exception:" + e.getMessage());

			if (mongo != null && mongo instanceof MongoClient) {
				try {
					mongo.close();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error("Exception:" + e1.getMessage());
				}
			}
		} finally {
			if (mongo != null && mongo instanceof MongoClient) {
				try {
					mongo.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		array.setList(arrayGreetings);
		return array;

	}

	public static void savefollowFollowing(ArrayList<User> data, String PERSON_COLLECTION,

	MongoOperations mongoOps, DateTimeZone zone, String social_site_user_id, String social_media_type, String child_user_id,
			String parent_id, ArrayList<Greeting> arrayGreetings, String suffix) {

		try {
			for (User i : data) {

				logger.info("STTWITTER start processing " + i.getId() + " is "

				+ (System.currentTimeMillis()));
				/*
				 * logger.info("###############Start#############" + i.getId());
				 * logger.info("Thread started reading post with id : " +
				 * i.getId() + "   updatged ptime " );
				 * logger.info("Twitter testing...");
				 */
				Person p = new Person();
				p.setUuid((UUID.randomUUID() + ""));
				p.setId(social_site_user_id + "_" + i.getId() + "_" + child_user_id + "_"+parent_id+"_" + suffix);

				p.setInsertDate((new DateTime().toDateTimeISO().toString()));

				p.setMessage(i.getDescription());
				p.setTotalCommentCount(0);

				Date local = i.getCreatedAt();
				long utc = zone.convertLocalToUTC(local.getTime(), false);
				p.setCreatedDate(utc);
				p.setCreatedDateApi(i.getCreatedAt().toString());
				p.setCreatedDateTimestamp(local.getTime());

				DateTime retriveDate = new DateTime(i.getCreatedAt());
				// DateTime retriveDate = new DateTime(i.getCreatedAt());
				DateTime sendingDate = new DateTime(retriveDate.getYear(), retriveDate.getMonthOfYear(),
						retriveDate.getDayOfMonth(), 0, 0, 0);
				// p.setUpdatedDate(sendingDate.getMillis());
				p.setUpdatedDate(java.lang.System.currentTimeMillis() - 86400000);
				p.setUpdateDateTime(retriveDate.getMillis());
				p.setName(i.getName());
				p.setTwitterUserFavouriteCount(i.getFavouritesCount());
				p.setTwitterUserFollowersCount(i.getFollowersCount());
				p.setTwitterUserFriendsCount(i.getFriendsCount());
				p.setTwitterUserIsVerifiedCelebrity(i.isVerified());

				p.setTwitterDiscriminator(suffix);

				p.setPhotoUrl(i.getProfileImageURL());

				p.setSocialMediaType(social_media_type);

				p.setChild_user_id(child_user_id);

				p.setReadFlag(0);
				p.setSaveFlag(0);
				p.setDeleteFlag(0);
				p.setErrorFlag(0);
				p.setErrorMessage("");
				p.setNotificationFlag(0);
				//adding read and delte flag for filters
				p.setDelete_users_favorites(0);
				p.setDelete_user_tweets(0);
				p.setDelete_new_followers(0);
				p.setDelete_new_followings(0);
				p.setRead_users_favorites(0);
				p.setRead_user_tweets(0);
				p.setRead_new_followers(0);
				p.setRead_new_followings(0);

				Person p2 = new Person();
				try {
					if (mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION) != null) {
						p2 = mongoOps.findById(p.getId(), Person.class, PERSON_COLLECTION);
						p.setReadFlag(p2.getReadFlag());
						p.setSaveFlag(p2.getSaveFlag());
						p.setDeleteFlag(p2.getDeleteFlag());
						p.setErrorFlag(p2.getErrorFlag());
						p.setErrorMessage("");
						p.setNotificationFlag(p2.getNotificationFlag());
						

						p.setDelete_users_favorites(p2.getDelete_users_favorites());
						p.setDelete_user_tweets(p2.getDelete_user_tweets());
						p.setDelete_new_followers(p2.getDelete_new_followers());
						p.setDelete_new_followings(p2.getDelete_new_followings());
						p.setRead_users_favorites(p2.getRead_users_favorites());
						p.setRead_user_tweets(p2.getRead_user_tweets());
						p.setRead_new_followers(p2.getRead_new_followers());
						p.setRead_new_followings(p2.getRead_new_followings());
						
						
						
					}
				} catch (Exception e) {
					logger.info("Twitter flag modification exception.."

					+ e.toString());
				}

				p.setParentId(parent_id);
				p.setSocialSiteUserId(social_site_user_id);

				// synchronized (this) {
				try {

					mongoOps.save(p, PERSON_COLLECTION);
					logger.info("After save  twitter post timeStamp  " + p.getId() + " is" + (System.currentTimeMillis()));
					JSONObject obj = new JSONObject();

					obj = new JSONObject();
					obj.put("id", (p.getId() + ""));
					obj.put("uuid", p.getUuid());

					obj.put("socialmediaType", p.getSocialMediaType());
					obj.put("child_user_id", p.getChild_user_id());
					obj.put("parent_id", p.getParentId());

					Greeting result = new Greeting();
					result.setId((String) obj.get("id"));
					result.setUuid((String) obj.get("uuid"));
					result.setChild_user_id((String) obj.get("child_user_id"));
					result.setParent_id((String) obj.get("parent_id"));
					result.setSocialmediaType((String) obj.get("socialmediaType"));

					arrayGreetings.add(result);

				} catch (Exception e) {
					logger.info("Twitter Save following followeres exception " + p.getId());

				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("twitter following following save  ");

		}
	}// end of function

}
