package com.dijiwise.semaphore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

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

import twitter4j.DirectMessage;
import twitter4j.PagableResponseList;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import com.dijiwise.reader.ErrorPerson;
import com.dijiwise.reader.FBcomments;
import com.dijiwise.reader.Person;
import com.dijiwise.reader.PersonFind;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;

public class TwitterWorker {
	// facebook handler
	static Logger logger = Logger.getLogger(TwitterWorker.class);
	
	byte[] body;
	String routingKey;
	ExecutorService executor;
	com.rabbitmq.client.Connection connection1;
	Properties prop;
	Long counterValue;
	public TwitterWorker(){}
	public TwitterWorker(byte[] body, String routingKey, ExecutorService executor, com.rabbitmq.client.Connection connection1,
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

					MongoOperations mongoOps = null;
					List<Status> favorites = null;

					List<Status> userTimeline = null;
					List<Status> userFavorites = null;
					List<Status> userRetweets = null;
					String PERSON_COLLECTION = "";
					String PERSON_LOGIN_STATUS = "";
					String PERSON_FOLLOWING_FOLLOWS = "";
					String FB_COMMENT_COLLECTION = "";
					String PERSON_COLLECTION_TEMP="";
					String FB_COMMENT_COLLECTION_TEMP = "";
					String TWITTER_ENTITY_COLLECTION = "";
					String child_user_id = null;
					String parent_id=null;

					com.rabbitmq.client.ConnectionFactory factory1 = null;
					MongoClient mongo = null;
					DateTimeZone zone = DateTimeZone.getDefault();

					String OAuthConsumerKey = null;
					String OAuthConsumerSecret = null;
					String OAuthAccessTokenValue = null;
					String OAuthAccessTokenSecret = null;
					Twitter twitter = null;
					ErrorPerson errorPerson = null;
					JSONObject obj = null;
					Person p = null;
					ErrorPerson p1 = null;
					PersonFind p2 = null;
					ResponseList<DirectMessage> directMessages = null;

					try {

						Channel writechannel = null;

						//TODO Senthil - Commented by
//						PropertyConfigurator.configure("/dijiwise/log4j.properties");
						PropertyConfigurator.configure(SocialNetworkService.LOG4J_PROP);
						/*
						 * logger.info("start of the Thread: " +
						 * this.getName());
						 */
						// Properties prop = new Properties();
						InputStream input = null;
						try {
							File file = new File(SocialNetworkService.CONFIG_PROP);
							input = new FileInputStream(file);
							prop.load(input);
							/*
							 * logger.info("Thread  read the propfile its name is :"
							 * + this.getName());
							 */
							// load a properties file
						} catch (Exception e) {
							logger.info("Thread  Error in reading file name is  :");
							e.printStackTrace();
						} finally {
							input.close();
						}
						final String RQUEUE_NAME = prop.getProperty("RQUEUE_NAME");
						final int PORT = Integer.parseInt(prop.getProperty("PORT"));
						final String HOST = prop.getProperty("HOST");
						final String DB_NAME = prop.getProperty("DB_NAME");
						PERSON_COLLECTION = prop.getProperty("PERSON_COLLECTION");
						PERSON_COLLECTION_TEMP = prop.getProperty("PERSON_COLLECTION_TEMP");
						PERSON_LOGIN_STATUS = prop.getProperty("PERSON_LOGIN_STATUS");
						TWITTER_ENTITY_COLLECTION = prop.getProperty("TWITTER_ENTITY_COLLECTION");
						FB_COMMENT_COLLECTION = prop.getProperty("FB_COMMENT_COLLECTION");
						PERSON_FOLLOWING_FOLLOWS = prop.getProperty("PERSON_FOLLOWING_FOLLOWS");
						PERSON_COLLECTION_TEMP=prop.getProperty("PERSON_COLLECTION_TEMP");
						FB_COMMENT_COLLECTION_TEMP =prop.getProperty("FB_COMMENT_COLLECTION_TEMP");
						final String MONGO_HOST = prop.getProperty("MONGO_HOST");
						final int MONGO_PORT = Integer.parseInt(prop.getProperty("MONGO_PORT"));
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("x-cancel-on-ha-failover", true);
						factory1 = new com.rabbitmq.client.ConnectionFactory();
						factory1.setNetworkRecoveryInterval(5000);
						factory1.setHost(HOST);
						factory1.setPort(PORT);

						String message = new String(body);
						JSONParser jsonParser = new JSONParser();
						JSONObject jsonObject = (JSONObject) jsonParser.parse(message);

						OAuthConsumerKey = "arW0EywAp2gQn0xn2SNzpGEZ0";
						OAuthConsumerSecret = "OG9EcoIpPeltoLR4Ia6h01QWca73GGlySAC42E6s6fwRn8vCC5";
						OAuthAccessTokenValue = ((String) jsonObject.get("access_token") != null ? (String) jsonObject
								.get("access_token") : "");
						
						OAuthAccessTokenSecret = ((String) jsonObject.get("oauth_token_secret") != null ? (String) jsonObject
								.get("oauth_token_secret") : "");
						
						/*OAuthAccessTokenValue = "new-1135562082-L1dqLcwhCeevZDOcweDFwXBmgk05VsiKNYRz4gz";
						
						OAuthAccessTokenSecret = "NbAQ3r4cPkisxSrrLA2CZ7hBO9ESDEV9j5pY3pq9cFvP9";
						*/
												
						child_user_id = (String) jsonObject.get("child_user_id");
						parent_id=(String) jsonObject.get("parent_id");
						logger.info("\nSTtwitter Listener start of the fb Thread: at :" + (System.currentTimeMillis())
								+ " childuserId: " + child_user_id+" of parent "+parent_id);
						String socialSiteUserId = ((String) jsonObject.get("social_site_user_id"));
						String screenName = "";

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
							ConfigurationBuilder cb = new ConfigurationBuilder();
							cb.setDebugEnabled(true).setOAuthConsumerKey(OAuthConsumerKey)
									.setOAuthConsumerSecret(OAuthConsumerSecret).setOAuthAccessToken(OAuthAccessTokenValue)
									.setOAuthAccessTokenSecret(OAuthAccessTokenSecret);
							twitter = new TwitterFactory(cb.build()).getInstance();

							favorites = twitter.getFavorites();//getHomeTimeline();
							
							//logger.info("\n\n data "+data.toString());
							userTimeline = twitter.getUserTimeline();
							screenName = twitter.getScreenName();
							//logger.info("\n\n userData  "+userData.toString());
							// directMessages=twitter.getDirectMessages();
							System.out.println("");

						} catch (Exception e) {
							p1 = new ErrorPerson();
							errorPerson = new ErrorPerson();
							
							logger.info("\nSTTwitter Listener Error at :" + (System.currentTimeMillis()) + " for childuserId: "
									+ child_user_id+" of Parent "+parent_id);
							errorPerson.setChildId(child_user_id);
							errorPerson.setParent_id(parent_id);
							
							errorPerson.setErrorOccuredDate(new Date().toString());
							errorPerson.setErrorMessage(e.getMessage());
							errorPerson.setUuid((UUID.randomUUID() + ""));

							errorPerson.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));
							errorPerson.setErrorMessage(e.getMessage());

