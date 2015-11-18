package com.digiwise.reader;

import hello.DijiWiseResultsArray;
import hello.Greeting;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

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
import org.springframework.social.oauth2.OAuth2Parameters;

import com.googlecode.googleplus.ActivityOperations;
import com.googlecode.googleplus.CommentOperations;
import com.googlecode.googleplus.GooglePlusFactory;
import com.googlecode.googleplus.MomentsOperations;
import com.googlecode.googleplus.PeopleOperations;
import com.googlecode.googleplus.Plus;
import com.googlecode.googleplus.model.activity.Activity;
import com.googlecode.googleplus.model.activity.ActivityCollection;
import com.googlecode.googleplus.model.activity.ActivityFeed;
import com.googlecode.googleplus.model.activity.ActivityObjectAttachments;
import com.googlecode.googleplus.model.comment.CommentFeed;
import com.googlecode.googleplus.model.person.PeopleCollection;
import com.googlecode.googleplus.model.person.PersonFeed;
import com.mongodb.MongoClient;
import com.rabbitmq.client.Channel;

public class GooglePlusListener {
	// facebook handler
	final static Logger logger = Logger.getLogger(GooglePlusListener.class);

	public static DijiWiseResultsArray getUserGoogleFeeds(String parent_id,
			String child_user_id, String access_token,
			String oauth_token_secret, String social_media_type,
			String social_site_user_id, DijiWiseResultsArray array){

		String accessTokenValue = null;
		String refreshTokenValue = null;
		MongoOperations mongoOps = null;
		Plus plus = null;
		List<Activity> data = null;
		String PERSON_COLLECTION = "";
		String FB_COMMENT_COLLECTION = "";
		Token secretToken = null;
		com.rabbitmq.client.ConnectionFactory factory1 = null;
		MongoClient mongo = null;
		DateTimeZone zone = DateTimeZone.getDefault();
		DateTimeFormatter df = DateTimeFormat.forPattern("yyyy-mm-dd hh-mm-ss");
		ArrayList<Greeting> arrayGreetings = new ArrayList();
		Person p1 = null;
		PersonFeed people = null;

		try {

			final String DB_NAME = "dijiwisecdndb";
			
			PERSON_COLLECTION = "dijiwisesocialcollection";
			FB_COMMENT_COLLECTION="dijiwiseComment";
			
//			final String MONGO_HOST = "173.244.67.213";
			//final String MONGO_HOST = "162.209.127.150";
			final String MONGO_HOST = JavaListener.MONGO_HOST;
			final int MONGO_PORT = Integer.parseInt("27017");
			 refreshTokenValue = oauth_token_secret;
			accessTokenValue = access_token;
			
			 
			 String secret = "wMkZAYYeKTgXMFr_A-0EUPnT";
				String id = "881464315694-225kilo20ne8rg0j42n21fkprgk87pnq.apps.googleusercontent.com";
				OAuth2Parameters oAuthParams = new OAuth2Parameters();

			GooglePlusFactory factory = null;
			try {
				factory = new GooglePlusFactory(id, secret);
				
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
				
			}

			/*
			 * ArrayList<Post> data = new GraphReaderExample( (String)
			 * jsonObject.get("access_token")) .FilteredPosts();
			 */
			try {
				mongo = new MongoClient(MONGO_HOST, MONGO_PORT);
				mongoOps = new MongoTemplate(mongo, DB_NAME);

				//plus = factory.getApi(accessTokenValue);
				plus = factory
						.getApi(accessTokenValue, refreshTokenValue, null);
				com.googlecode.googleplus.model.person.Person user = plus
						.getPeopleOperations().get("me");
				ActivityOperations activities = plus.getActivityOperations();

				ActivityFeed ac = activities.list(user.getId(),
						ActivityCollection.PUBLIC);
				data = ac.getItems();
				/*CommentOperations commentsOps = plus.getCommentOperations();
				MomentsOperations momentsOps = plus.getHistoryOperations();
				PeopleOperations peopleOps = plus.getPeopleOperations();

				people = peopleOps.list(user.getId(), PeopleCollection.VISIBLE);*/

				/*
				 * MomentFeed momentfeed = momentsOps.list(user.getId(),
				 * MomentCollection.VAULT);
				 */

			} catch (Exception e) {
				logger.error("Exception: " + e.getMessage() + "--"
						+ e.getStackTrace() + " e.tostring: " + e.toString());
				Person errorPerson = new Person();

				errorPerson.setUuid((UUID.randomUUID() + ""));
				errorPerson.setParentId(parent_id);
				errorPerson.setSocialSiteUserId(social_site_user_id);
				errorPerson.setErrorFlag(1);
				errorPerson.setErrorMessage(e.toString());
				errorPerson.setSocialMediaType(social_media_type);
				mongoOps.save(errorPerson, PERSON_COLLECTION);
			}
			try {
				logger.info("Thread  Got data  w.r.t accesstoken :"
						+ access_token);
				int countFeed = 0;
				for (Activity i : data) {
					logger.info("###############Start#############" + i.getId());
					logger.info("Thread started reading post with id : "
							+ i.getId() + "   updatged ptime "
							+ i.getUpdated().getTime());
					logger.info("fb testing..." + i.toString());
					Person p = new Person();
					p.setUuid((UUID.randomUUID() + ""));
					p.setId(i.getId());
					p.setName(i.getActor().getDisplayName());

					p.setInsertDate((new DateTime().toDateTimeISO().toString()));

					if (i.getObject().getAttachments() != null) {

						for (ActivityObjectAttachments e : i.getObject()
								.getAttachments()) {

							if (e.getImage() != null) {
								// System.out.println("\n e.getObjectType() :"+e.getObjectType()+" e.getUrl(): "+e.getUrl()+"  e.getImage().getUrl()"+e.getImage().getUrl());
								p.setGoogleImageAttachment(e.getImage()
										.getUrl());
							}
							if (e.getEmbed() != null) {
								p.setGoogleVideoAttachment(e.getEmbed()
										.getUrl());
								p.setGoogleEmbedType(e.getEmbed().getType());
							}
							// System.out.println("e.getEmbed().getType(): "+e.getEmbed().getType()+
							// " e.getEmbed().getUrl(): "+e.getEmbed().getUrl()
							// );
							if (e.getContent() != null) {

								p.setGoogleAlbumAttachment(e.getContent());
							}// System.out.println("e.getContent(): "+e.getContent());

						}
					}

					p.setGoogleActivityUrl((i.getUrl() != null ? i.getUrl()
							: ""));
					p.setGoogleOriginalContent((i.getObject() != null ? (i
							.getObject().getOriginalContent() != null ? i
							.getObject().getOriginalContent() : "") : ""));
					p.setGoogleContent(i.getObject() != null ? i.getObject()
							.getContent() != null ? i.getObject().getContent()
							: "" : "");
					p.setGoogleCrossPost(i.getCrosspostSource() != null ? i
							.getCrosspostSource() : "");

					p.setMessage(i.getTitle());

					logger.info("fb testing for toLocaleString...");

					try {
						CommentOperations commentsOp = plus
								.getCommentOperations();

						CommentFeed ac = commentsOp.list(i.getId());
						p.setCommentCount((long) ac.getItems().size());
						// logger.info("fb testing for toLocaleString. i.getComments().getData().."+i.getComments().getData());
						if (ac != null & ac.getItems().size() > 0) {
							String uuidCommentId = UUID.randomUUID() + "";
							int j = 0;
							for (com.googlecode.googleplus.model.comment.Comment c : ac
									.getItems()) {
								FBcomments comment = new FBcomments();
								comment.setUuidComment(uuidCommentId);
								comment.setPostId(i.getId());

								comment.setCommentId((c.getId() != null ? c
										.getId() : ""));
								comment.setCommentMessage((c.getObject()
										.getContent() != null ? c.getObject()
										.getContent() : ""));
								comment.setCommentFrom((c.getActor() != null ? c
										.getActor().getDisplayName() != null ? c
										.getActor().getDisplayName() : ""
										: ""));
								comment.setProfileImage((c.getActor()
										.getImage().getUrl() != null ? c
										.getActor().getImage().getUrl() : ""));
								comment.setCommentedById((c.getActor().getId() != null ? c
										.getActor().getId() : ""));
								comment.setAttachmentType((c.getObject()
										.getObjectType() != null ? c
										.getObject().getObjectType() : ""));
								comment.setAttachmentUrl((c.getVerb() != null ? c
										.getVerb() : ""));

								j++;
								mongoOps.save(comment, FB_COMMENT_COLLECTION);
							}
							p.setFbComments(uuidCommentId);
							// p.setCommentCount((long)i.getComments().getData().size());
							// p.setCommentCount((long)j);
						}
					} catch (Exception e1) {
						logger.info("webservice googleplus Comments list exception..."
								+ e1.toString());
						// TODO Auto-generated catch block

					}

					Date local = i.getPublished();
					long utc = zone.convertLocalToUTC(local.getTime(), false);
					p.setCreatedDate(utc);

					p.setPostType((i.getKind() != null ? i.getKind() : ""));

					p.setName((i.getActor().getDisplayName() != null ? i
							.getActor().getDisplayName() : ""));

					p.setPhotoUrl((i.getActor().getImage().getUrl()) != null ? (i
							.getActor().getImage().getUrl()) : "");

					p.setLink((i.getObject().getContent() != null ? i
							.getObject().getContent() : ""));

					p.setSocialMediaType(social_media_type);

					p.setChild_user_id(child_user_id);
					p.setTotalCommentCount(0);

					DateTime retriveDate = new DateTime(i.getUpdated());
					DateTime sendingDate = new DateTime(retriveDate.getYear(),
							retriveDate.getMonthOfYear(),
							retriveDate.getDayOfMonth(), 0, 0, 0);
					p.setUpdatedDate(sendingDate.getMillis());
					p.setUpdateDateTime(retriveDate.getMillis());

					p.setCaption((i.getTitle() != null ? i.getTitle() : ""));

					p.setDescription((i.getObject().getContent() != null ? i
							.getObject().getContent() : ""));

					p.setStory((i.getVerb() != null ? i.getVerb() : ""));

					p.setFromId((i.getActor().getId() != null ? i.getActor()
							.getId() : ""));

					p.setAttribution((i.getGeocode() != null ? i.getGeocode()
							: ""));

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
							p1 = mongoOps.findById(i.getId(), Person.class,
									PERSON_COLLECTION);
							p.setReadFlag(p1.getReadFlag());
							p.setSaveFlag(p1.getSaveFlag());
							p.setDeleteFlag(p1.getDeleteFlag());
							p.setErrorFlag(p1.getErrorFlag());
							p.setErrorMessage("");
							p.setNotificationFlag(p1.getNotificationFlag());
						}
					} catch (Exception e) {
						logger.info("Googleplus flag modification exception.."
								+ e.toString());
					}

					p.setParentId(parent_id);
					p.setSocialSiteUserId(social_site_user_id);

					
					logger.info("New post with Data: " + p.toString()
							+ " for social type:" + p.getSocialMediaType());
					mongoOps.save(p, PERSON_COLLECTION);
					JSONObject obj = new JSONObject();
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
					logger.info("Json object that will be sent to response queue :    "
							+ obj.toJSONString());
					countFeed++;
					Greeting result = new Greeting();
					result.setId((String) obj.get("id"));
					result.setUuid((String) obj.get("uuid"));
					result.setChild_user_id((String) obj.get("child_user_id"));
					result.setParent_id((String) obj.get("parent_id"));
					result.setSocialmediaType((String) obj
							.get("socialmediaType"));
					System.out.println("$$$$$$$$$$$$countFeed$$$$$$$$$"
							+ countFeed);
					arrayGreetings.add(result);
					System.out.println("\n arrayGreetings**********"
							+ arrayGreetings.size());

					

					logger.info("###############End#############" + i.getId());
				}
			} catch (Exception e) {
				logger.error("UnknownHostException: e.toSting: " + e.toString());
				e.printStackTrace();
			} finally {

			}
			logger.info("Thread  ending ");
		} catch (Exception e) {
			logger.error("Exception:" + e.getMessage());
			e.printStackTrace();
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

}
