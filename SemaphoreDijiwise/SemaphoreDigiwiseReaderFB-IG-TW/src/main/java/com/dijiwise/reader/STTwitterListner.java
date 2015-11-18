package com.dijiwise.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jinstagram.auth.model.Token;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;

public class STTwitterListner {
	// facebook handler
	static Logger logger = Logger.getLogger(STTwitterListner.class);
	byte[] body;
	String routingKey;
	ExecutorService executor;
	com.rabbitmq.client.Connection connection1;
	Properties prop;
	Long counterValue;

	public STTwitterListner(byte[] body, String routingKey,
			ExecutorService executor,
			com.rabbitmq.client.Connection connection1, Properties prop,
			Long counterValue) {
		this.body = body;
		this.routingKey = routingKey;
		this.executor = executor;
		this.connection1 = connection1;
		this.prop = prop;

	}

	public static void processData(byte[] body, String routingKey,
			ExecutorService executor,
			com.rabbitmq.client.Connection connection1, Properties prop,
			Long counterValue,MongoClient mongo1) {

		logger.info("start of the twitter Thread: "
				+ (System.currentTimeMillis()));

		MongoOperations mongoOps = null;
		List<Status> data = null;
		String PERSON_COLLECTION = "";
		String FB_COMMENT_COLLECTION = "";
		String TWITTER_ENTITY_COLLECTION = "";

		com.rabbitmq.client.ConnectionFactory factory1 = null;
		MongoClient mongo = null;
		DateTimeZone zone = DateTimeZone.getDefault();

		String OAuthConsumerKey = null;
		String OAuthConsumerSecret = null;
		String OAuthAccessTokenValue = null;
		String OAuthAccessTokenSecret = null;
		Twitter twitter = null;
		Person errorPerson = null;
		JSONObject obj = null;
		Person p = null;
		Person p1 = null;

		try {
			Channel writechannel = null;

			PropertyConfigurator.configure("/dijiwise/log4j.properties");
			/* logger.info("start of the Thread: " + this.getName()); */
			// Properties prop = new Properties();
			InputStream input = null;
			try {
				File file = new File("/dijiwise/config.properties");
				input = new FileInputStream(file);
				prop.load(input);
				/*
				 * logger.info("Thread  read the propfile its name is :" +
				 * this.getName());
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
			TWITTER_ENTITY_COLLECTION = prop
					.getProperty("TWITTER_ENTITY_COLLECTION");
			FB_COMMENT_COLLECTION = prop.getProperty("FB_COMMENT_COLLECTION");
			final String MONGO_HOST = prop.getProperty("MONGO_HOST");
			final int MONGO_PORT = Integer.parseInt(prop
					.getProperty("MONGO_PORT"));
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
			OAuthAccessTokenSecret = ((String) jsonObject
					.get("oauth_token_secret") != null ? (String) jsonObject
					.get("oauth_token_secret") : "");

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
				mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
				mongoOps = new MongoTemplate(mongo, DB_NAME);
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setDebugEnabled(true).setOAuthConsumerKey(OAuthConsumerKey)
						.setOAuthConsumerSecret(OAuthConsumerSecret)
						.setOAuthAccessToken(OAuthAccessTokenValue)
						.setOAuthAccessTokenSecret(OAuthAccessTokenSecret);
				twitter = new TwitterFactory(cb.build()).getInstance();

				data = twitter.getHomeTimeline();

			} catch (Exception e) {
				logger.error("Exception: " + e.getMessage() + "--"
						+ e.getStackTrace() + " e.tostring: " + e.toString());
				errorPerson = new Person();

				errorPerson.setUuid((UUID.randomUUID() + ""));
				errorPerson.setParentId((String) jsonObject.get("parent_id"));
				errorPerson.setSocialSiteUserId((String) jsonObject
						.get("social_site_user_id"));
				errorPerson.setErrorFlag(1);
				errorPerson.setErrorMessage(e.toString());
				errorPerson.setSocialMediaType((String) jsonObject
						.get("social_media_type"));
				mongoOps.save(errorPerson, PERSON_COLLECTION);
			} finally {

			}
			try {
				/*
				 * logger.info("Thread  Got data  w.r.t accesstoken :" +
				 * (String) jsonObject.get("access_token") + "\n Thread Name:" +
				 * this.getName());
				 */
				for (Status i : data) {

					logger.info("STTWITTER start processing " + i.getId() + " is "
							+ (System.currentTimeMillis()));
					/*
					 * logger.info("###############Start#############" +
					 * i.getId());
					 * logger.info("Thread started reading post with id : " +
					 * i.getId() + "   updatged ptime " );
					 * logger.info("Twitter testing...");
					 */
					p = new Person();
					p.setUuid((UUID.randomUUID() + ""));
					p.setId(i.getId() + "");

					p.setInsertDate((new DateTime().toDateTimeISO().toString()));
					try {
						// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
						if (i.getURLEntities() != null
								&& i.getURLEntities().length > 0) {

							int j = 0;
							for (twitter4j.URLEntity c : i.getURLEntities()) {
								TwitterMediaEntities entity = new TwitterMediaEntities();
								entity.setStatusId(i.getId() + "");
								entity.setEntityId(i.getId() + "_" + (j++));
								entity.setMediaUrl(c.getURL());
								mongoOps.save(entity, TWITTER_ENTITY_COLLECTION);
							}

						}
					} catch (Exception e1) {
						logger.info("fb Comments list exception..."
								+ e1.toString());
						// TODO Auto-generated catch block

					}

					try {
						// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
						if (i.getMediaEntities() != null
								&& i.getMediaEntities().length > 0) {

							
							p.setTwitterMedias(i.getMediaEntities());

						}
					} catch (Exception e1) {
						logger.info("fb Comments list exception..."
								+ e1.toString());
						// TODO Auto-generated catch block

					}

					p.setMessage(i.getText());

					try {
						// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
						if ((twitter.getRetweets(i.getId()) != null)
								&& !(twitter.getRetweets(i.getId()).isEmpty())) {
							String uuidCommentId = UUID.randomUUID() + "";
							int j = 0;
							for (Status c : twitter.getRetweets(i.getId())) {
								FBcomments comment = new FBcomments();

								comment.setPostId(i.getId() + "");

								comment.setCommentId((c.getId() > 0 ? c.getId()
										+ "" : ""));
								comment.setCommentMessage((c.getText() != null ? c
										.getText() : ""));
								comment.setCommentFrom((c.getUser().getName() != null ? c
										.getUser().getName() != null ? c
										.getUser().getName() : "" : ""));
								comment.setProfileImage((c.getUser()
										.getMiniProfileImageURL() != null ? c
										.getUser().getMiniProfileImageURL()
										: ""));

								mongoOps.save(comment, FB_COMMENT_COLLECTION);
							}
							p.setFbComments(uuidCommentId);
						}
					} catch (Exception e1) {
						logger.info("fb Comments list exception..."
								+ e1.toString());
						// TODO Auto-generated catch block

					}

					Date local = i.getCreatedAt();
					long utc = zone.convertLocalToUTC(local.getTime(), false);
					p.setCreatedDate(utc);

					DateTime retriveDate = new DateTime(i.getCreatedAt());
					DateTime sendingDate = new DateTime(retriveDate.getYear(),
							retriveDate.getMonthOfYear(),
							retriveDate.getDayOfMonth(), 0, 0, 0);
					p.setUpdatedDate(sendingDate.getMillis());
					p.setUpdateDateTime(retriveDate.getMillis());
					p.setName((i.getUser().getName() != null ? i.getUser()
							.getName() : ""));

					try {
						if (i.getRetweetCount() > 0) {
							p.setCommentCount((long) i.getRetweetCount());
						} else {
							p.setCommentCount(0L);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("Comment count not exception...."
								+ e.toString());

					}

					try {
						if (i.getFavoriteCount() > 0) {

							p.setLikeCount((long) i.getFavoriteCount());

						} else {
							p.setLikeCount(0L);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						logger.error("Like count not exception...."
								+ e.toString());

					}

					p.setPhotoUrl((i.getUser().getMiniProfileImageURL()) != null ? (i
							.getUser().getMiniProfileImageURL()) : "");

					p.setSocialMediaType((String) jsonObject
							.get("social_media_type"));

					p.setChild_user_id((String) jsonObject.get("child_user_id"));

					p.setReadFlag(0);
					p.setSaveFlag(0);
					p.setDeleteFlag(0);
					p.setErrorFlag(0);
					p.setErrorMessage("");
					p.setNotificationFlag(0);
					
					p1 = new Person();
					try {
						if (mongoOps.findById(new Long(i.getId()).toString(), Person.class,
								PERSON_COLLECTION) != null)
						{
							p1 = mongoOps.findById(new Long(i.getId()).toString(),
									Person.class, PERSON_COLLECTION);
							p.setReadFlag(p1.getReadFlag());
							p.setSaveFlag(p1.getSaveFlag());
							p.setDeleteFlag(p1.getDeleteFlag());
							p.setErrorFlag(p1.getErrorFlag());
							p.setErrorMessage("");
							p.setNotificationFlag(p1.getNotificationFlag());
						}
					} catch (Exception e) {
						logger.info("Twitter flag modification exception.." 
								
								+ e.toString());
					}
					
					p.setParentId((String) jsonObject.get("parent_id"));
					p.setSocialSiteUserId((String) jsonObject
							.get("social_site_user_id"));

					// synchronized (this) {
					writechannel = connection1.createChannel();
					writechannel.queueDeclare(RQUEUE_NAME, false, false, false,
							args);
					/*p1 = new Person();
					if (mongoOps.findById(i.getId(), Person.class,
							PERSON_COLLECTION) != null) {
						p1 = mongoOps.findById(i.getId(), Person.class,
								PERSON_COLLECTION);
					}
					if (p1.getUuid() != null && p1.toString().length() > 0) {

						if (!(p1.toString()).equals(p.toString())) {

							mongoOps.save(p, PERSON_COLLECTION);

							logger.info("After update update twitter post timeStamp  "
									+ p.getId()
									+ " is"
									+ (System.currentTimeMillis()));
							obj = new JSONObject();
							obj.put("uuid", p.getUuid());

							obj.put("socialmediaType", p.getSocialMediaType());
							obj.put("child_user_id", p.getChild_user_id());
							obj.put("parent_id", p.getParentId());

							writechannel.basicPublish("", RQUEUE_NAME, null,
									obj.toString().getBytes());
						}
					} else {*/

						mongoOps.save(p, PERSON_COLLECTION);
						logger.info("After save  twitter post timeStamp  "
								+ p.getId() + " is"
								+ (System.currentTimeMillis()));
						obj = new JSONObject();
						obj.put("uuid", p.getUuid());

						obj.put("socialmediaType", p.getSocialMediaType());
						obj.put("child_user_id", p.getChild_user_id());
						obj.put("parent_id", p.getParentId());

						writechannel.basicPublish("", RQUEUE_NAME, null, obj
								.toString().getBytes());
					//}//else

					// } synchronized

				}
				data = null;
			} catch (UnknownHostException e) {
				logger.error("UnknownHostException: e.toSting: " + e.toString());
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
			}
			logger.info("End of the twitter Thread at :"
					+ (System.currentTimeMillis()));
		} catch (Exception e) {
			logger.error("Exception:" + e.getMessage());
			e.printStackTrace();
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
		}
	}

}