							errorPerson.setSocialMediaType((String) jsonObject.get("social_media_type"));
							if(e.getMessage().contains("429:Returned in API")){
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
								
								try {
									long cursor = -1;
									long cursorFollowing = -1;
									PagableResponseList<User> followers, following;
									ArrayList<User> followersFinalList = new ArrayList();
									ArrayList<User> followingFinalList = new ArrayList();

									try {
										do {
											followers = twitter.getFollowersList(screenName, cursor);
											

											followersFinalList.addAll(followers);
											System.out.println("\n followersFinalList.size()  : " + followersFinalList.size());

										} while ((cursor = followers.getNextCursor()) != 0);
										savefollowFollowing(followersFinalList, PERSON_COLLECTION,PERSON_COLLECTION_TEMP,FB_COMMENT_COLLECTION_TEMP, connection1, RQUEUE_NAME, args,
												mongoOps, jsonObject, zone, writechannel,child_user_id,parent_id, "followers");
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									

									try {
										do {
											following = twitter.getFriendsList(screenName, cursorFollowing);
											
											followingFinalList.addAll(following);
											System.out.println("\n followingFinalList.size()  : " + followingFinalList.size());

										} while ((cursorFollowing = following.getNextCursor()) != 0);
										//PersonFollowersFollowing followingList = new PersonFollowersFollowing();
										savefollowFollowing(followingFinalList, PERSON_COLLECTION,PERSON_COLLECTION_TEMP,FB_COMMENT_COLLECTION_TEMP, connection1, RQUEUE_NAME, args,
												mongoOps, jsonObject, zone, writechannel,child_user_id,parent_id, "following");
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								} catch (Exception e2) {
									// TODO Auto-generated catch block
									logger.error("STTWITTER follwers/following exception  for " + screenName);
								}

								
								for (Status i : favorites) {

									logger.info("STTWITTER start processing " + i.getId() +"_"+child_user_id +" of parent "+parent_id+" is " + (System.currentTimeMillis()));
									
									p = new Person();
									p.setUuid((UUID.randomUUID() + ""));
									p.setId(i.getId() +"_"+child_user_id+"_"+parent_id);

									p.setInsertDate((new DateTime().toDateTimeISO().toString()));
									try {
										// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
										if (i.getURLEntities() != null) {

											p.setTwitterUrls(i.getURLEntities());
										}

									} catch (Exception e1) {
										logger.info("fb Comments list exception 4 ..." + e1.toString());
										// TODO Auto-generated catch block

									}

									try {
										// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
										if (i.getMediaEntities() != null && i.getMediaEntities().length > 0) {

											p.setTwitterMedias(i.getMediaEntities());

										}
									} catch (Exception e1) {
										logger.info("fb Comments list exception 5 ..." + e1.toString());
										// TODO Auto-generated catch block

									}

									p.setMessage(i.getText());
									p.setCommentCount(0L);
									try {
										//TODO Senthil - Commented
//										if ((twitter.getRetweets(i.getId()) != null)
//												&& !(twitter.getRetweets(i.getId()).isEmpty())) {
//											String uuidCommentId = UUID.randomUUID() + "";
//											long j = 0L;
//											for (Status c : twitter.getRetweets(i.getId())) {
//												
//												try {
//													FBcomments comment = new FBcomments();
//
//													
//													comment.setPostId(p.getId());
//													comment.setCommentId((c.getId() > 0 ?( c.getId() +"_"+ p.getId()) : ""));
//													comment.setCommentMessage((c.getText() != null ? c.getText() : ""));
//													comment.setCommentFrom((c.getUser().getName() != null ? c.getUser().getName() != null ? c
//															.getUser().getName() : ""
//															: ""));
//													
//													comment.setProfileImage((c.getUser().getMiniProfileImageURL() != null ? c
//															.getUser().getMiniProfileImageURL() : ""));
//													
//													comment.setSocialMediaType((String) jsonObject.get("social_media_type"));
//													comment.setChildUserId((String) jsonObject.get("child_user_id"));
//													comment.setParent_id(parent_id);
//													
//													//adding flags to Comments
//													comment.setReadFlag(0);
//													comment.setSaveFlag(0);
//													comment.setDeleteFlag(0);
//													comment.setErrorFlag(0);
//													comment.setErrorMessage("");
//													comment.setNotificationFlag(0);
//
//													FBcomments dbcomment = new FBcomments();
//													try {
//														
//														//if (mongoOps.findById(i.getId()+"_"+child_user_id, Person.class, PERSON_COLLECTION) != null) {
//														//	p2 = mongoOps.findById(new Long(i.getId()).toString()+"_"+child_user_id, Person.class, PERSON_COLLECTION);
//														if (mongoOps.findById(comment.getCommentId(),  FBcomments.class,  FB_COMMENT_COLLECTION) != null) {
//															dbcomment = mongoOps.findById(comment.getCommentId(), FBcomments.class,  FB_COMMENT_COLLECTION);
//															comment.setReadFlag(dbcomment.getReadFlag());
//															comment.setSaveFlag(dbcomment.getSaveFlag());
//															comment.setDeleteFlag(dbcomment.getDeleteFlag());
//															comment.setErrorFlag(dbcomment.getErrorFlag());
//															comment.setErrorMessage("");
//															comment.setNotificationFlag(dbcomment.getNotificationFlag());
//															
//															
//														}
//													} catch (Exception e) {
//														logger.info("Twitter comment modification exception.."
//
//														+ e.toString());
//													}
//													//end  of adding flags to comment
//
//													//mongoOps.save(comment, FB_COMMENT_COLLECTION);
//													mongoOps.save(comment, FB_COMMENT_COLLECTION_TEMP);
//												} catch (Exception e) {
//													logger.info("Twitter comment modification exception.."
//
//														+ e.toString());
//												}
//												
//											}
//											Query query5 = new Query();
//											query5.addCriteria(Criteria.where("postId").is(p.getId()));
//											j=(mongoOps.count(query5, FB_COMMENT_COLLECTION_TEMP));
//											p.setCommentCount(j);
//											p.setFbComments(uuidCommentId);
//										}
									} catch (Exception e1) {
										logger.info("fb Comments list exception 6 ..." + e1.toString());
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

									p.setPhotoUrl((i.getUser().getMiniProfileImageURL()) != null ? (i.getUser()
											.getMiniProfileImageURL()) : "");

									p.setSocialMediaType((String) jsonObject.get("social_media_type"));

									p.setChild_user_id((String) jsonObject.get("child_user_id"));
									
									p.setPostSource("hometimeline");
									p.setReadFlag(0);
									p.setSaveFlag(0);
									p.setDeleteFlag(0);
									p.setErrorFlag(0);
									p.setErrorMessage("");
									p.setNotificationFlag(0);
									//adding read and delete flags for filters
									p.setDelete_users_favorites(0);
									p.setDelete_user_tweets(0);
									p.setDelete_new_followers(0);
									p.setDelete_new_followings(0);
									p.setRead_users_favorites(0);
									p.setRead_user_tweets(0);
									p.setRead_new_followers(0);
									p.setRead_new_followings(0);
		

									p2 = new PersonFind();/*i.getId() +"_"+child_user_id*/
									try {
										/*if (mongoOps.findById(new Long(i.getId()).toString()+"_"+child_user_id, Person.class, PERSON_COLLECTION) != null) {
											p2 = mongoOps.findById(new Long(i.getId()).toString()+"_"+child_user_id, Person.class,
													PERSON_COLLECTION);*/
										logger.info("Sri Entering ..parent id "+p.getParentId()+" child id "+p.getChild_user_id() +" postid : "+p.getId());
										if (mongoOps.findById(p.getId(), PersonFind.class, PERSON_COLLECTION) != null) {
											p2 = mongoOps.findById(p.getId(), PersonFind.class,
													PERSON_COLLECTION);
											logger.info("EXISTING OBJECT# "+ p2);
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
											
											logger.info("EXISTING OBJECT# "+ p);
											
											
										}
										else{
											logger.info("Count not found ..parent id "+p.getParentId()+" child id "+p.getChild_user_id() +" postid : "+p.getId());

										
										}
									} catch (Exception e) {
										logger.info("Data exception p2.."

										+ e.toString());
									}

									p.setParentId((String) jsonObject.get("parent_id"));
									p.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));

									// synchronized (this) {
									//writechannel = connection1.createChannel();
									//writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
									

									//mongoOps.save(p, PERSON_COLLECTION);
									mongoOps.save(p,PERSON_COLLECTION_TEMP);
									logger.info("After save  twitter post timeStamp  " + p.getId() + " is"
											+ (System.currentTimeMillis()));
									obj = new JSONObject();
									obj.put("uuid", p.getUuid());

									obj.put("socialmediaType", p.getSocialMediaType());
									obj.put("child_user_id", p.getChild_user_id());
									obj.put("parent_id", p.getParentId());

									//writechannel.basicPublish("", RQUEUE_NAME, null, obj.toString().getBytes());
									try {

										if (writechannel != null && writechannel.isOpen()) {

											writechannel.close();
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block

										logger.info("Exception in closing the channel. " + e.toString());
									}
									// }//else

									// } synchronized

								}

								// user time line
								for (Status i : userTimeline) {

									try {
										logger.info("STTWITTER start processing " + i.getId() +"_"+child_user_id + " of parent "+parent_id+" is " + (System.currentTimeMillis()));
										/*
										 * logger.info(
										 * "###############Start#############" +
										 * i.getId()); logger.info(
										 * "Thread started reading post with id : "
										 * + i.getId() + "   updatged ptime " );
										 * logger.info("Twitter testing...");
										 */
										p = new Person();
										//need to clean up
										p.setUuid((UUID.randomUUID() + ""));
										String strPostId;
										strPostId=i.getId() +"_"+child_user_id+"_"+parent_id;
										p.setId(strPostId);

										p.setInsertDate((new DateTime().toDateTimeISO().toString()));
										try {
											// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
											if (i.getURLEntities() != null) {

												p.setTwitterUrls(i.getURLEntities());
											}

										} catch (Exception e1) {
											logger.info("fb Comments list exception 1 ..." + e1.toString());
											// TODO Auto-generated catch block

										}

										try {
											// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
											if (i.getMediaEntities() != null && i.getMediaEntities().length > 0) {

												p.setTwitterMedias(i.getMediaEntities());

											}
										} catch (Exception e1) {
											logger.info("fb Comments list exception 2 ..." + e1.toString());
											// TODO Auto-generated catch block

										}

										p.setMessage(i.getText());
										p.setCommentCount(0L);

										try {
											// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
											//TODO Senthil - Commented
//											if ((twitter.getRetweets(i.getId()) != null)
//													&& !(twitter.getRetweets(i.getId()).isEmpty())) {
//												String uuidCommentId = UUID.randomUUID() + "";
//												long j = 0L;
//												for (Status c : twitter.getRetweets(i.getId())) {
//													
//													try {
//														FBcomments comment = new FBcomments();
//
//														//comment.setPostId(i.getId() +"_"+child_user_id);
//														comment.setPostId(p.getId());
//														comment.setCommentId((c.getId() > 0 ? ( c.getId() +"_"+ p.getId()) : ""));
//														comment.setCommentMessage((c.getText() != null ? c.getText() : ""));
//														comment.setCommentFrom((c.getUser().getName() != null ? c.getUser().getName() != null ? c
//																.getUser().getName() : ""
//																: ""));
//														comment.setProfileImage((c.getUser().getMiniProfileImageURL() != null ? c
//																.getUser().getMiniProfileImageURL() : ""));
//														comment.setSocialMediaType((String) jsonObject.get("social_media_type"));
//														comment.setChildUserId((String) jsonObject.get("child_user_id"));
//														comment.setParent_id(parent_id);
//														//adding flags to Comments
//
//														comment.setReadFlag(0);
//														comment.setSaveFlag(0);
//														comment.setDeleteFlag(0);
//														comment.setErrorFlag(0);
//														comment.setErrorMessage("");
//														comment.setNotificationFlag(0);
//
//														FBcomments dbcomment = new FBcomments();
//														try {
//															
//															//if (mongoOps.findById(i.getId()+"_"+child_user_id, Person.class, PERSON_COLLECTION) != null) {
//															//	p2 = mongoOps.findById(new Long(i.getId()).toString()+"_"+child_user_id, Person.class, PERSON_COLLECTION);
//															if (mongoOps.findById(comment.getCommentId(),  FBcomments.class,  FB_COMMENT_COLLECTION) != null) {
//																dbcomment = mongoOps.findById(comment.getCommentId(), FBcomments.class,  FB_COMMENT_COLLECTION);
//																comment.setReadFlag(dbcomment.getReadFlag());
//																comment.setSaveFlag(dbcomment.getSaveFlag());
//																comment.setDeleteFlag(dbcomment.getDeleteFlag());
//																comment.setErrorFlag(dbcomment.getErrorFlag());
//																comment.setErrorMessage("");
//																comment.setNotificationFlag(dbcomment.getNotificationFlag());
//															}
//														} catch (Exception e) {
//															logger.info("Twitter comments modification exception.."
//
//															+ e.toString());
//														}
//														//end  of adding flags to comment
//														//mongoOps.save(comment, FB_COMMENT_COLLECTION);
//														mongoOps.save(comment, FB_COMMENT_COLLECTION_TEMP);
//													} catch (Exception e) {
//														logger.info("Twitter comments modification exception.."
//
//															+ e.toString());
//													}
//													
//												}
//												Query query5 = new Query();
//												query5.addCriteria(Criteria.where("postId").is(p.getId()));
//												j=(mongoOps.count(query5, FB_COMMENT_COLLECTION_TEMP));
//												p.setCommentCount(j);
//												p.setFbComments(uuidCommentId);
//																							}
										} catch (Exception e1) {
											logger.info("fb Comments list exception 3 ..." + e1.toString());
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

										p.setPhotoUrl((i.getUser().getMiniProfileImageURL()) != null ? (i.getUser()
												.getMiniProfileImageURL()) : "");

										p.setSocialMediaType((String) jsonObject.get("social_media_type"));

										p.setChild_user_id((String) jsonObject.get("child_user_id"));
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
										String strId;
										strId=p.getId();

										p2 = new PersonFind();
										try {
											logger.info("Sri Entering ..parent id  "+p.getParentId()+" child id "+p.getChild_user_id() +" postid : "+p.getId());
											
											if (mongoOps.findById(p.getId(), PersonFind.class, PERSON_COLLECTION) != null) {
												p2 = mongoOps.findById(strId, PersonFind.class,
														PERSON_COLLECTION);
												logger.info("EXISTING OBJECT# "+ p2);
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
												
												logger.info("NEW OBJECT#"+ p);
												
											}
											else{
												logger.info("Count not found ..parent id "+p.getParentId()+" child id "+p.getChild_user_id() +" postid : "+p.getId());

											
											}
										} catch (Exception e) {
											logger.info("Data exception p2." + p.getId()

											+ e.toString());
										}

										p.setParentId((String) jsonObject.get("parent_id"));
										p.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));

										// synchronized (this) {
										writechannel = connection1.createChannel();
										writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
										

										//mongoOps.save(p, PERSON_COLLECTION);
										mongoOps.save(p,PERSON_COLLECTION_TEMP);
										logger.info("After save  twitter post timeStamp  " + p.getId() + " is"
												+ (System.currentTimeMillis()));
										obj = new JSONObject();
										obj.put("uuid", p.getUuid());

										obj.put("socialmediaType", p.getSocialMediaType());
										obj.put("child_user_id", p.getChild_user_id());
										obj.put("parent_id", p.getParentId());

										//writechannel.basicPublish("", RQUEUE_NAME, null, obj.toString().getBytes());
										try {

											if (writechannel != null && writechannel.isOpen()) {

												writechannel.close();
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block

											logger.info("Exception in closing the channel. " + e.toString());
										}
										
									} catch (Exception e) {
										// TODO Auto-generated catch block
										logger.error("Exception processing a single twitter post " + e.toString());
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
								favorites = null;

							}// end of if case
						} catch (Exception e5) {
							logger.error("UnknownHostException: e.toSting: " + e5.toString());
							e5.printStackTrace();
						} finally {
							if (writechannel != null && writechannel.isOpen()) {
								writechannel.close();
							}
							try {
								writechannel = connection1.createChannel();
								writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);
								writechannel.basicPublish("", RQUEUE_NAME, null, jsonObject.toString().getBytes());
								logger.info("TW message placed for  child  :" +child_user_id+ "with parent "+parent_id );
							} catch (Exception e4) {
								// TODO Auto-generated catch block
								e4.printStackTrace();
							}
							if (mongo != null && mongo instanceof MongoClient) {
								try {
									mongo.close();
								} catch (Exception e3) {
									// TODO Auto-generated catch block
									logger.error("mongo connection exception : " + e3.toString());
								}
							}
							if (writechannel != null && writechannel.isOpen()) {
								writechannel.close();
							}
						}
						logger.info("End of the twitter Thread at :" + (System.currentTimeMillis()));

					} catch (Exception e) {
						logger.error("Exception:" + e.getMessage());
						e.printStackTrace();
					} finally {

						errorPerson = null;
						favorites = null;
						obj = null;
						if (mongo != null && mongo instanceof MongoClient) {
							try {
								mongo.close();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}
				} catch (Exception e) {
				} finally {
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

	public static void savefollowFollowing(ArrayList<User> data,
			String PERSON_COLLECTION,String PERSON_COLLECTION_TEMP,String FB_COMMENT_COLLECTION_TEMP,
			com.rabbitmq.client.Connection connection1, String RQUEUE_NAME,
			Map<String, Object> args, MongoOperations mongoOps,
			JSONObject jsonObject, DateTimeZone zone,
			com.rabbitmq.client.Channel writechannel, String child_user_id, String parent_id,
			String suffix) {
	
		String socialSiteUserId = ((String) jsonObject.get("social_site_user_id"));
		
			for (User i : data) {
				try {
				logger.info("STTWITTER start processing " +i.getId() +"_"+child_user_id + " is "

				+ (System.currentTimeMillis()));
				/*
				 * logger.info("###############Start#############" + i.getId());
				 * logger.info("Thread started reading post with id : " +
				 * i.getId() + "   updatged ptime " );
				 * logger.info("Twitter testing...");
				 */
				Person p = new Person();
				p.setUuid((UUID.randomUUID() + ""));
				p.setId(socialSiteUserId + "_" + i.getId()+"_"+child_user_id + "_"+parent_id+"_" + suffix);

				p.setInsertDate((new DateTime().toDateTimeISO().toString()));

				p.setMessage(i.getDescription());

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

				p.setSocialMediaType((String) jsonObject.get("social_media_type"));

				p.setChild_user_id((String) jsonObject.get("child_user_id"));
				p.setTotalCommentCount(0);

				p.setReadFlag(0);
				p.setSaveFlag(0);
				p.setDeleteFlag(0);
				p.setErrorFlag(0);
				p.setErrorMessage("");
				p.setNotificationFlag(0);
				//adding read and delete flags for filter
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
					
					//if (mongoOps.findById(i.getId()+"_"+child_user_id, Person.class, PERSON_COLLECTION) != null) {
					//	p2 = mongoOps.findById(new Long(i.getId()).toString()+"_"+child_user_id, Person.class, PERSON_COLLECTION);
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

				p.setParentId((String) jsonObject.get("parent_id"));
				p.setSocialSiteUserId((String) jsonObject.get("social_site_user_id"));

				// synchronized (this) {
				try {
					writechannel = connection1.createChannel();
					writechannel.queueDeclare(RQUEUE_NAME, false, false, false, args);

					//mongoOps.save(p, PERSON_COLLECTION);
					mongoOps.save(p,PERSON_COLLECTION_TEMP);
					logger.info("After save  twitter post timeStamp  " + p.getId() + " is" + (System.currentTimeMillis()));
					JSONObject obj = new JSONObject();
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
				} catch (IOException e) {
					logger.info("writechannedl creation error in twitter following  " + p.getId() + " is"
							+ (System.currentTimeMillis()));

				}

			}
				catch (Exception e) {
					// TODO Auto-generated catch block
					logger.info("Error processing a single followers/following  post "+e.getMessage()); 

				}
		} 
	}

}
